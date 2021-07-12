/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.transferapi.impl.item;

import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import aztech.modern_industrialization.transferapi.api.item.ItemPreconditions;
import aztech.modern_industrialization.transferapi.api.item.PlayerInventoryWrapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

// A wrapper around a PlayerInventory with the additional functions in PlayerInventoryWrapper.
class PlayerInventoryWrapperImpl extends CombinedStorage<ItemKey, InventorySlotWrapper> implements PlayerInventoryWrapper {
    private final PlayerInventory playerInventory;
    private final CursorSlotWrapper cursorSlotWrapper;
    private final DroppedStacks droppedStacks;

    PlayerInventoryWrapperImpl(List<InventorySlotWrapper> slots, PlayerInventory playerInventory) {
        super(slots);
        this.playerInventory = playerInventory;
        this.cursorSlotWrapper = new CursorSlotWrapper();
        this.droppedStacks = new DroppedStacks();
    }

    @Override
    public void offerOrDrop(ItemKey resource, long amount, TransactionContext tx) {
        ItemPreconditions.notBlankNotNegative(resource, amount);

        for (int iteration = 0; iteration < 2; iteration++) {
            boolean allowEmptySlots = iteration == 1;

            for (InventorySlotWrapper slot : parts) {
                if (!slot.inventory.getStack(slot.slot).isEmpty() || allowEmptySlots) {
                    amount -= slot.insert(resource, amount, tx);
                }
            }
        }

        // Drop leftover in the world on the server side (will be synced by the game
        // with the client).
        // Dropping items is server-side only because it involves randomness.
        if (amount > 0 && playerInventory.player.world.isClient()) {
            droppedStacks.addDrop(resource, amount, tx);
        }
    }

    @Override
    public Storage<ItemKey> slotWrapper(int slot) {
        return parts.get(slot);
    }

    @Override
    public Storage<ItemKey> cursorSlotWrapper() {
        return cursorSlotWrapper;
    }

    private class CursorSlotWrapper extends SnapshotParticipant<ItemStack> implements Storage<ItemKey>, StorageView<ItemKey> {
        @Override
        public boolean supportsInsertion() {
            return true;
        }

        @Override
        public long insert(ItemKey itemKey, long maxAmount, TransactionContext transaction) {
            ItemPreconditions.notBlankNotNegative(itemKey, maxAmount);
            ItemStack stack = playerInventory.getCursorStack();
            int inserted = (int) Math.min(maxAmount, Math.min(64, itemKey.getItem().getMaxCount()) - stack.getCount());

            if (stack.isEmpty()) {
                ItemStack keyStack = itemKey.toStack(inserted);
                this.updateSnapshots(transaction);
                playerInventory.setCursorStack(keyStack);
                return inserted;
            } else if (itemKey.matches(stack)) {
                this.updateSnapshots(transaction);
                stack.increment(inserted);
                return inserted;
            }

            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        public long extract(ItemKey itemKey, long maxAmount, TransactionContext transaction) {
            ItemPreconditions.notBlankNotNegative(itemKey, maxAmount);
            ItemStack stack = playerInventory.getCursorStack();

            if (itemKey.matches(stack)) {
                int extracted = (int) Math.min(stack.getCount(), maxAmount);
                this.updateSnapshots(transaction);
                stack.decrement(extracted);
                return extracted;
            }

            return 0;
        }

        @Override
        public Iterator<StorageView<ItemKey>> iterator(TransactionContext transaction) {
            return SingleViewIterator.create(this, transaction);
        }

        @Override
        public ItemKey getResource() {
            return ItemKey.of(playerInventory.getCursorStack());
        }

        @Override
        public long getCapacity() {
            return playerInventory.getCursorStack().getMaxCount();
        }

        @Override
        public boolean isResourceBlank() {
            return playerInventory.getCursorStack().isEmpty();
        }

        @Override
        public long getAmount() {
            return playerInventory.getCursorStack().getCount();
        }

        @Override
        protected ItemStack createSnapshot() {
            return playerInventory.getCursorStack().copy();
        }

        @Override
        protected void readSnapshot(ItemStack snapshot) {
            playerInventory.setCursorStack(snapshot);
        }

        @Override
        public void onFinalCommit() {
            playerInventory.markDirty();
        }
    }

    private class DroppedStacks extends SnapshotParticipant<Integer> {
        final List<ItemKey> droppedKeys = new ArrayList<>();
        final List<Long> droppedCounts = new ArrayList<>();

        void addDrop(ItemKey key, long count, TransactionContext transaction) {
            updateSnapshots(transaction);
            droppedKeys.add(key);
            droppedCounts.add(count);
        }

        @Override
        protected Integer createSnapshot() {
            return droppedKeys.size();
        }

        @Override
        protected void readSnapshot(Integer snapshot) {
            // effectively cancel dropping the stacks
            int previousSize = snapshot;
            while (droppedKeys.size() > previousSize) {
                droppedKeys.remove(droppedKeys.size() - 1);
                droppedCounts.remove(droppedCounts.size() - 1);
            }
        }

        @Override
        protected void onFinalCommit() {
            // drop the stacks and mark dirty
            for (int i = 0; i < droppedKeys.size(); ++i) {
                ItemKey key = droppedKeys.get(i);

                while (droppedCounts.get(i) > 0) {
                    int dropped = (int) Math.min(key.getItem().getMaxCount(), droppedCounts.get(i));
                    playerInventory.player.dropStack(key.toStack(dropped));
                    droppedCounts.set(i, droppedCounts.get(i) - dropped);
                }
            }

            droppedKeys.clear();
            droppedCounts.clear();
        }
    }
}

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
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

// A wrapper around a single slot of an inventory
// We must ensure that only one instance of this class exists for every inventory slot,
// or the transaction logic will not work correct.
class InventorySlotWrapper extends SnapshotParticipant<ItemStack> implements Storage<ItemKey>, StorageView<ItemKey> {
    final Inventory inventory;
    final int slot;

    InventorySlotWrapper(Inventory inventory, int slot) {
        this.inventory = inventory;
        this.slot = slot;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public long insert(ItemKey key, long maxAmount, Transaction transaction) {
        // TODO: clean this up
        ItemPreconditions.notEmpty(key);
        int count = (int) Math.min(maxAmount, inventory.getMaxCountPerStack());
        ItemStack stack = inventory.getStack(slot);

        if (stack.isEmpty()) {
            ItemStack keyStack = key.toStack(count);

            if (inventory.isValid(slot, keyStack)) {
                int inserted = Math.min(keyStack.getMaxCount(), count);
                this.updateSnapshots(transaction);
                keyStack.setCount(inserted);
                inventory.setStack(slot, keyStack);
                return inserted;
            }
        } else if (key.matches(stack)) {
            int inserted = Math.min(stack.getMaxCount() - stack.getCount(), count);
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
    public long extract(ItemKey key, long maxAmount, Transaction transaction) {
        ItemPreconditions.notEmpty(key);
        ItemStack stack = inventory.getStack(slot);

        if (key.matches(stack)) {
            int extracted = (int) Math.min(stack.getCount(), maxAmount);
            this.updateSnapshots(transaction);
            stack.decrement(extracted);
            return extracted;
        }

        return 0;
    }

    @Override
    public boolean forEach(Storage.Visitor<ItemKey> visitor, Transaction transaction) {
        if (!inventory.getStack(slot).isEmpty()) {
            return visitor.accept(this);
        }

        return false;
    }

    @Override
    public ItemKey resource() {
        return ItemKey.of(inventory.getStack(slot));
    }

    @Override
    public long amount() {
        return inventory.getStack(slot).getCount();
    }

    @Override
    protected ItemStack createSnapshot() {
        return inventory.getStack(slot).copy();
    }

    @Override
    protected void readSnapshot(ItemStack snapshot) {
        inventory.setStack(slot, snapshot);
    }

    @Override
    public void onFinalCommit() {
        inventory.markDirty();
    }
}

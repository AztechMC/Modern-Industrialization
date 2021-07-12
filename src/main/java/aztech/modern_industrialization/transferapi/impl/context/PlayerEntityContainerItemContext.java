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
package aztech.modern_industrialization.transferapi.impl.context;

import aztech.modern_industrialization.transferapi.api.context.ContainerItemContext;
import aztech.modern_industrialization.transferapi.api.item.InventoryWrappers;
import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import aztech.modern_industrialization.transferapi.api.item.PlayerInventoryWrapper;
import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class PlayerEntityContainerItemContext implements ContainerItemContext {
    private final ItemKey boundKey;
    private final Storage<ItemKey> slot;
    private final PlayerInventoryWrapper wrapper;

    public static ContainerItemContext ofHand(PlayerEntity player, Hand hand) {
        PlayerInventoryWrapper wrapper = InventoryWrappers.ofPlayerInventory(player.inventory);
        int slot = hand == Hand.MAIN_HAND ? player.inventory.selectedSlot : 40;
        return new PlayerEntityContainerItemContext(ItemKey.of(player.inventory.getStack(slot)), wrapper.slotWrapper(slot), wrapper);
    }

    public static ContainerItemContext ofCursor(PlayerEntity player) {
        PlayerInventoryWrapper wrapper = InventoryWrappers.ofPlayerInventory(player.inventory);
        return new PlayerEntityContainerItemContext(ItemKey.of(player.inventory.getCursorStack()), wrapper.cursorSlotWrapper(), wrapper);
    }

    private PlayerEntityContainerItemContext(ItemKey boundKey, Storage<ItemKey> slot, PlayerInventoryWrapper wrapper) {
        this.boundKey = boundKey;
        this.slot = slot;
        this.wrapper = wrapper;
    }

    @Override
    public long getCount(TransactionContext tx) {
        long count = 0;
        for (StorageView<ItemKey> view : slot.iterable(tx)) {
            if (view.getResource().equals(boundKey)) {
                count = view.getAmount();
            }
        }
        return count;
    }

    @Override
    public boolean transform(long count, ItemKey into, TransactionContext tx) {
        Preconditions.checkArgument(count <= getCount(tx), "Can't transform items that are not available.");

        if (slot.extract(boundKey, count, tx) != count) {
            throw new AssertionError("Implementation error.");
        }

        if (!into.isEmpty()) {
            count -= slot.insert(into, count, tx);
            wrapper.offerOrDrop(into, count, tx);
        }

        return true;
    }
}

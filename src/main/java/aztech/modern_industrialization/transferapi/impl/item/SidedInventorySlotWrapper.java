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
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.Direction;

// Wraps an InventorySlotWrapper with SidedInventory#canInsert and SidedInventory#canExtract checks for a given direction.
class SidedInventorySlotWrapper implements Storage<ItemKey> {
    private final InventorySlotWrapper slotWrapper;
    private final SidedInventory sidedInventory; // TODO: should we just cast slotWrapper.inventory instead?
    private final Direction direction;

    SidedInventorySlotWrapper(InventorySlotWrapper slotWrapper, SidedInventory sidedInventory, Direction direction) {
        this.slotWrapper = slotWrapper;
        this.sidedInventory = sidedInventory;
        this.direction = direction;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public long insert(ItemKey resource, long maxAmount, Transaction transaction) {
        if (!sidedInventory.canInsert(slotWrapper.slot, resource.toStack(), direction)) {
            return 0;
        } else {
            return slotWrapper.insert(resource, maxAmount, transaction);
        }
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public long extract(ItemKey resource, long maxAmount, Transaction transaction) {
        if (!sidedInventory.canExtract(slotWrapper.slot, resource.toStack(), direction)) {
            return 0;
        } else {
            return slotWrapper.insert(resource, maxAmount, transaction);
        }
    }

    @Override
    public Iterator<StorageView<ItemKey>> iterator(Transaction transaction) {
        return slotWrapper.iterator(transaction);
    }
}

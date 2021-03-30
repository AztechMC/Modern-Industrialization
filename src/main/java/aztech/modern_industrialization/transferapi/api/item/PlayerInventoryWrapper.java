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
package aztech.modern_industrialization.transferapi.api.item;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.ApiStatus;

/**
 * A wrapper around a PlayerInventory.
 *
 * <p>
 * Do not implement. Obtain an instance through
 * {@link InventoryWrappers#ofPlayerInventory} instead.
 */
@ApiStatus.NonExtendable
public interface PlayerInventoryWrapper {
    /**
     * Return a wrapper around a specific slot of the player inventory.
     *
     * <p>
     * Slots 0 to 35 are for the main inventory, slots 36 to 39 are for the armor,
     * and slot 40 is the offhand slot.
     */
    Storage<ItemKey> slotWrapper(int index);

    /**
     * Return a wrapper around the cursor slot of the player inventory.
     */
    Storage<ItemKey> cursorSlotWrapper();

    /**
     * Add items to the inventory if possible, and drop any leftover items in the
     * world.
     */
    void offerOrDrop(ItemKey key, long amount, Transaction transaction);
}

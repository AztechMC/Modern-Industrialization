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
package aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleSlotStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.impl.TransferApiImpl;
import java.util.List;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * A {@link Storage} implementation made of indexed slots.
 *
 * <p>
 * Please note that some storages may not implement this interface.
 * It is up to the storage implementation to decide whether to implement this interface or not.
 * Checking whether a storage is slotted can be done using {@code instanceof}.
 *
 * @param <T> The type of the stored resources.
 */
public interface SlottedStorage<T> extends Storage<T> {
    /**
     * Retrieve the number of slots in this storage.
     */
    int getSlotCount();

    /**
     * Retrieve a specific slot of this storage.
     *
     * @throws IndexOutOfBoundsException If the slot index is out of bounds.
     */
    SingleSlotStorage<T> getSlot(int slot);

    /**
     * Retrieve a list containing all the slots of this storage. <b>The list must not be modified.</b>
     *
     * <p>
     * This function can be used to interface with code that requires a slot list,
     * for example {@link StorageUtil#insertStacking} or {@link ContainerItemContext#getAdditionalSlots()}.
     *
     * <p>
     * It is guaranteed that calling this function is fast.
     * The default implementation returns a view over the storage that delegates to {@link #getSlotCount} and {@link #getSlot}.
     */
    @UnmodifiableView
    default List<SingleSlotStorage<T>> getSlots() {
        return TransferApiImpl.makeListView(this);
    }
}

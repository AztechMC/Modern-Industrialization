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
package aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.SlottedStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StorageView;
import aztech.modern_industrialization.thirdparty.fabrictransfer.impl.TransferApiImpl;
import java.util.Iterator;

/**
 * A storage that is also its only storage view.
 * It can be used in APIs for storages that are wrappers around a single "slot", or for slightly more convenient implementation.
 *
 * @param <T> The type of the stored resource.
 */
public interface SingleSlotStorage<T> extends SlottedStorage<T>, StorageView<T> {
    @Override
    default Iterator<StorageView<T>> iterator() {
        return TransferApiImpl.singletonIterator(this);
    }

    @Override
    default int getSlotCount() {
        return 1;
    }

    @Override
    default SingleSlotStorage<T> getSlot(int slot) {
        if (slot != 0) {
            throw new IndexOutOfBoundsException("Slot " + slot + " does not exist in a single-slot storage.");
        }

        return this;
    }
}

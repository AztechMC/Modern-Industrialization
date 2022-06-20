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
package aztech.modern_industrialization.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class StorageUtil2 {
    /**
     * Wrap vanilla inventories so that insertion tries to stack slots first.
     * Hehehehe.
     */
    @Nullable
    public static Storage<ItemVariant> wrapInventory(@Nullable Storage<ItemVariant> foundStorage) {
        if (foundStorage instanceof InventoryStorage wrapper) {
            return new SmarterInventoryStorage(wrapper.getSlots());
        } else {
            return foundStorage;
        }
    }

    private static class SmarterInventoryStorage extends CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> {
        public SmarterInventoryStorage(List<SingleSlotStorage<ItemVariant>> parts) {
            super(parts);
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            long amount = 0;

            for (int iter = 0; iter < 2; ++iter) {
                for (SingleSlotStorage<ItemVariant> part : parts) {
                    if (iter == 1 || !part.isResourceBlank()) {
                        amount += part.insert(resource, maxAmount - amount, transaction);
                    }
                }
            }

            return amount;
        }
    }

    public static <T> Iterator<T> singletonIterator(T it) {
        return new Iterator<T>() {
            boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public T next() {
                if (!hasNext) {
                    throw new NoSuchElementException();
                }

                hasNext = false;
                return it;
            }
        };
    }
}

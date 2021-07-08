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

import dev.technici4n.fasttransferlib.experimental.api.item.InventoryWrapper;
import dev.technici4n.fasttransferlib.experimental.api.item.ItemKey;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class StorageUtil2 {
    @Nullable
    public static <T> T findExtractableResource(@Nullable Storage<T> storage, @Nullable Transaction transaction) {
        if (storage == null)
            return null;

        try (Transaction nested = transaction == null ? Transaction.openOuter() : transaction.openNested()) {
            for (StorageView<T> view : storage.iterable(nested)) {
                // Extract below could change the resource, so we have to query it before
                // extracting.
                T resource = view.resource();

                if (!view.isEmpty() && view.extract(resource, Long.MAX_VALUE, nested) > 0) {
                    // Will abort the extraction.
                    return resource;
                }
            }
        }

        return null;
    }

    /**
     * Wrap vanilla inventories so that insertion tries to stack slots first.
     * Hehehehe. // TODO: update when the item API is updated, really dirty right
     * now.
     */
    @Nullable
    public static Storage<ItemKey> wrapInventory(@Nullable Storage<ItemKey> foundStorage) {
        if (foundStorage instanceof InventoryWrapper wrapper) {
            List<Storage<ItemKey>> slots = ((CombinedStorage<ItemKey, Storage<ItemKey>>) wrapper).parts;
            return new InventoryStorage(slots);
        } else {
            return foundStorage;
        }
    }

    private static boolean isEmpty(Storage<ItemKey> storage, Transaction transaction) {
        for (StorageView<ItemKey> view : storage.iterable(transaction)) {
            if (view.amount() > 0) {
                return false;
            }
        }

        return true;
    }

    private static class InventoryStorage extends CombinedStorage<ItemKey, Storage<ItemKey>> {
        public InventoryStorage(List<Storage<ItemKey>> parts) {
            super(parts);
        }

        @Override
        public long insert(ItemKey resource, long maxAmount, Transaction transaction) {
            StoragePreconditions.notEmptyNotNegative(resource, maxAmount);
            long amount = 0;

            for (int iter = 0; iter < 2; ++iter) {
                for (Storage<ItemKey> part : parts) {
                    if (iter == 1 || !isEmpty(part, transaction)) {
                        amount += part.insert(resource, maxAmount - amount, transaction);
                    }
                }
            }

            return amount;
        }
    }
}

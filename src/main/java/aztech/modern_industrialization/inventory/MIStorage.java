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
package aztech.modern_industrialization.inventory;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class MIStorage<T, K extends TransferVariant<T>, S extends AbstractConfigurableStack<T, K>> implements Storage<K> {
    final List<S> stacks;
    private final boolean oneSlotPerResource; // true for fluids, false for items

    protected MIStorage(List<S> stacks, boolean oneSlotPerResource) {
        this.stacks = stacks;
        this.oneSlotPerResource = oneSlotPerResource;
    }

    /**
     * @param filter    Return false to skip some configurable stacks.
     * @param lockSlots Whether to lock slots or not.
     */
    public long insert(K resource, long maxAmount, TransactionContext tx, Predicate<? super S> filter, boolean lockSlots) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long totalInserted = 0;

        outer: for (int iter = 0; iter < 2; ++iter) {
            boolean insertIntoEmptySlots = iter == 1;
            for (S stack : stacks) {
                if (filter.test(stack) && stack.isResourceAllowedByLock(resource)) {
                    if ((stack.getAmount() == 0 && insertIntoEmptySlots) || stack.getResource().equals(resource)) {
                        long inserted = Math.min(maxAmount - totalInserted, stack.getRemainingCapacityFor(resource));

                        if (inserted > 0) {
                            stack.updateSnapshots(tx);
                            stack.setKey(resource);
                            stack.increment(inserted);

                            if (lockSlots) {
                                stack.enableMachineLock(resource.getObject());
                            }
                        }

                        totalInserted += inserted;

                        if (oneSlotPerResource) {
                            break outer;
                        }
                    }
                }
            }
        }

        return totalInserted;
    }

    @Override
    public long insert(K resource, long maxAmount, TransactionContext transaction) {
        return insert(resource, maxAmount, transaction, AbstractConfigurableStack::canPipesInsert, false);
    }

    @Override
    public long extract(K resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long amount = 0;

        for (int i = 0; i < stacks.size() && amount < maxAmount; ++i) {
            amount += stacks.get(i).extract(resource, maxAmount - amount, transaction);
        }

        return amount;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Iterator<StorageView<K>> iterator(TransactionContext transaction) {
        return (Iterator) stacks.iterator();
    }
}

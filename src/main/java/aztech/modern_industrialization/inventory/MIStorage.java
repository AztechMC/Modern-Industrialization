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

import aztech.modern_industrialization.transfer.MIPreconditions;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.DataComponentHolderResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.List;
import java.util.function.Predicate;

public class MIStorage<T, K extends DataComponentHolderResource<T>, S extends AbstractConfigurableStack<T, K>> implements ResourceHandler<K> {
    final List<S> stacks;
    private final boolean oneSlotPerResource; // true for fluids, false for items

    protected MIStorage(List<S> stacks, boolean oneSlotPerResource) {
        this.stacks = stacks;
        this.oneSlotPerResource = oneSlotPerResource;
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public K getResource(int index) {
        return stacks.get(index).getResource();
    }

    @Override
    public long getAmountAsLong(int index) {
        return stacks.get(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, K resource) {
        return stacks.get(index).getTotalCapacityFor(resource.value());
    }

    @Override
    public boolean isValid(int index, K resource) {
        return stacks.get(index).isResourceAllowedByLock(resource);
    }

    private int insertDirect(S stack, K resource, int amount, TransactionContext transaction) {
        if ((stack.getAmount() == 0 && stack.isResourceAllowedByLock(resource)) || stack.getResource().equals(resource)) {
            int inserted = Math.min(amount, stack.getRemainingCapacityFor(resource));

            if (inserted > 0) {
                stack.updateSnapshots(transaction);
                stack.setKey(resource);
                stack.increment(inserted);
            }

            return inserted;
        }

        return 0;
    }

    @Override
    public int insert(int index, K resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        S stack = stacks.get(index);
        if (!stack.canPipesInsert()) {
            return 0;
        }
        return insertDirect(stack, resource, amount, transaction);
    }

    /**
     * @param filter    Return false to skip some configurable stacks.
     * @param lockSlots Whether to lock slots or not.
     */
    public int insert(K resource, int maxAmount, TransactionContext tx, Predicate<? super S> filter, boolean lockSlots) {
        MIPreconditions.checkNonEmptyNonNegative(resource, maxAmount);
        boolean containsResourceAlready = false;
        int totalInserted = 0;

        outer:
        for (int iter = 0; iter < 2; ++iter) {
            for (S stack : stacks) {
                if (!filter.test(stack))
                    continue;
                boolean isSlotEmpty = stack.getAmount() == 0 && stack.getLockedInstance() == null;
                boolean canInsert;

                if (isSlotEmpty) {
                    // Always check for the second iteration.
                    if (oneSlotPerResource) {
                        // Additionally check that the resource is not contained yet.
                        canInsert = iter == 1 && !containsResourceAlready;
                    } else {
                        canInsert = iter == 1;
                    }
                } else {
                    canInsert = true;
                }

                if (canInsert) {
                    int inserted = insertDirect(stack, resource, Math.min(maxAmount - totalInserted, stack.getRemainingCapacityFor(resource)), tx);

                    if (inserted > 0 && lockSlots) {
                        stack.enableMachineLock(resource.value());
                    }

                    totalInserted += inserted;
                }

                containsResourceAlready = containsResourceAlready || stack.getResource().equals(resource);
            }
        }

        return totalInserted;
    }

    public int insertAllSlot(K resource, int maxAmount, TransactionContext tx) {
        return insert(resource, maxAmount, tx, s -> true, false);
    }

    @Override
    public int insert(K resource, int maxAmount, TransactionContext transaction) {
        return insert(resource, maxAmount, transaction, AbstractConfigurableStack::canPipesInsert, false);
    }

    private int extractDirect(S stack, K key, int maxAmount, TransactionContext transaction) {
        if (key.equals(stack.getResource())) {
            int extracted = Math.min(stack.getAmount(), maxAmount);
            stack.updateSnapshots(transaction);
            stack.decrement(extracted);
            return extracted;
        }
        return 0;
    }

    @Override
    public int extract(int index, K resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        S stack = stacks.get(index);
        if (!stack.canPipesExtract()) {
            return 0;
        }
        return extractDirect(stack, resource, amount, transaction);
    }

    public int extract(K resource, int maxAmount, TransactionContext transaction, Predicate<? super S> filter) {
        MIPreconditions.checkNonEmptyNonNegative(resource, maxAmount);
        int amount = 0;
        for (int i = 0; i < stacks.size() && amount < maxAmount; ++i) {
            S stack = stacks.get(i);
            if (!filter.test(stack)) {
                continue;
            }
            amount += extractDirect(stack, resource, maxAmount - amount, transaction);
        }
        return amount;
    }

    @Override
    public int extract(K resource, int maxAmount, TransactionContext transaction) {
        return extract(resource, maxAmount, transaction, AbstractConfigurableStack::canPipesExtract);
    }

    /*
     * Ignore requirement for slot to have pipeExtract = true
     */
    public int extractAllSlot(K resource, int maxAmount, TransactionContext transaction, Predicate<? super S> filter) {
        return extract(resource, maxAmount, transaction, filter);
    }

    public int extractAllSlot(K resource, int maxAmount, TransactionContext transaction) {
        return extractAllSlot(resource, maxAmount, transaction, s -> true);
    }
}

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

import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import aztech.modern_industrialization.transferapi.api.item.ItemPreconditions;
import com.google.common.primitives.Ints;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public class MIItemStorage implements Storage<ItemKey> {
    final List<ConfigurableItemStack> stacks;

    public MIItemStorage(List<ConfigurableItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    /**
     * @param filter    Return false to skip some ConfigurableItemStacks.
     * @param lockSlots Whether to lock slots or not.
     */
    public long insert(ItemKey key, int count, Transaction tx, Predicate<ConfigurableItemStack> filter, boolean lockSlots) {
        ItemPreconditions.notEmptyNotNegative(key, count);
        int totalInsert = 0;
        for (int iter = 0; iter < 2; ++iter) {
            boolean insertIntoEmptySlots = iter == 1;
            for (ConfigurableItemStack stack : stacks) {
                if (filter.test(stack) && stack.isValid(key.getItem())) {
                    if ((stack.getCount() == 0 && insertIntoEmptySlots) || stack.getItemKey().equals(key)) {
                        int inserted = Math.min(count, Math.min(key.getItem().getMaxCount(), 64) - stack.getCount());

                        if (inserted > 0) {
                            totalInsert += inserted;
                            count -= inserted;
                            stack.updateSnapshots(tx);
                            stack.setItemKey(key);
                            stack.increment(inserted);

                            if (lockSlots) {
                                stack.enableMachineLock(key.getItem());
                            }
                        }
                    }
                }
            }
        }
        return totalInsert;
    }

    @Override
    public long insert(ItemKey key, long count, Transaction transaction) {
        return insert(key, Ints.saturatedCast(count), transaction, ConfigurableItemStack::canPipesInsert, false);
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public long extract(ItemKey key, long maxAmount, Transaction transaction) {
        ItemPreconditions.notEmptyNotNegative(key, maxAmount);
        long amount = 0L;

        for (int i = 0; i < stacks.size() && amount < maxAmount; ++i) {
            amount += this.stacks.get(i).extract(key, maxAmount - amount, transaction);
        }

        return amount;
    }

    @Override
    public boolean forEach(Visitor<ItemKey> visitor, Transaction transaction) {
        for (ConfigurableItemStack stack : stacks) {
            if (stack.getCount() > 0) {
                if (visitor.accept(stack)) {
                    return true;
                }
            }
        }

        return false;
    }
}

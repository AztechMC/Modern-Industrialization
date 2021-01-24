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

import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.fabricmc.fabric.api.transfer.v1.base.CombinedStorageFunction;
import net.fabricmc.fabric.api.transfer.v1.item.ItemPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageFunction;

public class MIItemStorage implements Storage<ItemKey> {
    private final List<ConfigurableItemStack> stacks;
    private final ItemInsertionFunction insertionFunction;
    private final StorageFunction<ItemKey> extractionFunction;

    public MIItemStorage(List<ConfigurableItemStack> stacks) {
        this.stacks = stacks;
        this.extractionFunction = new CombinedStorageFunction<>(
                stacks.stream().map(ConfigurableItemStack::extractionFunction).collect(Collectors.toList()));
        this.insertionFunction = (key, count, tx, filter, lockSlots) -> {
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
                                tx.enlist(stack);
                                stack.decrement(inserted);

                                if (lockSlots) {
                                    stack.enableMachineLock(key.getItem());
                                }
                            }
                        }
                    }
                }
            }
            return totalInsert;
        };
    }

    @Override
    public ItemInsertionFunction insertionFunction() {
        return insertionFunction;
    }

    @Override
    public StorageFunction<ItemKey> extractionFunction() {
        return extractionFunction;
    }

    @Override
    public boolean forEach(Visitor<ItemKey> visitor) {
        for (ConfigurableItemStack stack : stacks) {
            if (stack.getCount() > 0) {
                if (visitor.visit(stack)) {
                    return true;
                }
            }
        }

        return false;
    }
}

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

import com.google.common.primitives.Ints;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.fabricmc.fabric.api.transfer.v1.base.IntegerStorageFunction;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

/**
 * An integer insertion function that can also lock slots.
 */
@FunctionalInterface
public interface ItemInsertionFunction extends IntegerStorageFunction<ItemKey> {
    /**
     * @param filter    Return false to skip some ConfigurableItemStacks.
     * @param lockSlots Whether to lock slots or not.
     */
    long apply(ItemKey key, int count, Transaction tx, Predicate<ConfigurableItemStack> filter, boolean lockSlots);

    @Override
    default long applyFixedDenominator(ItemKey key, long count, Transaction tx) {
        return apply(key, Ints.saturatedCast(count), tx, ConfigurableItemStack::canPipesInsert, false);
    }
}

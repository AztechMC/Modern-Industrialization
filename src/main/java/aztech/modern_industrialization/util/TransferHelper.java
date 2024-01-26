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

import aztech.modern_industrialization.MI;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class TransferHelper {
    public static void moveAll(IItemHandler src, IItemHandler target, boolean stackInTarget) {
        int srcSlots = src.getSlots();

        for (int i = 0; i < srcSlots; ++i) {
            // Simulate
            var extracted = src.extractItem(i, Integer.MAX_VALUE, true);
            if (extracted.isEmpty()) {
                continue;
            }
            int extractedCount = extracted.getCount();

            var leftover = stackInTarget ? ItemHandlerHelper.insertItemStacked(target, extracted, true)
                    : ItemHandlerHelper.insertItem(target, extracted, true);
            int insertedCount = extractedCount - leftover.getCount();
            if (insertedCount <= 0) {
                continue;
            }

            // Action
            extracted = src.extractItem(i, insertedCount, false);
            if (extracted.isEmpty()) {
                continue;
            }

            leftover = stackInTarget ? ItemHandlerHelper.insertItemStacked(target, extracted, false)
                    : ItemHandlerHelper.insertItem(target, extracted, false);
            if (!leftover.isEmpty()) {
                // Try to give overflow back
                leftover = src.insertItem(i, leftover, false);

                if (!leftover.isEmpty()) {
                    MI.LOGGER.warn("Item handler {} rejected {}, discarding.", target, leftover);
                }
            }
        }
    }

    public static ItemStack extractMatching(IItemHandler src, Predicate<ItemStack> predicate, int maxAmount) {
        int srcSlots = src.getSlots();

        // Find first stack
        ItemStack ret = ItemStack.EMPTY;
        int slot;
        for (slot = 0; slot < srcSlots && ret.isEmpty(); ++slot) {
            var stack = src.getStackInSlot(slot);
            if (predicate.test(stack)) {
                ret = src.extractItem(slot, maxAmount, false);
            }
        }

        // Try to extract more
        ++slot;
        for (; slot < srcSlots && maxAmount < ret.getCount(); ++slot) {
            var stack = src.getStackInSlot(slot);
            if (ItemStack.matches(stack, ret)) {
                var extracted = src.extractItem(slot, maxAmount - ret.getCount(), true);
                ret.grow(extracted.getCount());
            }
        }

        return ItemStack.EMPTY;
    }
}

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

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.world.item.ItemStack;

public class ItemStackHelper {
    public static boolean areEqualIgnoreCount(ItemStack s1, ItemStack s2) {
        return ItemStack.isSameItemSameTags(s1, s2);
    }

    /**
     * Try to consume the fuel in the stack.
     * 
     * @param stack    The fuel stack
     * @param simulate If true, the stack will not be modified
     * @return false if the fuel could not be consumed, true otherwise
     */
    public static boolean consumeFuel(ConfigurableItemStack stack, boolean simulate) {
        if (stack.isResourceBlank())
            return false;
        var itemStack = stack.toStack();
        if (itemStack.hasCraftingRemainingItem()) {
            var remainder = ItemVariant.of(itemStack.getCraftingRemainingItem());
            if (stack.getAmount() == 1 && stack.isResourceAllowedByLock(remainder)) {
                if (!simulate) {
                    stack.setAmount(1);
                    stack.setKey(remainder);
                }
                return true;
            }
        } else {
            if (!simulate) {
                stack.decrement(1);
            }
            return true;
        }
        return false;
    }
}

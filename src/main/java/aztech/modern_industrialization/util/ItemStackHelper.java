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
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackHelper {
    public static boolean areEqualIgnoreCount(ItemStack s1, ItemStack s2) {
        return ItemStack.areItemsEqual(s1, s2) && ItemStack.areNbtEqual(s1, s2);
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
        Item item = stack.getResource().getItem();
        if (item.hasRecipeRemainder()) {
            if (stack.getAmount() == 1 && stack.isResourceAllowedByLock(item.getRecipeRemainder())) {
                if (!simulate) {
                    stack.setAmount(1);
                    stack.setKey(ItemVariant.of(item.getRecipeRemainder()));
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

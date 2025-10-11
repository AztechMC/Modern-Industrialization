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

import aztech.modern_industrialization.util.UnsupportedOperationInventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Hacky base slot to work around vanilla mutating stacks directly.
 */
public abstract class HackySlot extends Slot {
    // Vanilla MC code modifies the stack returned by `getStack()` directly, but it
    // calls `markDirty()` when that happens, so we just cache the returned stack,
    // and set it when `markDirty()` is called.
    private ItemStack cachedReturnedStack = null;

    public HackySlot(int x, int y) {
        super(new UnsupportedOperationInventory(), 0, x, y);
    }

    protected abstract ItemStack getRealStack();

    protected abstract void setRealStack(ItemStack stack);

    @Override
    public final ItemStack getItem() {
        return cachedReturnedStack = getRealStack();
    }

    @Override
    public final void set(ItemStack stack) {
        setRealStack(stack);
        cachedReturnedStack = stack;
    }

    @Override
    public final void setChanged() {
        if (cachedReturnedStack != null) {
            set(cachedReturnedStack);
        }
    }

    @Override
    public final ItemStack remove(int amount) {
        var stack = getRealStack().copy();
        var ret = stack.split(amount);
        set(stack);
        cachedReturnedStack = null;
        return ret;
    }
}

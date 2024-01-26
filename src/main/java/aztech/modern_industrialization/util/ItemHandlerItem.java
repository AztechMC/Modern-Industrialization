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

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyHandler;
import net.neoforged.neoforge.items.wrapper.ForwardingItemHandler;
import org.jetbrains.annotations.Nullable;

public class ItemHandlerItem extends ForwardingItemHandler {
    private final SlotAccess slot;

    public ItemHandlerItem(SlotAccess slot, Function<ItemStack, @Nullable IItemHandler> getter) {
        super(() -> {
            var cap = getter.apply(slot.get());
            return cap == null ? EmptyHandler.INSTANCE : cap;
        });
        this.slot = slot;
    }

    public ItemHandlerItem(SlotAccess slot) {
        this(slot, stack -> stack.getCapability(Capabilities.ItemHandler.ITEM));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (simulate || this.slot.get().getCount() <= 1) {
            return super.insertItem(slot, stack, simulate);
        } else {
            return doOnCopy(() -> super.insertItem(slot, stack, false));
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (simulate || this.slot.get().getCount() <= 1) {
            return super.extractItem(slot, amount, simulate);
        } else {
            return doOnCopy(() -> super.extractItem(slot, amount, false));
        }
    }

    private ItemStack doOnCopy(Supplier<ItemStack> runnable) {
        var slotStack = this.slot.get();
        // Copy stack
        var stackCopy = slotStack.copy();
        stackCopy.shrink(1);

        // Do insertion via super
        slotStack.setCount(1);
        var ret = runnable.get();

        // Replace stack, and stow away the new result
        var newStack = this.slot.get();
        if (ItemStack.isSameItemSameTags(stackCopy, newStack)) {
            stackCopy.grow(1);
        } else if (!newStack.isEmpty()) {
            // TODO stow
        }

        this.slot.set(stackCopy);

        return ret;
    }
}

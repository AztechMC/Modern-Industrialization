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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import dev.technici4n.fasttransferlib.api.ContainerItemContext;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.fluid.FluidIo;
import dev.technici4n.fasttransferlib.api.item.ItemKey;
import java.util.List;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

/**
 * Helper class for fluid items that can only contain FluidFuels
 */
public interface FluidFuelItemHelper {
    static Fluid getFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? Fluids.EMPTY : NbtHelper.getFluid(tag, "fluid");
    }

    static void setFluid(ItemStack stack, Fluid fluid) {
        if (fluid != Fluids.EMPTY) {
            NbtHelper.putFluid(stack.getOrCreateTag(), "fluid", fluid);
        } else {
            stack.removeSubTag("fluid");
        }
    }

    static long getAmount(ItemStack stack) {
        if (getFluid(stack) == Fluids.EMPTY) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return tag.contains("amount") ? tag.getInt("amount") * 81 : tag.getLong("amt");
        } else {
            return 0;
        }
    }

    static void setAmount(ItemStack stack, long amount) {
        if (amount != 0) {
            stack.getOrCreateTag().putLong("amt", amount);
        } else {
            stack.removeSubTag("amt");
            stack.removeSubTag("fluid");
        }
    }

    static void decrement(ItemStack stack) {
        long amount = getAmount(stack);
        if (amount > 0) {
            setAmount(stack, Math.max(0, amount - 81));
        }
    }

    class Io implements FluidIo {
        private final Item item;
        private final Fluid fluid;
        private final long amount;
        private final long capacity;
        private final ContainerItemContext ctx;

        public Io(long capacity, ItemKey key, ContainerItemContext ctx) {
            ItemStack stack = key.toStack();
            this.item = key.getItem();
            this.fluid = FluidFuelItemHelper.getFluid(stack);
            this.amount = getAmount(stack);
            this.capacity = capacity;
            this.ctx = ctx;
        }

        @Override
        public int getFluidSlotCount() {
            return 1;
        }

        @Override
        public Fluid getFluid(int i) {
            return fluid;
        }

        @Override
        public long getFluidAmount(int i) {
            return amount;
        }

        @Override
        public boolean supportsFluidInsertion() {
            return true;
        }

        @Override
        public long insert(Fluid fluid, long amount, Simulation simulation) {
            if (ctx.getCount() <= 0)
                return amount;
            if (fluid == Fluids.EMPTY || FluidFuelRegistry.getEu(fluid) == 0)
                return amount;

            long inserted = 0;
            if (fluid == this.fluid || this.fluid == Fluids.EMPTY) {
                inserted = Math.min(capacity - this.amount, amount);
            }
            if (inserted > 0) {
                if (!updateItem(fluid, this.amount + inserted, simulation)) {
                    return amount;
                }
            }
            return amount - inserted;
        }

        private boolean updateItem(Fluid fluid, long amount, Simulation simulation) {
            ItemStack stack = new ItemStack(item);
            setAmount(stack, amount);
            setFluid(stack, fluid);
            return ctx.transform(ItemKey.of(stack), simulation);
        }
    }

    static void appendTooltip(ItemStack stack, List<Text> tooltip, long capacity) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
        Fluid fluid = getFluid(stack);
        tooltip.add(FluidHelper.getFluidName(fluid, true));
        if (fluid != Fluids.EMPTY) {
            tooltip.add(FluidHelper.getFluidAmount(getAmount(stack), capacity).setStyle(style));
        }
    }
}

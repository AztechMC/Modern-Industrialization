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
import java.util.List;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for fluid items that can only contain FluidFuels
 */
public interface FluidFuelItemHelper {
    static FluidVariant getFluid(ItemStack stack) {
        return FluidVariant.of(new ItemStorage(stack, 0).getFluid());
    }

    // TODO NEO
//    static void setFluid(ItemStack stack, FluidVariant fluid) {
//        if (!fluid.isBlank()) {
//            NbtHelper.putFluid(stack.getOrCreateTag(), "fluid", fluid);
//        } else {
//            stack.removeTagKey("fluid");
//        }
//    }

    static int getAmount(ItemStack stack) {
        return new ItemStorage(stack, 0).getFluid().getAmount();
    }

    // TODO NEO
//    static void setAmount(ItemStack stack, long amount) {
//        if (amount != 0) {
//            stack.getOrCreateTag().putLong("amt", amount);
//        } else {
//            stack.removeTagKey("amt");
//            stack.removeTagKey("fluid");
//        }
//    }

    static void decrement(ItemStack stack) {
        new ItemStorage(stack, 0).drain(1, IFluidHandler.FluidAction.EXECUTE);
    }

    class ItemStorage extends FluidHandlerItemStack {
        public ItemStorage(ItemStack container, int capacity) {
            super(container, capacity);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return FluidFuelRegistry.getEu(fluid.getFluid()) > 0;
        }

        @Override
        protected void setFluid(FluidStack fluid) {
            if (fluid.isEmpty()) {
                this.setContainerToEmpty();
            } else {
                super.setFluid(fluid);
            }
        }
    }

    static void appendTooltip(ItemStack stack, List<Component> tooltip, long capacity) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
        FluidVariant fluid = getFluid(stack);
        tooltip.add(FluidHelper.getFluidName(fluid, true));
        if (!fluid.isBlank()) {
            tooltip.add(FluidHelper.getFluidAmount(getAmount(stack), capacity).setStyle(style));
        }
    }
}

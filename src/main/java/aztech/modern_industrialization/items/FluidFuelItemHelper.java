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
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

/**
 * Helper class for fluid items that can only contain FluidFuels
 */
public interface FluidFuelItemHelper {
    static FluidVariant getFluid(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        return tag == null ? FluidVariant.blank() : NbtHelper.getFluidCompatible(tag, "fluid");
    }

    static void setFluid(ItemStack stack, FluidVariant fluid) {
        if (!fluid.isBlank()) {
            NbtHelper.putFluid(stack.getOrCreateNbt(), "fluid", fluid);
        } else {
            stack.removeSubNbt("fluid");
        }
    }

    static long getAmount(ItemStack stack) {
        if (getFluid(stack).isBlank()) {
            return 0;
        }
        NbtCompound tag = stack.getNbt();
        if (tag != null) {
            return tag.getLong("amt");
        } else {
            return 0;
        }
    }

    static void setAmount(ItemStack stack, long amount) {
        if (amount != 0) {
            stack.getOrCreateNbt().putLong("amt", amount);
        } else {
            stack.removeSubNbt("amt");
            stack.removeSubNbt("fluid");
        }
    }

    static void decrement(ItemStack stack) {
        long amount = getAmount(stack);
        if (amount > 0) {
            setAmount(stack, Math.max(0, amount - 81));
        }
    }

    class ItemStorage extends SingleVariantItemStorage<FluidVariant> {
        private final long capacity;

        public ItemStorage(long capacity, ContainerItemContext ctx) {
            super(ctx);
            this.capacity = capacity;
        }

        @Override
        protected FluidVariant getBlankResource() {
            return FluidVariant.blank();
        }

        @Override
        protected FluidVariant getResource(ItemVariant currentVariant) {
            return getFluid(currentVariant.toStack());
        }

        @Override
        protected long getAmount(ItemVariant currentVariant) {
            return FluidFuelItemHelper.getAmount(currentVariant.toStack());
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return capacity;
        }

        @Override
        protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, FluidVariant newResource, long newAmount) {
            ItemStack stack = currentVariant.toStack();
            setFluid(stack, newResource);
            setAmount(stack, newAmount);
            return ItemVariant.of(stack);
        }

        @Override
        protected boolean canInsert(FluidVariant resource) {
            return FluidFuelRegistry.getEu(resource.getFluid()) > 0;
        }
    }

    static void appendTooltip(ItemStack stack, List<Text> tooltip, long capacity) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
        FluidVariant fluid = getFluid(stack);
        tooltip.add(FluidHelper.getFluidName(fluid, true));
        if (!fluid.isBlank()) {
            tooltip.add(FluidHelper.getFluidAmount(getAmount(stack), capacity).setStyle(style));
        }
    }
}

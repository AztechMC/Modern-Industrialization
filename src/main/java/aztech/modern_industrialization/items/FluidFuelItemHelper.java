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

import alexiil.mc.lib.attributes.ItemAttributeList;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.misc.Reference;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import java.math.RoundingMode;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;

/**
 * Helper class for fluid items that can only contain FluidFuels
 */
public interface FluidFuelItemHelper {
    static FluidKey getFluid(ItemStack stack) {
        CompoundTag fluidTag = stack.getSubTag("fluid");
        return fluidTag == null ? FluidKeys.EMPTY : FluidKey.fromTag(fluidTag);
    }

    static void setFluid(ItemStack stack, FluidKey fluid) {
        stack.getOrCreateTag().put("fluid", fluid.toTag());
    }

    static int getAmount(ItemStack stack) {
        if (stack.getTag() != null) {
            return stack.getTag().getInt("amount");
        } else {
            return 0;
        }
    }

    static void setAmount(ItemStack stack, int amount) {
        if (amount != 0) {
            stack.getOrCreateTag().putInt("amount", amount);
        } else {
            stack.removeSubTag("amount");
            stack.removeSubTag("fluid");
        }
    }

    static void decrement(ItemStack stack) {
        int amount = getAmount(stack);
        if (amount > 0) {
            setAmount(stack, amount - 1);
        }
    }

    static void offerInsertable(Reference<ItemStack> stack, ItemAttributeList<?> to, int capacity) {
        to.offer((FluidInsertable) (fluidVolume, simulation) -> {
            FluidKey storedFluid = getFluid(stack.get());
            if (storedFluid.isEmpty()) {
                if (FluidFuelRegistry.getEu(fluidVolume.getFluidKey()) != 0) {
                    int inserted = Math.min(capacity - getAmount(stack.get()), fluidVolume.amount().asInt(1000, RoundingMode.FLOOR));
                    ItemStack copy = stack.get().copy();
                    setFluid(copy, fluidVolume.getFluidKey());
                    setAmount(copy, inserted);
                    if (!stack.set(copy, simulation)) {
                        return fluidVolume;
                    }
                    return fluidVolume.getFluidKey().withAmount(fluidVolume.amount().sub(FluidAmount.of(inserted, 1000)));
                }
            } else if (storedFluid.equals(fluidVolume.getFluidKey())) {
                int amount = getAmount(stack.get());
                int inserted = Math.min(capacity - amount, fluidVolume.amount().asInt(1000, RoundingMode.FLOOR));
                ItemStack copy = stack.get().copy();
                setAmount(copy, amount + inserted);
                if (!stack.set(copy, simulation)) {
                    return fluidVolume;
                }
                return fluidVolume.getFluidKey().withAmount(fluidVolume.amount().sub(FluidAmount.of(inserted, 1000)));
            }
            return fluidVolume;
        });
    }

    static void appendTooltip(ItemStack stack, List<Text> tooltip, int capacity) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
        FluidKey fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(getFluid(stack).name);
            String quantity = getAmount(stack) + " / " + capacity;
            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_quantity", quantity).setStyle(style));
        } else {
            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_empty").setStyle(style));
        }
    }
}

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

import alexiil.mc.lib.attributes.AttributeProviderItem;
import alexiil.mc.lib.attributes.ItemAttributeList;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.misc.LimitedConsumer;
import alexiil.mc.lib.attributes.misc.Reference;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface LockableFluidItem extends AttributeProviderItem {

    static FluidKey getFluid(ItemStack stack) {
        CompoundTag fluidTag = stack.getSubTag("fluid");
        return fluidTag == null ? FluidKeys.EMPTY : FluidKey.fromTag(fluidTag);
    }

    static void setFluid(ItemStack stack, FluidKey fluid) {
        if (fluid.isEmpty()) {
            stack.getOrCreateTag().remove("fluid");
        } else {
            stack.getOrCreateTag().put("fluid", fluid.toTag());
        }
    }

    boolean isFluidLockable();

    default void appendFluidTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (isFluidLockable()) {
            FluidKey fluid = getFluid(stack);
            if (!fluid.isEmpty()) {
                Text add_tooltip = new TranslatableText("text.modern_industrialization.fluid_lock", fluid.name);
                tooltip.add(add_tooltip);
            }
        }
    }

    @Override
    default void addAllAttributes(Reference<ItemStack> reference, LimitedConsumer<ItemStack> limitedConsumer, ItemAttributeList<?> to) {
        if (to.attribute == FluidAttributes.INSERTABLE) {
            if (isFluidLockable()) {
                to.offer((FluidInsertable) (fluidVolume, simulation) -> {

                    setFluid(reference.get(), fluidVolume.getFluidKey());
                    return fluidVolume;
                });
            } else {
                to.offer((FluidInsertable) (fluidVolume, simulation) -> fluidVolume);
            }
        }
    }
}

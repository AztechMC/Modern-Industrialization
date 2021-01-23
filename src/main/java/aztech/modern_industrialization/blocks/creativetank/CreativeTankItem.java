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
package aztech.modern_industrialization.blocks.creativetank;

import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.List;

import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.fabricmc.fabric.api.transfer.v1.base.FixedDenominatorStorageView;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageFunction;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class CreativeTankItem extends BlockItem {
    public CreativeTankItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static boolean isEmpty(ItemStack stack) {
        return stack.getSubTag("BlockEntityTag") == null;
    }

    public static Fluid getFluid(ItemStack stack) {
        return NbtHelper.getFluidCompatible(stack.getSubTag("BlockEntityTag"), "fluid");
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(FluidHelper.getFluidName(getFluid(stack), true));
    }

    public static class TankItemStorage implements Storage<Fluid>, FixedDenominatorStorageView<Fluid> {
        private final Fluid fluid;

        public TankItemStorage(ItemKey key, ContainerItemContext ignored) {
            ItemStack stack = key.toStack();
            this.fluid = CreativeTankItem.getFluid(stack);
        }

        @Override
        public StorageFunction<Fluid> extractionFunction() {
            return StorageFunction.identity();
        }

        @Override
        public Fluid resource() {
            return fluid;
        }

        @Override
        public boolean forEach(Visitor<Fluid> visitor) {
            if (fluid != Fluids.EMPTY) {
                return visitor.visit(this);
            }
            return false;
        }

        @Override
        public long denominator() {
            return 81000;
        }

        @Override
        public long amountFixedDenominator() {
            return Integer.MAX_VALUE;
        }
    }
}

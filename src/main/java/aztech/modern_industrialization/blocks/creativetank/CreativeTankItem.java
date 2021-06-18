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
import dev.technici4n.fasttransferlib.experimental.api.context.ContainerItemContext;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
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

    public static FluidKey getFluid(ItemStack stack) {
        return NbtHelper.getFluidCompatible(stack.getSubTag("BlockEntityTag"), "fluid");
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(FluidHelper.getFluidName(getFluid(stack), true));
    }

    public static class TankItemStorage implements ExtractionOnlyStorage<FluidKey>, StorageView<FluidKey> {
        private final FluidKey fluid;

        public TankItemStorage(ItemStack stack, ContainerItemContext ignored) {
            this.fluid = CreativeTankItem.getFluid(stack);
        }

        @Override
        public long extract(FluidKey fluid, long maxAmount, Transaction transaction) {
            FluidPreconditions.notEmptyNotNegative(fluid, maxAmount);
            return maxAmount;
        }

        @Override
        public FluidKey resource() {
            return fluid;
        }

        @Override
        public boolean isEmpty() {
            return resource().isEmpty();
        }

        @Override
        public long capacity() {
            return Integer.MAX_VALUE / 100;
        }

        @Override
        public long amount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Iterator<StorageView<FluidKey>> iterator(Transaction transaction) {
            return SingleViewIterator.create(this, transaction);
        }
    }
}

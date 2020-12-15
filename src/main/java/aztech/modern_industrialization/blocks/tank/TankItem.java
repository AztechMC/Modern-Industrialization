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
package aztech.modern_industrialization.blocks.tank;

import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import dev.technici4n.fasttransferlib.api.ContainerItemContext;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.fluid.FluidApi;
import dev.technici4n.fasttransferlib.api.fluid.FluidIo;
import dev.technici4n.fasttransferlib.api.item.ItemKey;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TankItem extends BlockItem {
    public final long capacity;

    public TankItem(Block block, Settings settings, long capacity) {
        super(block, settings);
        this.capacity = capacity;
    }

    public void registerItemApi() {
        FluidApi.ITEM.register(Io::new, this);
    }

    public boolean isEmpty(ItemStack stack) {
        return stack.getSubTag("BlockEntityTag") == null;
    }

    public Fluid getFluid(ItemStack stack) {
        return NbtHelper.getFluidCompatible(stack.getSubTag("BlockEntityTag"), "fluid");
    }

    private void setFluid(ItemStack stack, Fluid fluid) {
        NbtHelper.putFluid(stack.getOrCreateSubTag("BlockEntityTag"), "fluid", fluid);
    }

    public long getAmount(ItemStack stack) {
        if (getFluid(stack) == Fluids.EMPTY) {
            return 0;
        }
        CompoundTag tag = stack.getSubTag("BlockEntityTag");
        if (tag == null)
            return 0;
        else if (tag.contains("amount"))
            return tag.getInt("amount") * 81;
        else
            return tag.getLong("amt");
    }

    private void setAmount(ItemStack stack, long amount) {
        stack.getOrCreateSubTag("BlockEntityTag").putLong("amt", amount);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
        if (!isEmpty(stack)) {
            tooltip.add(FluidHelper.getFluidName(getFluid(stack), true));
            tooltip.add(FluidHelper.getFluidAmount(getAmount(stack), capacity));
        } else {
            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_empty").setStyle(style));
        }
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state) {
        ((TankBlockEntity) world.getBlockEntity(pos)).setCapacity(capacity);
        return super.postPlacement(pos, world, player, stack, state);
    }

    class Io implements FluidIo {
        private final Fluid fluid;
        private final long amount;
        private final ContainerItemContext ctx;

        Io(ItemKey key, ContainerItemContext ctx) {
            ItemStack stack = key.toStack();
            this.fluid = TankItem.this.getFluid(stack);
            this.amount = getAmount(stack);
            this.ctx = ctx;
        }

        @Override
        public int getFluidSlotCount() {
            return 1;
        }

        @Override
        public Fluid getFluid(int i) {
            checkSingleSlot(i);
            return fluid;
        }

        @Override
        public long getFluidAmount(int i) {
            checkSingleSlot(i);
            return amount;
        }

        @Override
        public boolean supportsFluidInsertion() {
            return true;
        }

        @Override
        public long insert(Fluid fluid, long amount, Simulation simulation) {
            if (ctx.getCount() == 0)
                return amount;
            if (fluid == Fluids.EMPTY)
                return amount;

            long inserted = 0;
            if (this.fluid == Fluids.EMPTY) {
                inserted = Math.min(capacity, amount);
            } else if (this.fluid == fluid) {
                inserted = Math.min(capacity - this.amount, amount);
            }
            if (inserted > 0) {
                if (!updateTank(fluid, this.amount + inserted, simulation)) {
                    return amount;
                }
            }
            return amount - inserted;
        }

        @Override
        public boolean supportsFluidExtraction() {
            return true;
        }

        @Override
        public long extract(int slot, Fluid fluid, long maxAmount, Simulation simulation) {
            checkSingleSlot(slot);
            if (ctx.getCount() == 0)
                return 0;
            if (fluid == Fluids.EMPTY)
                return 0;

            long extracted = 0;
            if (this.fluid == fluid) {
                extracted = Math.min(maxAmount, amount);
            }
            if (extracted > 0) {
                if (!updateTank(fluid, amount - extracted, simulation)) {
                    return 0;
                }
            }
            return extracted;
        }

        private boolean updateTank(Fluid fluid, long amount, Simulation simulation) {
            ItemStack result = new ItemStack(TankItem.this);
            if (amount > 0) {
                setFluid(result, fluid);
                setAmount(result, amount);
            }
            ItemKey into = ItemKey.of(result);

            return ctx.transform(1, into, simulation);
        }
    }

    private static void checkSingleSlot(int slot) {
        if (slot != 0) {
            throw new IndexOutOfBoundsException("This tank only has 1 slot, this slot is out of bounds: " + slot);
        }
    }
}

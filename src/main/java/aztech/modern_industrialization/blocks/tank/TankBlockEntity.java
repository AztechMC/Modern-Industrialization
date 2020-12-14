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

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import dev.technici4n.fasttransferlib.api.ContainerItemContext;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.fluid.FluidApi;
import dev.technici4n.fasttransferlib.api.fluid.FluidIo;
import dev.technici4n.fasttransferlib.api.fluid.FluidMovement;
import dev.technici4n.fasttransferlib.api.item.ItemKey;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;

public class TankBlockEntity extends FastBlockEntity implements FluidIo, BlockEntityClientSerializable {
    Fluid fluid = Fluids.EMPTY;
    long amount;
    long capacity;

    public TankBlockEntity() {
        super(MITanks.BLOCK_ENTITY_TYPE);
    }

    public boolean isEmpty() {
        return amount == 0;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
        if (tag.contains("amount")) {
            amount = tag.getInt("amount") * 81;
            capacity = tag.getInt("capacity") * 81;
        } else {
            amount = tag.getLong("amt");
            capacity = tag.getLong("cap");
        }
        if (fluid == Fluids.EMPTY) {
            amount = 0;
        }
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        NbtHelper.putFluid(tag, "fluid", fluid);
        tag.putLong("amt", amount);
        tag.putLong("cap", capacity);
        return tag;
    }

    public void onChanged() {
        markDirty();
        if (!world.isClient)
            sync();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        toClientTag(tag);
        return super.toTag(tag);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        fromClientTag(tag);
        super.fromTag(state, tag);
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public boolean onPlayerUse(PlayerEntity player) {
        FluidIo handIo = FluidApi.ITEM.get(ItemKey.of(player.getMainHandStack()), ContainerItemContext.ofPlayerHand(player, Hand.MAIN_HAND));
        if (handIo != null) {
            // move from hand into this tank
            if (FluidMovement.moveMultiple(handIo, this, Long.MAX_VALUE) > 0)
                return true;
            // move from this tank into hand
            if (FluidMovement.moveMultiple(this, handIo, Long.MAX_VALUE) > 0)
                return true;
        }
        return false;
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
        if (this.fluid != Fluids.EMPTY && this.fluid != fluid) {
            return amount;
        } else {
            long inserted = Math.min(amount, capacity - this.amount);
            if (simulation.isActing()) {
                this.amount += inserted;
                this.fluid = fluid;
                onChanged();
            }
            return amount - inserted;
        }
    }

    @Override
    public boolean supportsFluidExtraction() {
        return true;
    }

    @Override
    public long extract(int slot, Fluid fluid, long maxAmount, Simulation simulation) {
        if (fluid != this.fluid) {
            return 0;
        } else {
            long extracted = Math.min(maxAmount, amount);
            if (simulation.isActing()) {
                amount -= extracted;
                if (amount == 0) {
                    this.fluid = Fluids.EMPTY;
                }
                onChanged();
            }
            return extracted;
        }
    }
}

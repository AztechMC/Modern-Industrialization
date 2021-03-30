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

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.transferapi.api.context.ContainerItemContext;
import aztech.modern_industrialization.transferapi.api.fluid.ItemFluidApi;
import aztech.modern_industrialization.util.NbtHelper;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;

public class CreativeTankBlockEntity extends FastBlockEntity
        implements ExtractionOnlyStorage<Fluid>, StorageView<Fluid>, BlockEntityClientSerializable {
    Fluid fluid = Fluids.EMPTY;

    public CreativeTankBlockEntity() {
        super(MITanks.CREATIVE_BLOCK_ENTITY_TYPE);
    }

    public boolean isEmpty() {
        return fluid == Fluids.EMPTY;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        NbtHelper.putFluid(tag, "fluid", fluid);
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

    public boolean onPlayerUse(PlayerEntity player) {
        Storage<Fluid> handIo = ItemFluidApi.ITEM.find(player.getMainHandStack(), ContainerItemContext.ofPlayerHand(player, Hand.MAIN_HAND));
        if (handIo != null) {
            if (isEmpty()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    handIo.forEach(view -> {
                        fluid = view.resource();
                        onChanged();
                        return true;
                    }, transaction);
                }
                return !isEmpty();
            } else {
                try (Transaction tx = Transaction.openOuter()) {
                    return handIo.insert(fluid, Integer.MAX_VALUE, tx) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public long extract(Fluid fluid, long maxAmount, Transaction transaction) {
        FluidPreconditions.notEmptyNotNegative(fluid, maxAmount);
        return maxAmount;
    }

    @Override
    public Fluid resource() {
        return fluid;
    }

    @Override
    public boolean forEach(Visitor<Fluid> visitor, Transaction transaction) {
        if (fluid != Fluids.EMPTY) {
            return visitor.accept(this);
        }
        return false;
    }

    @Override
    public long amount() {
        return Integer.MAX_VALUE;
    }
}

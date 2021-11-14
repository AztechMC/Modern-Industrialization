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
package aztech.modern_industrialization.blocks.storage.tank;

import aztech.modern_industrialization.blocks.storage.AbstractStorageBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class TankBlockEntity extends AbstractStorageBlockEntity<FluidVariant> {

    final long capacity;

    public TankBlockEntity(BlockEntityType<?> bet, BlockPos pos, BlockState state, long capacity) {
        super(bet, pos, state);
        this.capacity = capacity;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        resource = NbtHelper.getFluidCompatible(tag, "fluid");
        amount = tag.getLong("amt");
        if (resource.isBlank()) {
            amount = 0;
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        NbtHelper.putFluid(tag, "fluid", getResource());
        tag.putLong("amt", amount);
    }

    public boolean onPlayerUse(PlayerEntity player) {
        Storage<FluidVariant> handIo = ContainerItemContext.ofPlayerHand(player, Hand.MAIN_HAND).find(FluidStorage.ITEM);
        if (handIo != null) {
            // move from hand into this tank
            if (StorageUtil.move(handIo, this, f -> true, Long.MAX_VALUE, null) > 0)
                return true;
            // move from this tank into hand
            if (StorageUtil.move(this, handIo, f -> true, Long.MAX_VALUE, null) > 0)
                return true;
        }
        return false;
    }

    @Override
    public FluidVariant getBlankResource() {
        return FluidVariant.blank();
    }

    @Override
    public long getCapacityForResource(FluidVariant resource) {
        return capacity;
    }
}

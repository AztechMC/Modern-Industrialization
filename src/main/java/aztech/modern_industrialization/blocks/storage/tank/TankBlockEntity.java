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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TankBlockEntity extends AbstractStorageBlockEntity<FluidVariant> {

    final long capacity;

    public TankBlockEntity(BlockEntityType<?> bet, BlockPos pos, BlockState state, long capacity) {
        super(bet, pos, state);
        this.capacity = capacity;
    }

    @Override
    public void load(CompoundTag tag) {
        resource = NbtHelper.getFluidCompatible(tag, "fluid");
        amount = tag.getLong("amt");
        if (resource.isBlank()) {
            amount = 0;
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        NbtHelper.putFluid(tag, "fluid", getResource());
        tag.putLong("amt", amount);
    }

    public boolean onPlayerUse(Player player) {
        Storage<FluidVariant> handIo = ContainerItemContext.ofPlayerHand(player, InteractionHand.MAIN_HAND).find(FluidStorage.ITEM);
        if (handIo != null) {
            // move from hand into this tank
            if (StorageUtil.move(handIo, this, f -> true, Long.MAX_VALUE, null) > 0) {
                player.playNotifySound(FluidVariantAttributes.getEmptySound(getResource()), SoundSource.BLOCKS, 1, 1);
                return true;
            }
            // move from this tank into hand
            FluidVariant oldFluid = getResource(); // get current fluid to play the sound later
            if (StorageUtil.move(this, handIo, f -> true, Long.MAX_VALUE, null) > 0) {
                player.playNotifySound(FluidVariantAttributes.getFillSound(oldFluid), SoundSource.BLOCKS, 1, 1);
                return true;
            }
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

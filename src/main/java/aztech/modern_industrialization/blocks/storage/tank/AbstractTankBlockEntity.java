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
import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import aztech.modern_industrialization.util.NbtHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractTankBlockEntity extends AbstractStorageBlockEntity<FluidVariant> {

    public AbstractTankBlockEntity(BlockEntityType<?> bet,
            BlockPos pos,
            BlockState state,
            StorageBehaviour<FluidVariant> behaviour) {
        super(bet, pos, state, behaviour);
    }

    @Override
    public FluidVariant loadResource(CompoundTag tag) {
        return NbtHelper.getFluidCompatible(tag, "fluid");
    }

    @Override
    public void saveResource(FluidVariant resource, CompoundTag tag) {
        NbtHelper.putFluid(tag, "fluid", getResource());
    }

    @Override
    public FluidVariant getBlankResource() {
        return FluidVariant.blank();
    }

    public abstract boolean onPlayerUse(Player player);

}

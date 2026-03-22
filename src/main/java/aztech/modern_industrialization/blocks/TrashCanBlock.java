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

package aztech.modern_industrialization.blocks;

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.VoidingResourceHandler;
import net.neoforged.neoforge.transfer.energy.VoidingEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class TrashCanBlock extends Block {
    public TrashCanBlock(Properties properties) {
        super(properties);
    }

    // We have no BE, so we must invalidate caps on placement and removal
    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        if (!pOldState.is(this)) {
            pLevel.invalidateCapabilities(pPos);
        }
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        level.invalidateCapabilities(pos);
    }

    private static final ResourceHandler<FluidResource> FLUID_TRASH = new VoidingResourceHandler<>(FluidResource.EMPTY);
    private static final ResourceHandler<ItemResource> ITEM_TRASH = new VoidingResourceHandler<>(ItemResource.EMPTY);

    private static class TrashEnergyStorage extends VoidingEnergyHandler implements MIEnergyStorage {
        private static final TrashEnergyStorage INSTANCE = new TrashEnergyStorage();

        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }
    }

    public static void onRegister(Block block, Item blockItem) {
        MICapabilities.onEvent(event -> {
            event.registerBlock(Capabilities.Item.BLOCK, (level, pos, state, be, direction) -> ITEM_TRASH, block);
            event.registerItem(Capabilities.Item.ITEM, (stack, itemAccess) -> ITEM_TRASH, blockItem);
            event.registerBlock(Capabilities.Fluid.BLOCK, (level, pos, state, be, direction) -> FLUID_TRASH, block);
            event.registerItem(Capabilities.Fluid.ITEM, (stack, itemAccess) -> FLUID_TRASH, blockItem);
            event.registerBlock(EnergyApi.SIDED, (level, pos, state, be, direction) -> TrashEnergyStorage.INSTANCE, block);
        });
    }
}

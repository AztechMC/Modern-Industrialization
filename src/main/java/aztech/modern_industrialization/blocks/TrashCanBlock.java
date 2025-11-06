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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;

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
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!pState.is(pNewState.getBlock())) {
            pLevel.invalidateCapabilities(pPos);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    private static class TrashItemHandler implements IItemHandler {
        private static final TrashItemHandler INSTANCE = new TrashItemHandler();

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }

    private static class TrashFluidHandler implements IFluidHandler {
        private static final TrashFluidHandler INSTANCE = new TrashFluidHandler();

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return resource.getAmount();
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    private static class TrashFluidHandlerItem extends TrashFluidHandler implements IFluidHandlerItem {
        private final ItemStack container;

        private TrashFluidHandlerItem(ItemStack container) {
            this.container = container;
        }

        @Override
        public ItemStack getContainer() {
            return container;
        }
    }

    private static class TrashEnergyStorage implements MIEnergyStorage {
        private static final TrashEnergyStorage INSTANCE = new TrashEnergyStorage();

        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public long receive(long maxReceive, boolean simulate) {
            return maxReceive;
        }

        @Override
        public long extract(long maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public long getAmount() {
            return 0;
        }

        @Override
        public long getCapacity() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    public static void onRegister(Block block, Item blockItem) {
        MICapabilities.onEvent(event -> {
            event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, be, direction) -> TrashItemHandler.INSTANCE, block);
            event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx) -> TrashItemHandler.INSTANCE, blockItem);
            event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, pos, state, be, direction) -> TrashFluidHandler.INSTANCE, block);
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new TrashFluidHandlerItem(stack), blockItem);
            event.registerBlock(EnergyApi.SIDED, (level, pos, state, be, direction) -> TrashEnergyStorage.INSTANCE, block);
        });
    }
}

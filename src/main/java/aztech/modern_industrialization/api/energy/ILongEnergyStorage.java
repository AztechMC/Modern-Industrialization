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
package aztech.modern_industrialization.api.energy;

import aztech.modern_industrialization.MI;
import com.google.common.primitives.Ints;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface ILongEnergyStorage extends IEnergyStorage {
    BlockCapability<ILongEnergyStorage, @Nullable Direction> BLOCK = BlockCapability.createSided(MI.id("long_energy_storage"),
            ILongEnergyStorage.class);
    ItemCapability<ILongEnergyStorage, Void> ITEM = ItemCapability.createVoid(MI.id("long_energy_storage"), ILongEnergyStorage.class);

    long receive(long maxReceive, boolean simulate);

    long extract(long maxExtract, boolean simulate);

    long getAmount();

    long getCapacity();

    // Default implementations below, do not override!

    @Override
    @ApiStatus.NonExtendable
    default int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) receive(maxReceive, simulate);
    }

    @Override
    @ApiStatus.NonExtendable
    default int extractEnergy(int maxExtract, boolean simulate) {
        return (int) extract(maxExtract, simulate);
    }

    @Override
    @ApiStatus.NonExtendable
    default int getEnergyStored() {
        return Ints.saturatedCast(getAmount());
    }

    @Override
    @ApiStatus.NonExtendable
    default int getMaxEnergyStored() {
        return Ints.saturatedCast(getCapacity());
    }

    // Internal implementation below

    @ApiStatus.Internal
    static void init(RegisterCapabilitiesEvent event, Block[] allBlocks, Item[] allItems) {
        ThreadLocal<Boolean> inCompat = ThreadLocal.withInitial(() -> false);

        event.registerBlock(BLOCK, (level, pos, state, be, direction) -> {
            if (inCompat.get()) {
                return null;
            }

            inCompat.set(Boolean.TRUE);
            try {
                var nonLongHandler = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, state, be, direction);
                return nonLongHandler == null ? null : new NonLongWrapper(nonLongHandler);
            } finally {
                inCompat.set(Boolean.FALSE);
            }
        }, allBlocks);

        event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, be, direction) -> {
            if (inCompat.get()) {
                return null;
            }

            inCompat.set(Boolean.TRUE);
            try {
                return level.getCapability(BLOCK, pos, state, be, direction);
            } finally {
                inCompat.set(Boolean.FALSE);
            }
        }, allBlocks);

        event.registerItem(ITEM, (stack, ignored) -> {
            if (inCompat.get()) {
                return null;
            }

            inCompat.set(Boolean.TRUE);
            try {
                var nonLongHandler = stack.getCapability(Capabilities.EnergyStorage.ITEM);
                return nonLongHandler == null ? null : new NonLongWrapper(nonLongHandler);
            } finally {
                inCompat.set(Boolean.FALSE);
            }
        }, allBlocks);

        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ignored) -> {
            if (inCompat.get()) {
                return null;
            }

            inCompat.set(Boolean.TRUE);
            try {
                return stack.getCapability(ITEM);
            } finally {
                inCompat.set(Boolean.FALSE);
            }
        }, allBlocks);
    }
}

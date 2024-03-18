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

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import dev.technici4n.grandpower.api.DelegatingEnergyStorage;
import dev.technici4n.grandpower.api.ILongEnergyStorage;
import dev.technici4n.grandpower.api.LimitingEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.ApiStatus;

public class EnergyApi {
    public static final BlockCapability<MIEnergyStorage, Direction> SIDED = BlockCapability
            .createSided(new MIIdentifier("sided_mi_energy_storage"), MIEnergyStorage.class);
    public static final ItemCapability<ILongEnergyStorage, Void> ITEM = ItemCapability
            .createVoid(new MIIdentifier("energy_storage"), ILongEnergyStorage.class);

    private static final ThreadLocal<Boolean> IN_COMPAT = ThreadLocal.withInitial(() -> false);

    public static final MIEnergyStorage CREATIVE = new MIEnergyStorage.NoInsert() {
        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            return maxAmount;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public long getAmount() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getCapacity() {
            return Long.MAX_VALUE;
        }
    };

    public static final MIEnergyStorage EMPTY = new EmptyStorage();

    private static class EmptyStorage implements MIEnergyStorage.NoInsert, MIEnergyStorage.NoExtract {
        @Override
        public boolean canConnect(CableTier cableTier) {
            return false;
        }

        @Override
        public long getAmount() {
            return 0;
        }

        @Override
        public long getCapacity() {
            return 0;
        }
    }

    @ApiStatus.Internal
    public static void init(RegisterCapabilitiesEvent event, Block[] allBlocks, Item[] allItems) {
        // Compat wrapper for TR energy
        if (MIConfig.getConfig().enableBidirectionalEnergyCompat) {
            event.registerBlock(ILongEnergyStorage.BLOCK, (world, pos, state, blockEntity, context) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return world.getCapability(SIDED, pos, state, blockEntity, context);
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allBlocks);
            event.registerBlock(SIDED, (world, pos, state, blockEntity, context) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    var trStorage = world.getCapability(ILongEnergyStorage.BLOCK, pos, state, blockEntity, context);
                    return trStorage == null ? null : new WrappedTrStorage(trStorage);
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allBlocks);

            event.registerItem(ILongEnergyStorage.ITEM, (stack, ignored) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return stack.getCapability(ITEM);
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
            event.registerItem(ITEM, (stack, ctx) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return stack.getCapability(ILongEnergyStorage.ITEM);
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
        } else {
            event.registerBlock(SIDED, (world, pos, state, blockEntity, context) -> {
                var trStorage = world.getCapability(ILongEnergyStorage.BLOCK, pos, state, blockEntity, context);
                return trStorage == null || !trStorage.canReceive() ? null : new InsertOnlyTrStorage(trStorage);
            }, allBlocks);
            event.registerItem(ITEM, (stack, ctx) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    var trStorage = stack.getCapability(ILongEnergyStorage.ITEM);
                    return trStorage == null || !trStorage.canReceive() ? null : new LimitingEnergyStorage(trStorage, Long.MAX_VALUE, 0);
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
            event.registerItem(ILongEnergyStorage.ITEM, (stack, ctx) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    var miStorage = stack.getCapability(ITEM);
                    return miStorage == null || !miStorage.canExtract() ? null : new LimitingEnergyStorage(miStorage, 0, Long.MAX_VALUE);
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
        }
    }

    private record InsertOnlyTrStorage(ILongEnergyStorage trStorage) implements MIEnergyStorage.NoExtract {
        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public long receive(long maxAmount, boolean simulate) {
            return trStorage.receive(maxAmount, simulate);
        }

        @Override
        public boolean canReceive() {
            return trStorage.canReceive();
        }

        @Override
        public long getAmount() {
            return trStorage.getAmount();
        }

        @Override
        public long getCapacity() {
            return trStorage.getCapacity();
        }
    }

    private static class WrappedTrStorage extends DelegatingEnergyStorage implements MIEnergyStorage {
        public WrappedTrStorage(ILongEnergyStorage backingStorage) {
            super(backingStorage);
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }
    }
}

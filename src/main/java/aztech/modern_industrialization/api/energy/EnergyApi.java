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
import aztech.modern_industrialization.config.MIServerConfig;
import aztech.modern_industrialization.config.MIStartupConfig;
import dev.technici4n.grandpower.api.ILongEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * MI's energy API. It uses the same types as GrandPower (i.e. {@link ILongEnergyStorage},
 * however the conversion ratio between EU and GrandPower energy is configurable.
 * So don't mix them up!
 */
public class EnergyApi {
    public static final BlockCapability<MIEnergyStorage, Direction> SIDED = BlockCapability
            .createSided(MI.id("sided_mi_energy_storage"), MIEnergyStorage.class);
    public static final ItemCapability<ILongEnergyStorage, Void> ITEM = ItemCapability
            .createVoid(MI.id("energy_storage"), ILongEnergyStorage.class);

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
        if (MIStartupConfig.INSTANCE.bidirectionalEnergyCompat.getAsBoolean()) {
            event.registerBlock(ILongEnergyStorage.BLOCK, (world, pos, state, blockEntity, context) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return WrappedMIStorage.of(world.getCapability(SIDED, pos, state, blockEntity, context));
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
                    return WrappedExternalStorage.of(world.getCapability(ILongEnergyStorage.BLOCK, pos, state, blockEntity, context));
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
                    return WrappedMIStorage.of(stack.getCapability(ITEM));
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
                    return WrappedExternalStorage.of(stack.getCapability(ILongEnergyStorage.ITEM));
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
        } else {
            event.registerBlock(SIDED, (world, pos, state, blockEntity, context) -> {
                return InsertOnlyExternalStorage.of(world.getCapability(ILongEnergyStorage.BLOCK, pos, state, blockEntity, context));
            }, allBlocks);
            event.registerItem(ITEM, (stack, ctx) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return InsertOnlyExternalStorage.of(stack.getCapability(ILongEnergyStorage.ITEM));
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
                    return ExtractOnlyMIStorage.of(stack.getCapability(ITEM));
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
        }
    }

    private static long ratio() {
        return MIServerConfig.INSTANCE.forgeEnergyPerEu.getAsInt();
    }

    /**
     * An MI energy storage that wraps an external storage to apply the energy conversion ratio to it.
     */
    private static class WrappedExternalStorage implements MIEnergyStorage {
        @Nullable
        private static WrappedExternalStorage of(@Nullable ILongEnergyStorage externalStorage) {
            return externalStorage == null ? null : new WrappedExternalStorage(externalStorage);
        }

        private final ILongEnergyStorage externalStorage;

        private WrappedExternalStorage(ILongEnergyStorage externalStorage) {
            this.externalStorage = externalStorage;
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public long receive(long maxReceive, boolean simulate) {
            long ratio = ratio();
            maxReceive = Math.min(maxReceive, Long.MAX_VALUE / ratio); // avoid overflow
            maxReceive *= ratio;
            if (ratio > 1) {
                // Do a simulate insertion to round down to a multiple of ratio that should be accepted.
                maxReceive = externalStorage.receive(maxReceive, true) / ratio * ratio;
            }
            return externalStorage.receive(maxReceive, simulate) / ratio;
        }

        @Override
        public long extract(long maxExtract, boolean simulate) {
            long ratio = ratio();
            maxExtract = Math.min(maxExtract, Long.MAX_VALUE / ratio); // avoid overflow
            maxExtract *= ratio;
            if (ratio > 1) {
                // Do a simulate extraction to round down to a multiple of ratio that should be accepted.
                maxExtract = externalStorage.extract(maxExtract, true) / ratio * ratio;
            }
            return externalStorage.extract(maxExtract, simulate) / ratio;
        }

        @Override
        public long getAmount() {
            return externalStorage.getAmount() / ratio();
        }

        @Override
        public long getCapacity() {
            return externalStorage.getCapacity() / ratio();
        }

        @Override
        public boolean canExtract() {
            return externalStorage.canExtract();
        }

        @Override
        public boolean canReceive() {
            return externalStorage.canReceive();
        }
    }

    private static class InsertOnlyExternalStorage extends WrappedExternalStorage {
        @Nullable
        private static InsertOnlyExternalStorage of(@Nullable ILongEnergyStorage externalStorage) {
            return externalStorage == null || !externalStorage.canReceive() ? null : new InsertOnlyExternalStorage(externalStorage);
        }

        private InsertOnlyExternalStorage(ILongEnergyStorage externalStorage) {
            super(externalStorage);
        }

        @Override
        public long extract(long maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }
    }

    /**
     * An external storage that wraps an MI storage to apply the energy conversion ratio to it.
     */
    private static class WrappedMIStorage implements ILongEnergyStorage {
        @Nullable
        private static WrappedMIStorage of(@Nullable ILongEnergyStorage miStorage) {
            return miStorage == null ? null : new WrappedMIStorage(miStorage);
        }

        private final ILongEnergyStorage miStorage;

        private WrappedMIStorage(ILongEnergyStorage miStorage) {
            this.miStorage = miStorage;
        }

        @Override
        public long receive(long maxReceive, boolean simulate) {
            long ratio = ratio();
            return miStorage.receive(maxReceive / ratio, simulate) * ratio;
        }

        @Override
        public long extract(long maxExtract, boolean simulate) {
            long ratio = ratio();
            return miStorage.extract(maxExtract / ratio, simulate) * ratio;
        }

        @Override
        public long getAmount() {
            return miStorage.getAmount() * ratio();
        }

        @Override
        public long getCapacity() {
            return miStorage.getCapacity() * ratio();
        }

        @Override
        public boolean canExtract() {
            return miStorage.canExtract();
        }

        @Override
        public boolean canReceive() {
            return miStorage.canReceive();
        }
    }

    private static class ExtractOnlyMIStorage extends WrappedMIStorage {
        @Nullable
        private static EnergyApi.ExtractOnlyMIStorage of(@Nullable ILongEnergyStorage miStorage) {
            return miStorage == null || !miStorage.canExtract() ? null : new ExtractOnlyMIStorage(miStorage);
        }

        private ExtractOnlyMIStorage(ILongEnergyStorage miStorage) {
            super(miStorage);
        }

        @Override
        public long receive(long maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}

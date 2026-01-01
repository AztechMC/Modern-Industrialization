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
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * MI's energy API. It uses the same types as NeoForge (i.e. {@link EnergyHandler},
 * however the conversion ratio between EU and FE is configurable.
 * So don't mix them up!
 */
public class EnergyApi {
    // TODO: rename to MIEnergyHandler, reorganize to match NeoForge caps
    public static final BlockCapability<MIEnergyStorage, Direction> SIDED = BlockCapability
            .createSided(MI.id("sided_mi_energy_storage"), MIEnergyStorage.class);
    public static final ItemCapability<EnergyHandler, ItemAccess> ITEM = ItemCapability
            .create(MI.id("energy_storage"), EnergyHandler.class, ItemAccess.class);

    private static final ThreadLocal<Boolean> IN_COMPAT = ThreadLocal.withInitial(() -> false);

    public static final MIEnergyStorage CREATIVE = new MIEnergyStorage.NoInsert() {
        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return amount;
        }

        @Override
        public long getAmountAsLong() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getCapacityAsLong() {
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
        public long getAmountAsLong() {
            return 0;
        }

        @Override
        public long getCapacityAsLong() {
            return 0;
        }
    }

    @ApiStatus.Internal
    public static void init(RegisterCapabilitiesEvent event, Block[] allBlocks, Item[] allItems) {
        // Compat wrapper for TR energy
        if (MIStartupConfig.INSTANCE.bidirectionalEnergyCompat.getAsBoolean()) {
            event.registerBlock(Capabilities.Energy.BLOCK, (world, pos, state, blockEntity, context) -> {
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
                    return WrappedExternalStorage.of(world.getCapability(Capabilities.Energy.BLOCK, pos, state, blockEntity, context));
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allBlocks);

            event.registerItem(Capabilities.Energy.ITEM, (stack, itemAccess) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return WrappedMIStorage.of(stack.getCapability(ITEM, itemAccess));
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
            event.registerItem(ITEM, (stack, itemAccess) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return WrappedExternalStorage.of(stack.getCapability(Capabilities.Energy.ITEM, itemAccess));
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
        } else {
            event.registerBlock(SIDED, (world, pos, state, blockEntity, context) -> {
                return InsertOnlyExternalStorage.of(world.getCapability(Capabilities.Energy.BLOCK, pos, state, blockEntity, context));
            }, allBlocks);
            event.registerItem(ITEM, (stack, itemAccess) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return InsertOnlyExternalStorage.of(stack.getCapability(Capabilities.Energy.ITEM, itemAccess));
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
            event.registerItem(Capabilities.Energy.ITEM, (stack, itemAccess) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return ExtractOnlyMIStorage.of(stack.getCapability(ITEM, itemAccess));
                } finally {
                    IN_COMPAT.set(false);
                }
            }, allItems);
        }
    }

    private static int ratio() {
        return MIServerConfig.INSTANCE.forgeEnergyPerEu.getAsInt();
    }

    /**
     * An MI energy storage that wraps an external storage to apply the energy conversion ratio to it.
     */
    private static class WrappedExternalStorage implements MIEnergyStorage {
        @Nullable
        private static WrappedExternalStorage of(@Nullable EnergyHandler externalStorage) {
            return externalStorage == null ? null : new WrappedExternalStorage(externalStorage);
        }

        private final EnergyHandler externalStorage;

        private WrappedExternalStorage(EnergyHandler externalStorage) {
            this.externalStorage = externalStorage;
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public int insert(int maxInsert, TransactionContext transaction) {
            int ratio = ratio();
            maxInsert = Math.min(maxInsert, Integer.MAX_VALUE / ratio); // avoid overflow
            maxInsert *= ratio;
            if (ratio > 1) {
                // Do a simulate insertion to round down to a multiple of ratio that should be accepted.
                try (var nested = Transaction.open(transaction)) {
                    maxInsert = externalStorage.insert(maxInsert, nested) / ratio * ratio;
                }
            }
            return externalStorage.insert(maxInsert, transaction) / ratio;
        }

        @Override
        public int extract(int maxExtract, TransactionContext transaction) {
            int ratio = ratio();
            maxExtract = Math.min(maxExtract, Integer.MAX_VALUE / ratio); // avoid overflow
            maxExtract *= ratio;
            if (ratio > 1) {
                // Do a simulate extraction to round down to a multiple of ratio that should be accepted.
                try (var nested = Transaction.open(transaction)) {
                    maxExtract = externalStorage.extract(maxExtract, nested) / ratio * ratio;
                }
            }
            return externalStorage.extract(maxExtract, transaction) / ratio;
        }

        @Override
        public long getAmountAsLong() {
            return externalStorage.getAmountAsLong() / ratio();
        }

        @Override
        public long getCapacityAsLong() {
            return externalStorage.getCapacityAsLong() / ratio();
        }
    }

    private static class InsertOnlyExternalStorage extends WrappedExternalStorage {
        @Nullable
        private static InsertOnlyExternalStorage of(@Nullable EnergyHandler externalStorage) {
            return externalStorage == null /*|| !externalStorage.supportsInsertion()*/ ? null : new InsertOnlyExternalStorage(externalStorage);
        }

        private InsertOnlyExternalStorage(EnergyHandler externalStorage) {
            super(externalStorage);
        }

        @Override
        public int extract(int maxExtract, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }
    }

    /**
     * An external storage that wraps an MI storage to apply the energy conversion ratio to it.
     */
    private static class WrappedMIStorage implements EnergyHandler {
        @Nullable
        private static WrappedMIStorage of(@Nullable EnergyHandler miStorage) {
            return miStorage == null ? null : new WrappedMIStorage(miStorage);
        }

        private final EnergyHandler miStorage;

        private WrappedMIStorage(EnergyHandler miStorage) {
            this.miStorage = miStorage;
        }

        @Override
        public int insert(int maxInsert, TransactionContext transaction) {
            int ratio = ratio();
            return miStorage.insert(maxInsert / ratio, transaction) * ratio;
        }

        @Override
        public int extract(int maxExtract, TransactionContext transaction) {
            int ratio = ratio();
            return miStorage.extract(maxExtract / ratio, transaction) * ratio;
        }

        @Override
        public long getAmountAsLong() {
            return miStorage.getAmountAsLong() * ratio();
        }

        @Override
        public long getCapacityAsLong() {
            return miStorage.getCapacityAsLong() * ratio();
        }

//        @Override
//        public boolean supportsExtraction() {
//            return miStorage.supportsExtraction();
//        }
//
//        @Override
//        public boolean supportsInsertion() {
//            return miStorage.supportsInsertion();
//        }
    }

    private static class ExtractOnlyMIStorage extends WrappedMIStorage {
        private static EnergyApi.@Nullable ExtractOnlyMIStorage of(@Nullable EnergyHandler miStorage) {
            return miStorage == null /*|| !miStorage.supportsExtraction()*/ ? null : new ExtractOnlyMIStorage(miStorage);
        }

        private ExtractOnlyMIStorage(EnergyHandler miStorage) {
            super(miStorage);
        }

        @Override
        public int insert(int maxInsert, TransactionContext transaction) {
            return 0;
        }

//        @Override
//        public boolean supportsInsertion() {
//            return false;
//        }
    }
}

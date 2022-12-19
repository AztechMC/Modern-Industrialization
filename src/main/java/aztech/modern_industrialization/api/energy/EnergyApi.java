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
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.DelegatingEnergyStorage;

public class EnergyApi {
    public static final BlockApiLookup<MIEnergyStorage, Direction> SIDED = BlockApiLookup
            .get(new ResourceLocation("modern_industrialization:sided_mi_energy_storage"), MIEnergyStorage.class, Direction.class);

    private static final ThreadLocal<Boolean> IN_COMPAT = ThreadLocal.withInitial(() -> false);

    public static final MIEnergyStorage CREATIVE = new MIEnergyStorage.NoInsert() {
        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return maxAmount;
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

    static {
        // Compat wrapper for TR energy
        if (MIConfig.getConfig().enableBidirectionalEnergyCompat) {
            EnergyStorage.SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    return SIDED.find(world, pos, state, blockEntity, context);
                } finally {
                    IN_COMPAT.set(false);
                }
            });

            SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
                if (IN_COMPAT.get()) {
                    return null;
                }

                IN_COMPAT.set(true);
                try {
                    EnergyStorage trStorage = EnergyStorage.SIDED.find(world, pos, state, blockEntity, context);
                    return trStorage == null ? null : new WrappedTrStorage(trStorage);
                } finally {
                    IN_COMPAT.set(false);
                }
            });
        } else {
            SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
                EnergyStorage trStorage = EnergyStorage.SIDED.find(world, pos, state, blockEntity, context);
                return trStorage == null || !trStorage.supportsInsertion() ? null : new InsertOnlyTrStorage(trStorage);
            });
        }
    }

    private record InsertOnlyTrStorage(EnergyStorage trStorage) implements MIEnergyStorage.NoExtract {
        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            return trStorage.insert(maxAmount, transaction);
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
        public WrappedTrStorage(EnergyStorage backingStorage) {
            super(backingStorage, null);
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }
    }
}

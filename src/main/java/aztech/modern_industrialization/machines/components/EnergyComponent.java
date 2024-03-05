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
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.api.machine.component.EnergyAccess;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnergyComponent implements IComponent.ServerOnly, EnergyAccess {
    private long storedEu;
    private final Supplier<Long> capacity;
    private final BlockEntity blockEntity; // used to call setChanged()

    public EnergyComponent(BlockEntity blockEntity, Supplier<Long> capacity) {
        this.capacity = capacity;
        this.blockEntity = blockEntity;
    }

    public EnergyComponent(BlockEntity blockEntity, long capacity) {
        this.capacity = () -> capacity;
        this.blockEntity = blockEntity;
    }

    @Override
    public long getEu() {
        return Math.min(storedEu, capacity.get());
    }

    @Override
    public long getCapacity() {
        return capacity.get();
    }

    public long getRemainingCapacity() {
        return capacity.get() - getEu();
    }

    public void writeNbt(CompoundTag tag) {
        tag.putLong("storedEu", getEu());
    }

    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        setEu(tag.getLong("storedEu"), false);
    }

    private void setEu(long eu, boolean update) {
        this.storedEu = Math.min(eu, capacity.get());

        if (update) {
            blockEntity.setChanged();
        }
    }

    public long consumeEu(long max, Simulation simulation) {
        Preconditions.checkArgument(max >= 0, "May not consume < 0 energy.");
        long ext = Math.min(max, getEu());
        if (simulation.isActing()) {
            setEu(getEu() - ext, true);
        }
        return ext;
    }

    public long insertEu(long max, Simulation simulation) {
        Preconditions.checkArgument(max >= 0, "May not insert < 0 energy.");
        long ext = Math.min(max, capacity.get() - getEu());
        if (simulation.isActing()) {
            setEu(getEu() + ext, true);
        }
        return ext;
    }

    private abstract class EnergyStorage implements MIEnergyStorage {
        @Override
        public long getAmount() {
            return storedEu;
        }

        @Override
        public long getCapacity() {
            return capacity.get();
        }
    }

    public MIEnergyStorage buildInsertable(Predicate<CableTier> canInsert) {
        return new EnergyStorage() {
            @Override
            public long receive(long maxReceive, boolean simulate) {
                return insertEu(maxReceive, simulate ? Simulation.SIMULATE : Simulation.ACT);
            }

            @Override
            public boolean canReceive() {
                return true;
            }

            @Override
            public long extract(long maxExtract, boolean simulate) {
                return 0;
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canConnect(CableTier cableTier) {
                return canInsert.test(cableTier);
            }
        };
    }

    public MIEnergyStorage buildExtractable(Predicate<CableTier> canExtract) {
        return new EnergyStorage() {
            @Override
            public long receive(long maxReceive, boolean simulate) {
                return 0;
            }

            @Override
            public boolean canReceive() {
                return false;
            }

            @Override
            public long extract(long maxExtract, boolean simulate) {
                return consumeEu(maxExtract, simulate ? Simulation.SIMULATE : Simulation.ACT);
            }

            @Override
            public boolean canExtract() {
                return true;
            }

            @Override
            public boolean canConnect(CableTier cableTier) {
                return canExtract.test(cableTier);
            }
        };
    }
}

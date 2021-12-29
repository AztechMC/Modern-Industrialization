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
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;

public class EnergyComponent implements IComponent.ServerOnly {
    private long storedEu;
    private final Supplier<Long> capacity;

    public EnergyComponent(Supplier<Long> capacity) {
        this.capacity = capacity;
    }

    public EnergyComponent(long capacity) {
        this.capacity = () -> capacity;
    }

    public long getEu() {
        return Math.min(storedEu, capacity.get());
    }

    public long getCapacity() {
        return capacity.get();
    }

    public long getRemainingCapacity() {
        return capacity.get() - getEu();
    }

    public void writeNbt(CompoundTag tag) {
        tag.putLong("storedEu", getEu());
    }

    public void readNbt(CompoundTag tag) {
        setEu(tag.getLong("storedEu"));
    }

    private void setEu(long eu) {
        this.storedEu = Math.min(eu, capacity.get());
    }

    public long consumeEu(long max, Simulation simulation) {
        Preconditions.checkArgument(max >= 0, "May not consume < 0 energy.");
        long ext = Math.min(max, getEu());
        if (simulation.isActing()) {
            setEu(getEu() - ext);
        }
        return ext;
    }

    public long insertEu(long max, Simulation simulation) {
        Preconditions.checkArgument(max >= 0, "May not insert < 0 energy.");
        long ext = Math.min(max, capacity.get() - getEu());
        if (simulation.isActing()) {
            setEu(getEu() + ext);
        }
        return ext;
    }

    public void insertEnergy(EnergyInsertable insertable) {
        setEu(getEu() - insertable.insertEnergy(getEu(), Simulation.ACT));
    }

    public EnergyInsertable buildInsertable(Predicate<CableTier> canInsert) {
        return new EnergyInsertable() {
            @Override
            public long insertEnergy(long amount, Simulation simulation) {
                Preconditions.checkArgument(amount >= 0, "May not insert < 0 energy.");
                long inserted = Math.min(amount, capacity.get() - getEu());
                if (simulation.isActing()) {
                    setEu(getEu() + inserted);
                    // TODO: markDirty?
                }
                return inserted;
            }

            @Override
            public boolean canInsert(CableTier tier) {
                return canInsert.test(tier);
            }
        };
    }

    public EnergyExtractable buildExtractable(Predicate<CableTier> canExtract) {
        return new EnergyExtractable() {
            @Override
            public long extractEnergy(long amount, Simulation simulation) {
                Preconditions.checkArgument(amount >= 0, "May not extract < 0 energy.");
                long extracted = Math.min(amount, getEu());
                if (simulation.isActing()) {
                    setEu(getEu() - extracted);
                }
                return extracted;
            }

            @Override
            public boolean canExtract(CableTier tier) {
                return canExtract.test(tier);
            }
        };
    }

}

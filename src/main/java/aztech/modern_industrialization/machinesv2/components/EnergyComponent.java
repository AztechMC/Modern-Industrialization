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
package aztech.modern_industrialization.machinesv2.components;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;

public class EnergyComponent {
    private long storedEu;
    private final long capacity;

    public EnergyComponent(long capacity) {
        this.capacity = capacity;
    }

    public long getEu() {
        return storedEu;
    }

    public long getCapacity() {
        return capacity;
    }

    public void writeNbt(CompoundTag tag) {
        tag.putLong("storedEu", storedEu);
    }

    public void readNbt(CompoundTag tag) {
        storedEu = tag.getLong("storedEu");
    }

    public long consumeEu(long max, Simulation simulation) {
        Preconditions.checkArgument(max >= 0, "May not consume < 0 energy.");
        long ext = Math.min(max, storedEu);
        if (simulation.isActing()) {
            storedEu -= ext;
        }
        return ext;
    }

    public EnergyInsertable buildInsertable(Predicate<CableTier> canInsert) {
        return new EnergyInsertable() {
            @Override
            public long insertEnergy(long amount) {
                Preconditions.checkArgument(amount >= 0, "May not insert < 0 energy.");
                long inserted = Math.min(amount, capacity - storedEu);
                storedEu += inserted;
                // TODO: markDirty?
                return amount - inserted;
            }

            @Override
            public boolean canInsert(CableTier tier) {
                return canInsert.test(tier);
            }
        };
    }
}

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
package aztech.modern_industrialization.util;

import net.neoforged.neoforge.energy.IEnergyStorage;

public class IOEnergyStorage implements IEnergyStorage {
    private final IEnergyStorage storage;
    private final boolean allowInsert, allowExtract;

    public IOEnergyStorage(IEnergyStorage storage, boolean allowInsert, boolean allowExtract) {
        this.storage = storage;
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        return allowInsert ? storage.receiveEnergy(amount, simulate) : 0;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        return allowExtract ? storage.extractEnergy(amount, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return allowExtract;
    }

    @Override
    public boolean canReceive() {
        return allowInsert;
    }
}

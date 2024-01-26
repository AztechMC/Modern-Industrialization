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

/**
 * An energy storage that will apply additional per-insert and per-extract limits to another storage.
 */
public class LimitingEnergyStorage extends DelegatingEnergyStorage {
    private final long maxReceive;
    private final long maxExtract;

    public LimitingEnergyStorage(ILongEnergyStorage delegate, long maxReceive, long maxExtract) {
        super(delegate);
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    @Override
    public long receive(long maxReceive, boolean simulate) {
        return super.receive(Math.min(this.maxReceive, maxReceive), simulate);
    }

    @Override
    public long extract(long maxExtract, boolean simulate) {
        return super.extract(Math.min(this.maxExtract, maxExtract), simulate);
    }

    @Override
    public boolean canReceive() {
        return maxReceive > 0 && super.canReceive();
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0 && super.canExtract();
    }
}

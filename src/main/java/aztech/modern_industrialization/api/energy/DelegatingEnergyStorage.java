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

import java.util.Objects;
import java.util.function.Supplier;

public class DelegatingEnergyStorage implements ILongEnergyStorage {
    protected final Supplier<ILongEnergyStorage> delegate;

    public DelegatingEnergyStorage(Supplier<ILongEnergyStorage> delegate) {
        this.delegate = delegate;
    }

    public DelegatingEnergyStorage(ILongEnergyStorage delegate) {
        Objects.requireNonNull(delegate, "Delegate cannot be null!");
        this.delegate = () -> delegate;
    }

    @Override
    public long receive(long maxReceive, boolean simulate) {
        return delegate.get().receive(maxReceive, simulate);
    }

    @Override
    public long extract(long maxExtract, boolean simulate) {
        return delegate.get().extract(maxExtract, simulate);
    }

    @Override
    public long getAmount() {
        return delegate.get().getAmount();
    }

    @Override
    public long getCapacity() {
        return delegate.get().getCapacity();
    }

    @Override
    public boolean canExtract() {
        return delegate.get().canExtract();
    }

    @Override
    public boolean canReceive() {
        return delegate.get().canReceive();
    }
}

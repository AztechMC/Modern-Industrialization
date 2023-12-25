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

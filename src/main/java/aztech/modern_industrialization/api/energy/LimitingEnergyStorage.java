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

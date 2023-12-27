package aztech.modern_industrialization.api.energy;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.energy.IEnergyStorage;

record NonLongWrapper(IEnergyStorage storage) implements ILongEnergyStorage {
    @Override
    public long receive(long maxReceive, boolean simulate) {
        return storage.receiveEnergy(Ints.saturatedCast(maxReceive), simulate);
    }

    @Override
    public long extract(long maxExtract, boolean simulate) {
        return storage.extractEnergy(Ints.saturatedCast(maxExtract), simulate);
    }

    @Override
    public long getAmount() {
        return storage.getEnergyStored();
    }

    @Override
    public long getCapacity() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return storage.canExtract();
    }

    @Override
    public boolean canReceive() {
        return storage.canReceive();
    }

    @Override
    public String toString() {
        return "NonLongWrapper[" + storage.toString() + "]";
    }
}

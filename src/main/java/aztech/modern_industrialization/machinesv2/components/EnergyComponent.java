package aztech.modern_industrialization.machinesv2.components;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;

import java.util.function.Predicate;

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

package aztech.modern_industrialization.machines.impl.multiblock;

import aztech.modern_industrialization.api.CableTier;
import aztech.modern_industrialization.machines.impl.MachineFactory;

import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.ENERGY_INPUT;

public class EnergyInputHatchBlockEntity extends HatchBlockEntity {
    public final CableTier tier;

    public EnergyInputHatchBlockEntity(MachineFactory factory, CableTier tier) {
        super(factory, ENERGY_INPUT);
        this.tier = tier;
    }

    @Override
    protected long getMaxStoredEu() {
        return tier.maxInsert*10;
    }
}

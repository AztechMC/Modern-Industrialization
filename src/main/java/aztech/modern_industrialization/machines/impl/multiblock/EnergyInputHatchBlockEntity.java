package aztech.modern_industrialization.machines.impl.multiblock;

import alexiil.mc.lib.attributes.AttributeList;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.machines.impl.MachineFactory;

import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.ENERGY_INPUT;

public class EnergyInputHatchBlockEntity extends HatchBlockEntity {
    public final CableTier tier;
    private final EnergyInsertable insertable;

    public EnergyInputHatchBlockEntity(MachineFactory factory, CableTier tier) {
        super(factory, ENERGY_INPUT);
        this.tier = tier;
        this.insertable = buildInsertable(tier);
    }

    @Override
    protected long getMaxStoredEu() {
        return tier.getMaxInsert()*10;
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        to.offer(insertable);
    }
}

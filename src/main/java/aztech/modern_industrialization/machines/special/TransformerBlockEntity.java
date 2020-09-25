package aztech.modern_industrialization.machines.special;

import alexiil.mc.lib.attributes.AttributeList;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;

public class TransformerBlockEntity extends MachineBlockEntity {
    private final EnergyInsertable insertable;
    private final EnergyExtractable extractable;
    private final long maxStoredEu;

    public TransformerBlockEntity(MachineFactory factory, CableTier inputTier, CableTier outputTier) {
        super(factory, null);

        insertable = buildInsertable(inputTier);
        extractable = buildExtractable(outputTier);
        maxStoredEu = Math.min(inputTier.getMaxInsert(), outputTier.getMaxInsert()) * 10;
    }

    @Override
    protected long getMaxStoredEu() {
        return maxStoredEu;
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        if(to.getTargetSide() == outputDirection) {
            to.offer(extractable);
        } else {
            to.offer(insertable);
        }
    }

    @Override
    public void tick() {
        // TODO: auto-extract energy
    }
}

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
    private final CableTier outputTier;

    public TransformerBlockEntity(MachineFactory factory, CableTier inputTier, CableTier outputTier) {
        super(factory);

        insertable = buildInsertable(inputTier);
        extractable = buildExtractable(outputTier);
        maxStoredEu = Math.min(inputTier.getMaxInsert(), outputTier.getMaxInsert()) * 10;
        this.outputTier = outputTier;
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
        autoExtractEnergy(outputDirection, outputTier);
        markDirty();
    }
}

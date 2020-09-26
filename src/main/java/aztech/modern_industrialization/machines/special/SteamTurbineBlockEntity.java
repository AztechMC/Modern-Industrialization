package aztech.modern_industrialization.machines.special;

import alexiil.mc.lib.attributes.AttributeList;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;

public final class SteamTurbineBlockEntity extends MachineBlockEntity {
    private final EnergyExtractable extractable;
    private final CableTier tier;
    public SteamTurbineBlockEntity(MachineFactory factory, CableTier tier) {
        super(factory);

        fluidStacks.set(0, ConfigurableFluidStack.lockedInputSlot(factory.getInputBucketCapacity() * 1000, STEAM_KEY));
        this.tier = tier;
        extractable = buildExtractable(tier);
    }

    @Override
    protected long getMaxStoredEu() {
        return tier.getMaxInsert() * 10;
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        if(to.getTargetSide() == outputDirection) {
            to.offer(extractable);
        }
    }

    @Override
    public void tick() {
        if(world.isClient) return;

        boolean wasActive = isActive;

        int transformed = (int) Math.min(Math.min(fluidStacks.get(0).getAmount(), getMaxStoredEu() - storedEu), tier.getEu());
        if(transformed > 0) {
            fluidStacks.get(0).decrement(transformed);
            storedEu += transformed;
            isActive = true;
        } else {
            isActive = false;
        }

        if(wasActive != isActive) {
            sync();
        }
        markDirty();
    }


}

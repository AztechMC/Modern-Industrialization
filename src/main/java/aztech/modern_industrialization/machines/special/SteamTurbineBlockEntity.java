package aztech.modern_industrialization.machines.special;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.Simulation;
import aztech.modern_industrialization.api.EnergyExtractable;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;

public final class SteamTurbineBlockEntity extends MachineBlockEntity {
    public SteamTurbineBlockEntity(MachineFactory factory, MachineRecipeType recipeType) {
        super(factory, recipeType);

        fluidStacks.set(0, ConfigurableFluidStack.lockedInputSlot(factory.getInputBucketCapacity() * 1000, STEAM_KEY));
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        if(to.getTargetSide() == outputDirection) {
            to.offer((EnergyExtractable) simulation -> {
                if(usedAmp) return 0;
                int extracted = getTier().getMaxEu();
                if(storedEu < extracted) return 0;
                if(simulation.isAction()) {
                    storedEu -= extracted;
                    usedAmp = true;
                    markDirty();
                }
                return extracted;
            });
        }
    }

    @Override
    public void tick() {
        if(world.isClient) return;

        boolean wasActive = isActive;
        usedAmp = false;

        int transformed = (int) Math.min(Math.min(fluidStacks.get(0).getAmount(), getMaxStoredEu() - storedEu), getTier().getMaxEu());
        if(transformed > 0) {
            fluidStacks.get(0).decrement(transformed);
            storedEu += transformed;
        } else {
            isActive = false;
        }

        if(wasActive != isActive) {
            sync();
        }
        markDirty();
    }


}

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
            to.offer((EnergyExtractable) maxAmount -> {
                long ext = Math.min(maxAmount, storedEu);
                storedEu -= ext;
                markDirty();
                return ext;
            });
        }
    }

    @Override
    public void tick() {
        if(world.isClient) return;

        boolean wasActive = isActive;

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

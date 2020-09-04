package aztech.modern_industrialization.machines.special;

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

// TODO: progress bar

/**
 * The block entity for a steam boiler.
 * We reuse the generic MachineBlockEntity, but we override the tick() function.
 * We reuse usedEnergy and recipeEnergy to keep track of the remaining burn time of the fuel.
 * We also reuse efficiencyTicks and maxEfficiencyTicks to keep track of the boiler temperature instead.
 */
public class SteamBoilerBlockEntity extends MachineBlockEntity {
    private static final int BURN_TIME_MULTIPLIER = 10;
    public SteamBoilerBlockEntity(MachineFactory factory, MachineRecipeType recipeType) {
        super(factory, recipeType);

        getFluidStacks().set(0, ConfigurableFluidStack.lockedInputSlot(this, factory.getInputBucketCapacity() * FluidUnit.DROPS_PER_BUCKET, FluidKeys.WATER));
        getFluidStacks().set(1, ConfigurableFluidStack.lockedOutputSlot(this, factory.getOutputBucketCapacity() * FluidUnit.DROPS_PER_BUCKET, STEAM_KEY));

        maxEfficiencyTicks = 10000;
        efficiencyTicks = 0;
    }

    @Override
    public void tick() {
        if(world.isClient) return;

        boolean wasActive = isActive;

        this.isActive = false;
        if(usedEnergy == 0) {
            ItemStack fuel = getItemStacks().get(0).getStack();
            if(fuel.getCount() > 0) {
                Integer fuelTime = FuelRegistryImpl.INSTANCE.get(fuel.getItem());
                if(fuelTime != null && fuelTime > 0) {
                    recipeEnergy = fuelTime * BURN_TIME_MULTIPLIER;
                    usedEnergy = recipeEnergy;
                    fuel.decrement(1);
                }
            }
        }

        if(usedEnergy > 0) {
            isActive = true;
            --usedEnergy;
        }

        if(isActive) {
            efficiencyTicks = Math.min(efficiencyTicks+1, maxEfficiencyTicks);
        } else {
            efficiencyTicks = Math.max(efficiencyTicks-1, 0);
        }

        if(efficiencyTicks > 1000) {
            int steamProduction = ( factory.tier ==  MachineTier.BRONZE ? 8 : 16) * efficiencyTicks / maxEfficiencyTicks;
            if(steamProduction > 0 && fluidStacks.get(0).getAmount() > 0) {
                int remSpace = fluidStacks.get(1).getRemainingSpace();
                int actualProduced = Math.min(steamProduction, remSpace);
                if(actualProduced > 0) {
                    fluidStacks.get(1).increment(actualProduced);
                    fluidStacks.get(0).decrement(1);
                }
            }
        }

        if(isActive != wasActive) {
            sync();
        }
        markDirty();

        for(Direction direction : Direction.values()) {
            autoExtractFluids(world, pos, direction);
        }
    }

    @Override
    public boolean hasOutput() {
        return false;
    }
}

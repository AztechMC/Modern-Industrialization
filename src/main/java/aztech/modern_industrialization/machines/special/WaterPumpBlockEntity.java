package aztech.modern_industrialization.machines.special;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * We reuse the generic MachineBlockEntity, overriding tick() and using the energy
 * for the cooldown between two pumping attempts.
 */
public class WaterPumpBlockEntity extends MachineBlockEntity {
    public WaterPumpBlockEntity(MachineFactory factory, MachineRecipeType recipeType) {
        super(factory, recipeType);

        int waterSlotId = fluidStacks.size()-1;
        fluidStacks.set(1, ConfigurableFluidStack.lockedOutputSlot(this, factory.getOutputBucketCapacity() * FluidUnit.DROPS_PER_BUCKET, Fluids.WATER));
        usedEnergy = 0;
        recipeEnergy = 100;
    }


    private static final int[] DX = new int[] { -1, 0, 1, 1, 1, 0, -1, -1 };
    private static final int[] DZ = new int[] { -1, -1, -1, 0, 1, 1, 1, 0 };
    @Override
    public void tick() {
        if(world.isClient) return;

        ConfigurableFluidStack waterStack = fluidStacks.get(fluidStacks.size()-1);
        if(waterStack.getRemainingSpace() < FluidUnit.DROPS_PER_BUCKET / 8) {
            if(isActive) {
                isActive = false;
                sync();
                markDirty();
            }
            return;
        }

        int eu = getEu(1, false);
        usedEnergy += eu;
        if(eu > 0) {
            if(!isActive) {
                isActive = true;
                sync();
            }
        }

        if(usedEnergy == recipeEnergy) {
            boolean[] adjWater = new boolean[] { false, false, false, false, false, false, false, false };
            for(int i = 0; i < 8; ++i) {
                BlockPos adjPos = pos.add(DX[i], 0, DZ[i]);
                if(world.isChunkLoaded(adjPos)) {
                    FluidState adjState = world.getFluidState(adjPos);
                    if(adjState.isStill() && adjState.getFluid() == Fluids.WATER) {
                        adjWater[i] = true;
                    }
                }
            }
            int providedBucketEights = 0;
            for(int i = 0; i < 8; ++i) {
                if(adjWater[i]) {
                    if(i%2 == 1 || (adjWater[(i+7)%8] || adjWater[(i+1)%8])) {
                        providedBucketEights++;
                    }
                }
            }
            int factorTier = ( factory.tier ==  MachineTier.BRONZE ? 1 : 2);
            waterStack.increment(Math.min(factorTier*providedBucketEights * FluidUnit.DROPS_PER_BUCKET / 8, waterStack.getRemainingSpace()));
            usedEnergy = 0;
        }
        markDirty();

        for(Direction direction : Direction.values()) {
            autoExtractFluids(direction, world.getBlockEntity(pos.offset(direction)));
        }
    }

    @Override
    public boolean hasOutput() {
        return false;
    }
}

package aztech.modern_industrialization.machines.special;

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static aztech.modern_industrialization.machines.impl.MachineTier.LV;
import static aztech.modern_industrialization.machines.impl.MachineTier.STEEL;

/**
 * We reuse the generic MachineBlockEntity, overriding tick() and using the energy
 * for the cooldown between two pumping attempts.
 */
public class WaterPumpBlockEntity extends MachineBlockEntity {
    public WaterPumpBlockEntity(MachineFactory factory) {
        super(factory);

        fluidStacks.set(fluidStacks.size()-1, ConfigurableFluidStack.lockedOutputSlot(factory.getOutputBucketCapacity() * 1000, FluidKeys.WATER));
        usedEnergy = 0;
        recipeEnergy = 100;
    }


    private static final int[] DX = new int[] { -1, 0, 1, 1, 1, 0, -1, -1 };
    private static final int[] DZ = new int[] { -1, -1, -1, 0, 1, 1, 1, 0 };
    @Override
    public void tick() {
        if(world.isClient) return;

        ConfigurableFluidStack waterStack = fluidStacks.get(fluidStacks.size()-1);
        if(waterStack.getRemainingSpace() < 1000 / 8) {
            if(isActive) {
                isActive = false;
                sync();
                markDirty();
            }
        } else {

            int eu = getEu(1, false);
            usedEnergy += eu;
            if (eu > 0) {
                if (!isActive) {
                    isActive = true;
                    sync();
                }
            }

            if (usedEnergy == recipeEnergy) {
                boolean[] adjWater = new boolean[]{false, false, false, false, false, false, false, false};
                for (int i = 0; i < 8; ++i) {
                    BlockPos adjPos = pos.add(DX[i], 0, DZ[i]);
                    if (world.isChunkLoaded(adjPos)) {
                        FluidState adjState = world.getFluidState(adjPos);
                        if (adjState.isStill() && adjState.getFluid() == Fluids.WATER) {
                            adjWater[i] = true;
                        }
                    }
                }
                int providedBucketEights = 0;
                for (int i = 0; i < 8; ++i) {
                    if (adjWater[i]) {
                        if (i % 2 == 1 || (adjWater[(i + 7) % 8] || adjWater[(i + 1) % 8])) {
                            providedBucketEights++;
                        }
                    }
                }
                int factorTier = 1;
                if(factory.tier == STEEL) {
                    factorTier = 2;
                } else if(factory.tier == LV) {
                    factorTier = 16;
                }
                waterStack.increment(Math.min(factorTier * providedBucketEights * 1000 / 8, waterStack.getRemainingSpace()));
                usedEnergy = 0;
            }
            markDirty();
        }

        for(Direction direction : Direction.values()) {
            autoExtractFluids(world, pos, direction);
        }
    }

    @Override
    public boolean hasOutput() {
        return false;
    }
}

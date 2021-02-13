/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machines.special;

import static aztech.modern_industrialization.machines.impl.MachineTier.LV;
import static aztech.modern_industrialization.machines.impl.MachineTier.STEEL;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * We reuse the generic ElectricMachineBlockEntity, overriding tick() and using the
 * energy for the cooldown between two pumping attempts.
 */
public class WaterPumpBlockEntity extends MachineBlockEntity {
    public WaterPumpBlockEntity(MachineFactory factory) {
        super(factory);

        inventory.fluidStacks.set(inventory.fluidStacks.size() - 1,
                ConfigurableFluidStack.lockedOutputSlot(factory.getOutputBucketCapacity() * 81000, Fluids.WATER));
        usedEnergy = 0;
        recipeEnergy = 100;
    }

    private static final int[] DX = new int[] { -1, 0, 1, 1, 1, 0, -1, -1 };
    private static final int[] DZ = new int[] { -1, -1, -1, 0, 1, 1, 1, 0 };

    @Override
    public void tick() {
        if (world.isClient)
            return;

        ConfigurableFluidStack waterStack = inventory.fluidStacks.get(inventory.fluidStacks.size() - 1);
        if (waterStack.getRemainingSpace() < 81000 / 8) {
            if (isActive) {
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
                boolean[] adjWater = new boolean[] { false, false, false, false, false, false, false, false };
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
                if (factory.tier == STEEL) {
                    factorTier = 2;
                } else if (factory.tier == LV) {
                    factorTier = 16;
                }
                waterStack.increment(Math.min(factorTier * providedBucketEights * 81000 / 8, waterStack.getRemainingSpace()));
                usedEnergy = 0;
            }
            markDirty();
        }

        for (Direction direction : Direction.values()) {
            inventory.autoExtractFluids(world, pos, direction);
        }
    }

    @Override
    public boolean hasOutput() {
        return false;
    }
}

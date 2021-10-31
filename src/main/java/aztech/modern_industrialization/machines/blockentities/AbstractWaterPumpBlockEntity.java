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
package aztech.modern_industrialization.machines.blockentities;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.sync.ProgressBar;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.helper.OrientationHelper;
import aztech.modern_industrialization.util.Tickable;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class AbstractWaterPumpBlockEntity extends MachineBlockEntity implements Tickable {
    protected static final int OUTPUT_SLOT_X = 110;
    protected static final int OUTPUT_SLOT_Y = 30;
    private static final ProgressBar.Parameters PROGRESS_BAR = new ProgressBar.Parameters(79, 29, "extract");
    private static final int OPERATION_TICKS = 100;

    public AbstractWaterPumpBlockEntity(BEP bep, String blockName) {
        super(bep, new MachineGuiParameters.Builder(blockName, false).build());

        orientation = new OrientationComponent(new OrientationComponent.Params(true, false, false));

        isActiveComponent = new IsActiveComponent();
        registerClientComponent(new ProgressBar.Server(PROGRESS_BAR, () -> (float) pumpingTicks / OPERATION_TICKS));
        this.registerComponents(orientation, isActiveComponent, new IComponent() {
            @Override
            public void writeNbt(NbtCompound tag) {
                tag.putInt("pumpingTicks", pumpingTicks);
            }

            @Override
            public void readNbt(NbtCompound tag) {
                pumpingTicks = tag.getInt("pumpingTicks");
            }
        });

    }

    abstract protected long consumeEu(long max);

    abstract protected int getWaterMultiplier();

    protected final OrientationComponent orientation;
    protected int pumpingTicks = 0; // number of ticks spent pumping this iteration
    protected IsActiveComponent isActiveComponent;

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        return OrientationHelper.onUse(player, hand, face, orientation, this);
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            List<ConfigurableFluidStack> fluidStacks = getInventory().getFluidStacks();
            ConfigurableFluidStack waterStack = fluidStacks.get(fluidStacks.size() - 1);
            if (waterStack.getRemainingSpace() < 81000 / 8) {
                updateActive(false);
            } else {
                long eu = consumeEu(1);
                pumpingTicks += eu;
                updateActive(eu > 0);

                if (pumpingTicks == OPERATION_TICKS) {
                    waterStack.setKey(FluidVariant.of(Fluids.WATER));
                    waterStack.increment(Math.min((long) getWaterMultiplier() * getWaterSourceCount() * 81000 / 8, waterStack.getRemainingSpace()));
                    pumpingTicks = 0;
                }
            }
            getInventory().autoExtractFluids(world, pos, orientation.outputDirection);
            markDirty();
        }
    }

    private void updateActive(boolean newActive) {
        isActiveComponent.updateActive(newActive, this);
    }

    private static final int[] DX = new int[] { -1, 0, 1, 1, 1, 0, -1, -1 };
    private static final int[] DZ = new int[] { -1, -1, -1, 0, 1, 1, 1, 0 };

    @SuppressWarnings("ConstantConditions")
    private int getWaterSourceCount() {
        boolean[] adjWater = new boolean[] { false, false, false, false, false, false, false, false };
        for (int i = 0; i < 8; ++i) {
            BlockPos adjPos = pos.add(DX[i], 0, DZ[i]);
            FluidState adjState = world.getFluidState(adjPos);
            if (adjState.isStill() && adjState.getFluid() == Fluids.WATER) {
                adjWater[i] = true;
            }
        }
        int count = 0;
        for (int i = 0; i < 8; ++i) {
            if (adjWater[i]) {
                if (i % 2 == 1 || (adjWater[(i + 7) % 8] || adjWater[(i + 1) % 8])) {
                    count++;
                }
            }
        }
        return count;
    }

}

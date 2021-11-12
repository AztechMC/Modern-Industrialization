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
package aztech.modern_industrialization.machines.blockentities.multiblocks;

import aztech.modern_industrialization.api.ScrewdriverableBlockEntity;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.Tickable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EnergyFromFluidMultiblockBlockEntity extends MultiblockMachineBlockEntity implements Tickable, ScrewdriverableBlockEntity {

    public EnergyFromFluidMultiblockBlockEntity(BEP bep, String name, ShapeTemplate shapeTemplate, Predicate<Fluid> acceptedFluid,
            ToLongFunction<Fluid> fluidEUperMb, long maxEnergyOutput) {

        super(bep, new MachineGuiParameters.Builder(name, false).backgroundHeight(128).build(), new OrientationComponent.Params(false, false, false));

        this.activeShape = new ActiveShapeComponent(new ShapeTemplate[] { shapeTemplate });
        this.inventory = new MultiblockInventoryComponent();
        this.isActiveComponent = new IsActiveComponent();
        this.fluidConsumer = new FluidConsumerComponent(maxEnergyOutput, acceptedFluid, fluidEUperMb);

        this.registerComponents(activeShape, isActiveComponent, fluidConsumer);
    }

    @Nullable
    private ShapeMatcher shapeMatcher = null;
    private boolean allowNormalOperation = false;

    private final ActiveShapeComponent activeShape;
    private final MultiblockInventoryComponent inventory;
    private final IsActiveComponent isActiveComponent;
    private final List<EnergyComponent> energyOutputs = new ArrayList<>();
    private final FluidConsumerComponent fluidConsumer;

    public ShapeTemplate getActiveShape() {
        return activeShape.getActiveShape();
    }

    protected void onSuccessfulMatch(ShapeMatcher shapeMatcher) {
        energyOutputs.clear();
        for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
            hatch.appendEnergyOutputs(energyOutputs);
        }
    }

    @Override
    public boolean useScrewdriver(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        return useScrewdriver(activeShape, player, hand, hitResult);
    }

    @Override
    public final MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    protected final MachineModelClientData getModelData() {
        return new MachineModelClientData(null, orientation.facingDirection).active(isActiveComponent.isActive);
    }

    @Override
    public final void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    @Override
    public final void tick() {

        if (!world.isClient) {
            link();
            if (allowNormalOperation) {
                long euProduced = fluidConsumer.getEuProduction(inventory.getFluidInputs(), insertEnergy(Long.MAX_VALUE, Simulation.SIMULATE));
                insertEnergy(euProduced, Simulation.ACT);
                isActiveComponent.updateActive(euProduced != 0, this);
            }
            markDirty();
        }

    }

    public long insertEnergy(long value, Simulation simulation) {
        long rem = value;
        long inserted = 0;
        for (EnergyComponent e : energyOutputs) {
            if (rem > 0) {
                inserted += e.insertEu(rem, simulation);
                rem -= inserted;
            }
        }
        return inserted;
    }

    protected final void link() {
        if (shapeMatcher == null) {
            shapeMatcher = new ShapeMatcher(world, pos, orientation.facingDirection, getActiveShape());
            shapeMatcher.registerListeners(world);
        }
        if (shapeMatcher.needsRematch()) {
            allowNormalOperation = false;
            shapeValid.shapeValid = false;
            shapeMatcher.rematch(world);

            if (shapeMatcher.isMatchSuccessful()) {
                inventory.rebuild(shapeMatcher);

                onSuccessfulMatch(shapeMatcher);
                shapeValid.shapeValid = true;
                allowNormalOperation = true;
            }

            if (shapeValid.update()) {
                sync(false);
            }
        }
    }

    @Override
    protected final void unlink() {
        if (shapeMatcher != null) {
            shapeMatcher.unlinkHatches();
            shapeMatcher.unregisterListeners(world);
            shapeMatcher = null;
        }
    }
}

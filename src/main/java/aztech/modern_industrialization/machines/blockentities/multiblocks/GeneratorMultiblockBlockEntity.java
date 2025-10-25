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

import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.Tickable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

public class GeneratorMultiblockBlockEntity extends MultiblockMachineBlockEntity implements Tickable,
        EnergyListComponentHolder, MultiblockInventoryComponentHolder {
    public GeneratorMultiblockBlockEntity(BEP bep,
            String name,
            ShapeTemplate shapeTemplate,
            FluidItemConsumerComponent fluidConsumer) {
        super(bep, new MachineGuiParameters.Builder(name, false)
                .backgroundHeight(128).build(),
                new OrientationComponent.Params(false, false, false));

        this.activeShape = new ActiveShapeComponent(new ShapeTemplate[] { shapeTemplate });
        this.inventory = new MultiblockInventoryComponent();
        this.isActiveComponent = new IsActiveComponent();
        this.fluidConsumer = fluidConsumer;
        this.redstoneControl = new RedstoneControlComponent();

        this.registerComponents(activeShape, isActiveComponent, fluidConsumer, redstoneControl);
        registerGuiComponent(new SlotPanel.Server(this).withRedstoneControl(redstoneControl));
    }

    private boolean allowNormalOperation = false;

    private final ActiveShapeComponent activeShape;
    private final MultiblockInventoryComponent inventory;
    private final IsActiveComponent isActiveComponent;
    private final RedstoneControlComponent redstoneControl;
    private final List<EnergyComponent> energyOutputs = new ArrayList<>();
    private final FluidItemConsumerComponent fluidConsumer;

    public ShapeTemplate getActiveShape() {
        return activeShape.getActiveShape();
    }

    @Override
    public List<EnergyComponent> getEnergyComponents() {
        return energyOutputs;
    }

    @Override
    public MultiblockInventoryComponent getMultiblockInventoryComponent() {
        return inventory;
    }

    @Override
    public final MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    protected final MachineModelClientData getMachineModelData() {
        return new MachineModelClientData(null, orientation.facingDirection).active(isActiveComponent.isActive);
    }

    @Override
    public final void tick() {
        if (!level.isClientSide) {
            link();
            if (allowNormalOperation) {
                if (this.redstoneControl.doAllowNormalOperation(this)) {
                    long euProduced = fluidConsumer.getEuProduction(inventory.getFluidInputs(),
                            inventory.getItemInputs(),
                            insertEnergy(Long.MAX_VALUE, Simulation.SIMULATE));
                    insertEnergy(euProduced, Simulation.ACT);
                    isActiveComponent.updateActive(euProduced != 0, this);
                } else {
                    isActiveComponent.updateActive(false, this);
                }
            } else {
                isActiveComponent.updateActive(false, this);
            }
            setChanged();
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

    @Override
    protected void onRematch(ShapeMatcher shapeMatcher) {
        allowNormalOperation = false;
        if (shapeMatcher.isMatchSuccessful()) {
            inventory.rebuild(shapeMatcher);
            allowNormalOperation = true;

            energyOutputs.clear();
            for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
                hatch.appendEnergyOutputs(energyOutputs);
            }
        }
    }

    @Override
    public List<Component> getTooltips() {
        return fluidConsumer.getTooltips();
    }
}

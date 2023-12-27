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

import static aztech.modern_industrialization.machines.components.FluidItemConsumerComponent.NumberOfFuel.*;

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.helper.EnergyHelper;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.Tickable;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class GeneratorMachineBlockEntity extends MachineBlockEntity implements Tickable, EnergyComponentHolder {

    private final CableTier outputTier;
    private final MIEnergyStorage extractable;
    private final RedstoneControlComponent redstoneControl;

    protected final MIInventory inventory;
    protected EnergyComponent energy;
    protected IsActiveComponent isActiveComponent;

    protected FluidItemConsumerComponent fluidItemConsumer;

    public GeneratorMachineBlockEntity(BEP bep,
            String name,
            CableTier outputTier,
            long energyCapacity,
            long fluidCapacity,
            FluidItemConsumerComponent fluidItemConsumer) {

        super(bep, new MachineGuiParameters.Builder(name, fluidItemConsumer.doAllowMoreThanOne()).build(),
                new OrientationComponent.Params(true, false, false));

        this.outputTier = outputTier;
        this.energy = new EnergyComponent(this, energyCapacity);
        this.extractable = energy.buildExtractable((CableTier tier) -> tier == outputTier);
        this.isActiveComponent = new IsActiveComponent();
        this.fluidItemConsumer = fluidItemConsumer;
        this.redstoneControl = new RedstoneControlComponent();

        EnergyBar.Parameters energyBarParams = new EnergyBar.Parameters(76, 39);
        registerGuiComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));

        List<ConfigurableItemStack> itemStacks;
        List<ConfigurableFluidStack> fluidStacks;

        SlotPositions itemPositions = SlotPositions.empty();
        SlotPositions fluidPositions = SlotPositions.empty();

        var numberOfFluid = fluidItemConsumer.fluidEUProductionMap.getNumberOfFuel();
        var numberOfItem = fluidItemConsumer.itemEUProductionMap.getNumberOfFuel();

        if (numberOfFluid == NONE
                && numberOfItem == NONE) {
            throw new IllegalArgumentException(
                    String.format("GeneratorMachineBlockEntity %s must accept at least one item or fluid", name));
        }

        if (numberOfItem != NONE) {
            if (numberOfItem == MANY) {
                itemStacks = List.of(ConfigurableItemStack.standardInputSlot());
            } else {
                itemStacks = List.of(ConfigurableItemStack.lockedInputSlot(
                        fluidItemConsumer.itemEUProductionMap.getAllAccepted().iterator().next()));
            }
        } else {
            itemStacks = Collections.emptyList();
            itemPositions = SlotPositions.empty();
            fluidPositions = new SlotPositions.Builder().addSlot(25, 38).build();
        }

        if (numberOfFluid != NONE) {

            if (fluidCapacity == 0) {
                throw new IllegalArgumentException(
                        String.format("GeneratorMachineBlockEntity %s must have a fluid capacity > 0 has it accepts fluids", name));
            }

            if (numberOfFluid == MANY) {
                fluidStacks = List.of(ConfigurableFluidStack.standardInputSlot(fluidCapacity));
            } else {
                fluidStacks = List.of(ConfigurableFluidStack.lockedInputSlot(fluidCapacity,
                        fluidItemConsumer.fluidEUProductionMap.getAllAccepted().iterator().next()));
            }

            if (numberOfItem != NONE) {
                itemPositions = new SlotPositions.Builder().addSlot(15, 38).build();
                fluidPositions = new SlotPositions.Builder().addSlot(35, 38).build();
            }

        } else {
            fluidStacks = Collections.emptyList();
            fluidPositions = SlotPositions.empty();
            itemPositions = new SlotPositions.Builder().addSlot(25, 38).build();
        }

        inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);

        this.registerComponents(energy, isActiveComponent, inventory, fluidItemConsumer, redstoneControl);
        this.registerGuiComponent(new SlotPanel.Server(this).withRedstoneControl(redstoneControl));

    }

    public GeneratorMachineBlockEntity(BEP bep,
            String name,
            CableTier outputTier,
            long energyCapacity,
            long fluidCapacity,
            long maxEnergyOutput,
            FluidDefinition acceptedFluid,
            long fluidEUperMb) {

        this(bep, name, outputTier, energyCapacity, fluidCapacity,
                FluidItemConsumerComponent.ofSingleFluid(
                        maxEnergyOutput,
                        acceptedFluid,
                        fluidEUperMb));
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    protected MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData();
        data.isActive = isActiveComponent.isActive;
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide)
            return;

        if (!redstoneControl.doAllowNormalOperation(this)) {
            isActiveComponent.updateActive(false, this);
        } else {
            long euProduced = fluidItemConsumer.getEuProduction(inventory.getFluidStacks(),
                    inventory.getItemStacks(),
                    energy.getRemainingCapacity());

            energy.insertEu(euProduced, Simulation.ACT);
            isActiveComponent.updateActive(0 != euProduced, this);
        }
        EnergyHelper.autoOutput(this, orientation, outputTier, extractable);
        setChanged();
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(EnergyApi.SIDED, bet, (be, direction) -> {
                GeneratorMachineBlockEntity abe = (GeneratorMachineBlockEntity) be;
                if (abe.orientation.outputDirection == direction) {
                    return abe.extractable;
                } else {
                    return null;
                }
            });
        });
    }

    @Override
    public List<Component> getTooltips() {
        return fluidItemConsumer.getTooltips();
    }
}

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
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.AutoExtract;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Tickable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.neoforged.neoforge.fluids.FluidType;

public class ConfigurableTankMachineBlockEntity extends MachineBlockEntity implements Tickable {

    private final MIInventory inventory;

    public ConfigurableTankMachineBlockEntity(BEP bep) {
        super(bep, new MachineGuiParameters.Builder("configurable_tank", true).backgroundHeight(170).build(),
                new OrientationComponent.Params(true, false, true));

        List<ConfigurableFluidStack> stacks = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            stacks.add(ConfigurableFluidStack.standardIOSlot(16 * FluidType.BUCKET_VOLUME, true));
        }
        SlotPositions fluidPositions = new SlotPositions.Builder().addSlots(68, 20, 3, 3).build();
        inventory = new MIInventory(Collections.emptyList(), stacks, SlotPositions.empty(), fluidPositions);

        registerGuiComponent(new AutoExtract.Server(orientation));
        registerComponents(inventory);
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    protected MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData(MachineCasings.CONFIGURABLE_TANK);
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            if (orientation.extractFluids) {
                inventory.autoExtractFluids(level, worldPosition, orientation.outputDirection);
            }
        }
    }
}

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
package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machinesv2.components.EnergyComponent;
import aztech.modern_industrialization.machinesv2.components.sync.EnergyBar;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import java.util.Collections;
import net.minecraft.block.entity.BlockEntityType;

public class ElectricWaterPumpBlockEntity extends AbstractWaterPumpBlockEntity {
    public ElectricWaterPumpBlockEntity(BlockEntityType<?> type) {
        super(type, "lv_water_pump");

        long capacity = 81000 * MITanks.BRONZE.bucketCapacity * 4;
        this.inventory = new MIInventory(Collections.emptyList(),
                Collections.singletonList(ConfigurableFluidStack.lockedOutputSlot(capacity, MIFluids.STEAM)), SlotPositions.empty(),
                new SlotPositions.Builder().addSlot(OUTPUT_SLOT_X, OUTPUT_SLOT_Y).build());
        this.energy = new EnergyComponent(3200);
        this.insertable = energy.buildInsertable(tier -> tier == CableTier.LV);
        registerClientComponent(new EnergyBar.Server(new EnergyBar.Parameters(18, 32), energy::getEu, energy::getCapacity));
        this.registerComponents(energy);
        this.registerComponents(inventory);
    }

    private final MIInventory inventory;
    private final EnergyComponent energy;
    private final EnergyInsertable insertable;

    @Override
    protected long consumeEu(long max) {
        return energy.consumeEu(max, Simulation.ACT);
    }

    @Override
    protected int getWaterMultiplier() {
        return 16;
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData();
        data.isActive = isActiveComponent.isActive;
        orientation.writeModelData(data);
        return data;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        EnergyApi.MOVEABLE.registerForBlockEntities((be, direction) -> ((ElectricWaterPumpBlockEntity) be).insertable, bet);
    }
}

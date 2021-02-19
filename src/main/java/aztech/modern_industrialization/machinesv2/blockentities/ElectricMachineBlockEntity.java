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

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.components.EnergyComponent;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import aztech.modern_industrialization.machinesv2.components.sync.EnergyBar;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.components.sync.RecipeEfficiencyBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;

public class ElectricMachineBlockEntity extends SingleBlockCraftingMachineBlockEntity {
    public ElectricMachineBlockEntity(BlockEntityType<?> type, MachineRecipeType recipeType, MachineInventoryComponent inventory,
            MachineGuiParameters guiParams, EnergyBar.Parameters energyBarParams, ProgressBar.Parameters progressBarParams,
            RecipeEfficiencyBar.Parameters efficiencyBarParams, MachineTier tier, long euCapacity) {
        super(type, recipeType, inventory, guiParams, progressBarParams, tier);
        this.energy = new EnergyComponent(euCapacity);
        this.insertable = energy.buildInsertable(cableTier -> cableTier == CableTier.LV);
        registerClientComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));
        registerClientComponent(new RecipeEfficiencyBar.Server(efficiencyBarParams, crafter));
    }

    private final EnergyComponent energy;
    private final EnergyInsertable insertable;

    @Override
    public long consumeEu(long max, Simulation simulation) {
        return energy.consumeEu(max, simulation);
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData(MachineCasings.LV);
        orientation.writeModelData(data);
        data.isActive = isActive;
        return data;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        EnergyApi.MOVEABLE.registerForBlockEntities((be, direction) -> ((ElectricMachineBlockEntity) be).insertable, bet);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        energy.writeNbt(tag);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        energy.readNbt(tag);
    }
}

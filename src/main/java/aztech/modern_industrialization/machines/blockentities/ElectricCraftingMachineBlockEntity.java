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

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.RecipeEfficiencyBar;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ElectricCraftingMachineBlockEntity extends AbstractCraftingMachineBlockEntity implements EnergyComponentHolder {

    public ElectricCraftingMachineBlockEntity(BEP bep, MachineRecipeType recipeType, MachineInventoryComponent inventory,
            MachineGuiParameters guiParams, EnergyBar.Parameters energyBarParams, ProgressBar.Parameters progressBarParams,
            RecipeEfficiencyBar.Parameters efficiencyBarParams, MachineTier tier, long euCapacity) {
        super(bep, recipeType, inventory, guiParams, progressBarParams, tier);
        this.redstoneControl = new RedstoneControlComponent();
        this.casing = new CasingComponent();
        this.upgrades = new UpgradeComponent();
        this.energy = new EnergyComponent(this, casing::getEuCapacity);
        this.insertable = energy.buildInsertable(cableTier -> this.casing.canInsertEu(cableTier));
        registerGuiComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));
        registerGuiComponent(new RecipeEfficiencyBar.Server(efficiencyBarParams, crafter));
        registerGuiComponent(new SlotPanel.Server(this)
                .withRedstoneControl(redstoneControl)
                .withUpgrades(upgrades)
                .withCasing(casing));
        this.registerComponents(redstoneControl, casing, upgrades, energy);
    }

    private final RedstoneControlComponent redstoneControl;
    private final CasingComponent casing;
    private final UpgradeComponent upgrades;
    private final EnergyComponent energy;
    private final MIEnergyStorage insertable;

    @Override
    public boolean isEnabled() {
        return redstoneControl.doAllowNormalOperation(this);
    }

    @Override
    public long consumeEu(long max, Simulation simulation) {
        return energy.consumeEu(max, simulation);
    }

    @Override
    protected MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData(casing.getCasing());
        orientation.writeModelData(data);
        data.isActive = isActiveComponent.isActive;
        return data;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(EnergyApi.SIDED, bet, (be, direction) -> ((ElectricCraftingMachineBlockEntity) be).insertable);
        });
    }

    @Override
    protected InteractionResult onUse(Player player, InteractionHand hand, Direction face) {
        InteractionResult result = super.onUse(player, hand, face);
        if (!result.consumesAction()) {
            result = redstoneControl.onUse(this, player, hand);
        }
        if (!result.consumesAction()) {
            result = casing.onUse(this, player, hand);
        }
        if (!result.consumesAction()) {
            result = upgrades.onUse(this, player, hand);
        }
        if (!result.consumesAction()) {
            result = LubricantHelper.onUse(this.crafter, player, hand);
        }
        return result;
    }

    @Override
    public long getMaxRecipeEu() {
        return tier.getMaxEu() + upgrades.getAddMaxEUPerTick();
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }
}

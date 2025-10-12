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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.minecraft.resources.ResourceLocation;

public class ElectricCraftingMultiblockBlockEntity extends AbstractElectricCraftingMultiblockBlockEntity implements EnergyListComponentHolder {
    public ElectricCraftingMultiblockBlockEntity(BEP bep, ResourceLocation blockId, ShapeTemplate shapeTemplate,
            MachineRecipeType recipeType) {
        super(bep, blockId, new OrientationComponent.Params(false, false, false), new ShapeTemplate[] { shapeTemplate });
        this.recipeType = recipeType;
        this.upgrades = new UpgradeComponent();
        this.overdrive = new OverdriveComponent();
        this.registerComponents(upgrades, overdrive);
        registerGuiComponent(new SlotPanel.Server(this)
                .withRedstoneControl(redstoneControl)
                .withUpgrades(upgrades)
                .withOverdrive(overdrive));
    }

    public ElectricCraftingMultiblockBlockEntity(BEP bep, String name, ShapeTemplate shapeTemplate, MachineRecipeType recipeType) {
        this(bep, MI.id(name), shapeTemplate, recipeType);
    }

    private final MachineRecipeType recipeType;
    private final UpgradeComponent upgrades;
    private final OverdriveComponent overdrive;

    @Override
    public MachineRecipeType recipeType() {
        return recipeType;
    }

    @Override
    public long getBaseRecipeEu() {
        return MachineTier.MULTIBLOCK.getBaseEu();
    }

    @Override
    public long getMaxRecipeEu() {
        return MachineTier.MULTIBLOCK.getMaxEu() + upgrades.getAddMaxEUPerTick();
    }

    @Override
    public boolean isOverdriving() {
        return overdrive.shouldOverdrive();
    }
}

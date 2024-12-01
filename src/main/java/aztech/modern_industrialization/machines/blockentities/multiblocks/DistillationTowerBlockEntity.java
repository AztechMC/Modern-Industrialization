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
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.stream.IntStream;

public class DistillationTowerBlockEntity extends AbstractElectricCraftingMultiblockBlockEntity implements EnergyListComponentHolder {
    private static final int MAX_HEIGHT = MIConfig.getConfig().maxDistillationTowerHeight;
    private static final ShapeTemplate[] shapeTemplates;

    public DistillationTowerBlockEntity(BEP bep) {
        super(bep, "distillation_tower", new OrientationComponent.Params(false, false, false), shapeTemplates);
        this.upgrades = new UpgradeComponent();
        this.overdrive = new OverdriveComponent();
        this.registerComponents(upgrades, overdrive);
        registerGuiComponent(new SlotPanel.Server(this)
                .withRedstoneControl(redstoneControl)
                .withUpgrades(upgrades)
                .withOverdrive(overdrive));

        registerGuiComponent(new ShapeSelection.Server(new ShapeSelection.Behavior() {
            @Override
            public void handleClick(int clickedLine, int delta) {
                activeShape.incrementShape(DistillationTowerBlockEntity.this, delta);
            }

            @Override
            public int getCurrentIndex(int line) {
                return activeShape.getActiveShapeIndex();
            }
        }, new ShapeSelection.LineInfo(
                MAX_HEIGHT,
                IntStream.range(1, MAX_HEIGHT + 1).mapToObj(MIText.ShapeTextHeight::text).toList(),
                false)));
    }

    private final UpgradeComponent upgrades;
    private final OverdriveComponent overdrive;

    @Override
    public MachineRecipeType recipeType() {
        return MIMachineRecipeTypes.DISTILLATION_TOWER;
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

    @Override
    public int getMaxFluidOutputs() {
        return activeShape.getActiveShapeIndex() + 1;
    }

    public static void registerReiShapes() {
        for (int i = 0; i < shapeTemplates.length; ++i) {
            ReiMachineRecipes.registerMultiblockShape("distillation_tower", shapeTemplates[i], "" + i);
        }
    }

    static {
        shapeTemplates = new ShapeTemplate[MAX_HEIGHT];
        for (int i = 0; i < MAX_HEIGHT; ++i) {
            ShapeTemplate towerShape = new ShapeTemplate.Structure(MI.id("distillation_tower_%d".formatted(i + 1))).build();
            shapeTemplates[i] = towerShape;
        }
    }
}

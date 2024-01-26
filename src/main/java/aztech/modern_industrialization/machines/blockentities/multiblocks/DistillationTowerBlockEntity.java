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

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.stream.IntStream;

public class DistillationTowerBlockEntity extends AbstractElectricCraftingMultiblockBlockEntity implements EnergyListComponentHolder {
    private static final int MAX_HEIGHT = MIConfig.getConfig().maxDistillationTowerHeight;
    private static final ShapeTemplate[] shapeTemplates;

    public DistillationTowerBlockEntity(BEP bep) {
        super(bep, "distillation_tower", new OrientationComponent.Params(false, false, false), shapeTemplates);
        this.upgrades = new UpgradeComponent();
        this.registerComponents(upgrades);
        registerGuiComponent(new SlotPanel.Server(this)
                .withRedstoneControl(redstoneControl)
                .withUpgrades(upgrades));

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
    public int getMaxFluidOutputs() {
        return activeShape.getActiveShapeIndex() + 1;
    }

    public static void registerReiShapes() {
        for (ShapeTemplate shapeTemplate : shapeTemplates) {
            ReiMachineRecipes.registerMultiblockShape("distillation_tower", shapeTemplate);
        }
    }

    static {
        shapeTemplates = new ShapeTemplate[MAX_HEIGHT];

        SimpleMember casing = SimpleMember.forBlock(MIBlock.BLOCK_DEFINITIONS.get(new MIIdentifier("clean_stainless_steel_machine_casing")));
        SimpleMember pipe = SimpleMember.forBlock(MIBlock.BLOCK_DEFINITIONS.get(new MIIdentifier("stainless_steel_machine_casing_pipe")));
        HatchFlags bottom = new HatchFlags.Builder().with(HatchType.ENERGY_INPUT, HatchType.FLUID_INPUT).build();
        HatchFlags layer = new HatchFlags.Builder().with(HatchType.FLUID_OUTPUT).build();
        for (int i = 0; i < MAX_HEIGHT; ++i) {
            ShapeTemplate.Builder builder = new ShapeTemplate.Builder(MachineCasings.CLEAN_STAINLESS_STEEL);
            for (int y = 0; y <= i + 1; ++y) {
                builder.add3by3(y, casing, y != 0, y == 0 ? bottom : layer);
                if (y != 0) {
                    builder.add(0, y, 1, pipe, null);

                }
            }
            shapeTemplates[i] = builder.build();
        }
    }
}

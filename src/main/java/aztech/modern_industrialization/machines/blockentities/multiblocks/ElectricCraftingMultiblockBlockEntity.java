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

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Simulation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ElectricCraftingMultiblockBlockEntity extends AbstractCraftingMultiblockBlockEntity {
    public ElectricCraftingMultiblockBlockEntity(BEP bep, String name, ShapeTemplate shapeTemplate, MachineRecipeType recipeType) {
        super(bep, name, new OrientationComponent.Params(false, false, false), new ShapeTemplate[] { shapeTemplate });
        this.recipeType = recipeType;
        this.upgrades = new UpgradeComponent();
        this.registerComponents(upgrades);
    }

    @Override
    protected CrafterComponent.Behavior getBehavior() {
        return new Behavior();
    }

    private final List<EnergyComponent> energyInputs = new ArrayList<>();
    private final MachineRecipeType recipeType;
    private final UpgradeComponent upgrades;

    @Override
    protected void onSuccessfulMatch(ShapeMatcher shapeMatcher) {
        energyInputs.clear();
        for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
            hatch.appendEnergyInputs(energyInputs);
        }
    }

    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        ActionResult result = super.onUse(player, hand, face);
        if (!result.isAccepted()) {
            result = upgrades.onUse(this, player, hand);
        }
        if (!result.isAccepted()) {
            result = LubricantHelper.onUse(this.crafter, player, hand);
        }
        return result;
    }

    @Override
    public List<ItemStack> dropExtra() {
        List<ItemStack> drops = super.dropExtra();
        drops.add(upgrades.getDrop());
        return drops;
    }

    private class Behavior implements CrafterComponent.Behavior {
        @Override
        public long consumeEu(long max, Simulation simulation) {
            long total = 0;

            for (EnergyComponent energyComponent : energyInputs) {
                total += energyComponent.consumeEu(max - total, simulation);
            }

            return total;
        }

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
        public World getCrafterWorld() {
            return world;
        }
    }

}

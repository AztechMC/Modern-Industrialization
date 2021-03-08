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
package aztech.modern_industrialization.machinesv2.blockentities.multiblocks;

import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.components.CrafterComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.helper.SteamHelper;
import aztech.modern_industrialization.machinesv2.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.world.World;

public class SteamCraftingMultiblockBlockEntity extends AbstractCraftingMultiblockBlockEntity {
    public SteamCraftingMultiblockBlockEntity(BlockEntityType<?> type, String name, ShapeTemplate shapeTemplate, MachineRecipeType recipeType) {
        super(type, name, new OrientationComponent(new OrientationComponent.Params(false, false, false)), new ShapeTemplate[] { shapeTemplate });

        this.recipeType = recipeType;
    }

    @Override
    protected CrafterComponent.Behavior getBehavior() {
        return new Behavior();
    }

    private final MachineRecipeType recipeType;
    private boolean steelTier;

    @Override
    protected void onSuccessfulMatch(ShapeMatcher shapeMatcher) {
        steelTier = false;

        for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
            if (hatch.upgradesToSteel()) {
                steelTier = true;
            }
        }
    }

    private class Behavior implements CrafterComponent.Behavior {
        @Override
        public long consumeEu(long max, Simulation simulation) {
            return SteamHelper.consumeSteamEu(inventory.getFluidInputs(), max, simulation);
        }

        @Override
        public MachineRecipeType recipeType() {
            return recipeType;
        }

        @Override
        public long getBaseRecipeEu() {
            return steelTier ? 4 : 2;
        }

        @Override
        public long getMaxRecipeEu() {
            return getBaseRecipeEu();
        }

        @Override
        public World getWorld() {
            return world;
        }
    }
}

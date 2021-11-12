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
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.components.GunpowderOverclockComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.helper.SteamHelper;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SteamCraftingMultiblockBlockEntity extends AbstractCraftingMultiblockBlockEntity {

    private final GunpowderOverclockComponent gunpowderOverclock;

    public SteamCraftingMultiblockBlockEntity(BEP bep, String name, ShapeTemplate shapeTemplate, MachineRecipeType recipeType) {
        super(bep, name, new OrientationComponent.Params(false, false, false), new ShapeTemplate[] { shapeTemplate });

        this.recipeType = recipeType;
        gunpowderOverclock = new GunpowderOverclockComponent();
        this.registerComponents(gunpowderOverclock);
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
            return (steelTier ? 4 : 2) * (gunpowderOverclock.isOverclocked() ? 2 : 1);
        }

        @Override
        public long getMaxRecipeEu() {
            return getBaseRecipeEu();
        }

        @Override
        public World getCrafterWorld() {
            return world;
        }
    }

    public final void tickExtra() {
        gunpowderOverclock.tick(this);
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        ActionResult result = super.onUse(player, hand, face);
        if (!result.isAccepted()) {
            return gunpowderOverclock.onUse(this, player, hand);
        }
        return result;
    }
}

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
package aztech.modern_industrialization.compat.rei.forgehammer_recipe;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.machinesv2.recipe.MachineRecipe;
import aztech.modern_industrialization.machinesv2.recipe.MachineRecipeType;
import java.util.function.Predicate;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

public class ForgeHammerRecipePlugin implements REIPluginV0 {
    private MachineRecipeType[] RECIPE_TYPES = new MachineRecipeType[] { ForgeHammerScreenHandler.RECIPE_HAMMER,
            ForgeHammerScreenHandler.RECIPE_SAW };

    @Override
    public Identifier getPluginIdentifier() {
        return new MIIdentifier("forge_hammer_recipe");
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        for (MachineRecipeType type : RECIPE_TYPES) {
            recipeHelper.registerCategory(new ForgeHammerRecipeCategory(type, type == RECIPE_TYPES[0]));
        }
    }

    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        for (MachineRecipeType type : RECIPE_TYPES) {
            recipeHelper.registerRecipes(type.getId(),
                    (Predicate<Recipe>) recipe -> recipe instanceof MachineRecipe && ((MachineRecipe) recipe).getType() == type,
                    recipe -> new ForgeHammerRecipeDisplay(type, (MachineRecipe) recipe));
        }
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        for (MachineRecipeType type : RECIPE_TYPES) {
            recipeHelper.registerWorkingStations(type.getId(), EntryStack.create(ModernIndustrialization.ITEM_FORGE_HAMMER));
            recipeHelper.removeAutoCraftButton(type.getId());
        }

        recipeHelper.registerContainerClickArea(new Rectangle(71, 48, 53, 15), ForgeHammerScreen.class, RECIPE_TYPES[0].getId(),
                RECIPE_TYPES[1].getId());
    }
}

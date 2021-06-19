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

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class ForgeHammerRecipePlugin implements REIClientPlugin {
    private MachineRecipeType[] RECIPE_TYPES = new MachineRecipeType[] { ForgeHammerScreenHandler.RECIPE_HAMMER,
            ForgeHammerScreenHandler.RECIPE_SAW };

    @Override
    public void registerCategories(CategoryRegistry registry) {
        for (MachineRecipeType type : RECIPE_TYPES) {
            registry.add(new ForgeHammerRecipeCategory(type, type == RECIPE_TYPES[0]));
            registry.addWorkstations(CategoryIdentifier.of(type.getId()), EntryStacks.of(ModernIndustrialization.ITEM_FORGE_HAMMER));
            registry.removePlusButton(CategoryIdentifier.of(type.getId()));
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (MachineRecipeType type : RECIPE_TYPES) {
            registry.registerFiller(MachineRecipe.class, recipe -> recipe.getType() == type, recipe -> new ForgeHammerRecipeDisplay(type, recipe));
        }
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerContainerClickArea(new Rectangle(71, 48, 53, 15), ForgeHammerScreen.class, CategoryIdentifier.of(RECIPE_TYPES[0].getId()),
                CategoryIdentifier.of(RECIPE_TYPES[1].getId()));
    }
}

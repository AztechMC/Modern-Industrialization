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

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class ForgeHammerRecipePlugin implements REIClientPlugin {

    static final CategoryIdentifier<ForgeHammerRecipeDisplay> CATEGORY = CategoryIdentifier.of(new MIIdentifier("forge_hammer"));

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ForgeHammerRecipeCategory());
        registry.addWorkstations(CATEGORY, EntryStacks.of(MIBlock.FORGE_HAMMER.asItem()));
        registry.removePlusButton(CATEGORY);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(MachineRecipe.class, recipe -> recipe.getType() == MIMachineRecipeTypes.FORGE_HAMMER,
                recipe -> new ForgeHammerRecipeDisplay(recipe));
    }

}

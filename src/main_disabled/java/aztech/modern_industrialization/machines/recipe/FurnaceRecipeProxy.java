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
package aztech.modern_industrialization.machines.recipe;

import static aztech.modern_industrialization.ModernIndustrialization.MOD_ID;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

public class FurnaceRecipeProxy extends ProxyableMachineRecipeType {
    public FurnaceRecipeProxy(ResourceLocation id) {
        super(id);
    }

    protected void fillRecipeList(Level world) {
        Map<ResourceLocation, MachineRecipe> recipes = new HashMap<>();

        for (SmeltingRecipe smeltingRecipe : world.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            MachineRecipe recipe = RecipeConversions.of(smeltingRecipe, this, world.registryAccess());
            recipes.put(recipe.id, recipe);
        }

        recipeList = new ArrayList<>(recipes.values());
        recipeList.sort(Comparator.comparing(r -> r.getId().getNamespace().equals(MOD_ID) ? 0 : 1));
    }
}

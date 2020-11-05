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
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class FurnaceRecipeProxy extends MachineRecipeType {
    public FurnaceRecipeProxy(Identifier id) {
        super(id);
    }

    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 20 * 1000;

    private final Map<Identifier, MachineRecipe> cachedRecipes = new HashMap<>();
    private List<MachineRecipe> sortedRecipes;

    private void buildCachedRecipes(ServerWorld world) {
        cachedRecipes.clear();
        for (SmeltingRecipe smeltingRecipe : world.getRecipeManager().listAllOfType(RecipeType.SMELTING)) {
            Ingredient ingredient = smeltingRecipe.getPreviewInputs().get(0);
            Identifier id = new Identifier(smeltingRecipe.getId().getNamespace(), smeltingRecipe.getId().getPath() + "_exported_mi_furnace");
            MachineRecipe recipe = new MachineRecipe(id, this);
            recipe.eu = 2;
            recipe.duration = smeltingRecipe.getCookTime();
            recipe.itemInputs = Collections.singletonList(new MachineRecipe.ItemInput(ingredient, 1, 1));
            recipe.fluidInputs = Collections.emptyList();
            recipe.itemOutputs = Collections.singletonList(new MachineRecipe.ItemOutput(smeltingRecipe.getOutput().getItem(), 1, 1));
            recipe.fluidOutputs = Collections.emptyList();
            cachedRecipes.put(id, recipe);
        }

        sortedRecipes = new ArrayList<>(cachedRecipes.values());
        sortedRecipes.sort(Comparator.comparing(r -> r.getId().getNamespace().equals(MOD_ID) ? 0 : 1));
    }

    @Override
    public Collection<MachineRecipe> getRecipes(ServerWorld world) {
        long time = System.currentTimeMillis();
        if (time - lastUpdate > UPDATE_INTERVAL) {
            lastUpdate = time;
            buildCachedRecipes(world);
        }
        return sortedRecipes;
    }
}

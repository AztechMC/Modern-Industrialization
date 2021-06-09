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

import java.util.Collections;
import net.minecraft.fluid.Fluids;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.Identifier;

public class RecipeConversions {
    public static MachineRecipe of(SmeltingRecipe smeltingRecipe, MachineRecipeType type) {
        Ingredient ingredient = smeltingRecipe.getIngredients().get(0);
        Identifier id = new Identifier(smeltingRecipe.getId().getNamespace(), smeltingRecipe.getId().getPath() + "_exported_mi_furnace");
        MachineRecipe recipe = new MachineRecipe(id, type);
        recipe.eu = 2;
        recipe.duration = smeltingRecipe.getCookTime();
        recipe.itemInputs = Collections.singletonList(new MachineRecipe.ItemInput(ingredient, 1, 1));
        recipe.fluidInputs = Collections.emptyList();
        recipe.itemOutputs = Collections.singletonList(new MachineRecipe.ItemOutput(smeltingRecipe.getOutput().getItem(), 1, 1));
        recipe.fluidOutputs = Collections.emptyList();
        return recipe;
    }

    public static MachineRecipe of(StonecuttingRecipe stonecuttingRecipe, MachineRecipeType type) {
        Identifier id = new Identifier(stonecuttingRecipe.getId().getNamespace(),
                stonecuttingRecipe.getId().getPath() + "_exported_mi_cutting_machine");
        MachineRecipe recipe = new MachineRecipe(id, type);
        recipe.eu = 2;
        recipe.duration = 200;
        recipe.itemInputs = Collections.singletonList(new MachineRecipe.ItemInput(stonecuttingRecipe.getIngredients().get(0), 1, 1));
        recipe.fluidInputs = Collections.singletonList(new MachineRecipe.FluidInput(Fluids.WATER, 81, 1));
        recipe.itemOutputs = Collections
                .singletonList(new MachineRecipe.ItemOutput(stonecuttingRecipe.getOutput().getItem(), stonecuttingRecipe.getOutput().getCount(), 1));
        recipe.fluidOutputs = Collections.emptyList();
        return recipe;
    }
}

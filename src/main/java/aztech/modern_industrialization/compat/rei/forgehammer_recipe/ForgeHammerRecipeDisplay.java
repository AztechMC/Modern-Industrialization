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

import aztech.modern_industrialization.compat.rei.ReiUtil;
import aztech.modern_industrialization.items.ForgeTool;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ForgeHammerRecipeDisplay implements Display {

    public final MachineRecipe recipe;

    public ForgeHammerRecipeDisplay(MachineRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {

        MachineRecipe.ItemInput input = recipe.itemInputs.get(0);
        List<EntryIngredient> inputs = new ArrayList<>();
        inputs.add(ReiUtil.createInputEntries(input));

        if (recipe.eu > 0) {
            EntryIngredient tools = EntryIngredients.ofIngredient(Ingredient.of(ForgeTool.TAG));
            inputs.add(tools);
        }
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        MachineRecipe.ItemOutput output = recipe.itemOutputs.get(0);
        return Collections.singletonList(EntryIngredient.of(EntryStacks.of(new ItemStack(output.item, output.amount))));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return (ForgeHammerRecipePlugin.CATEGORY);
    }
}

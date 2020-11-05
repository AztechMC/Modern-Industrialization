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

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ForgeHammerRecipeDisplay implements RecipeDisplay {
    private final MachineRecipe recipe;
    private final Identifier category;

    public ForgeHammerRecipeDisplay(MachineRecipeType type, MachineRecipe recipe) {
        this.recipe = recipe;
        this.category = type.getId();
    }

    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        MachineRecipe.ItemInput input = recipe.itemInputs.get(0);
        return Collections.singletonList(createInputEntries(input));
    }

    private static List<EntryStack> createInputEntries(MachineRecipe.ItemInput input) {
        return input.getInputItems().stream().map(i -> EntryStack.create(new ItemStack(i, input.amount))).collect(Collectors.toList());
    }

    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        MachineRecipe.ItemOutput output = recipe.itemOutputs.get(0);
        return Collections.singletonList(Collections.singletonList(EntryStack.create(new ItemStack(output.item, output.amount))));
    }

    @Override
    public @NotNull Identifier getRecipeCategory() {
        return category;
    }
}

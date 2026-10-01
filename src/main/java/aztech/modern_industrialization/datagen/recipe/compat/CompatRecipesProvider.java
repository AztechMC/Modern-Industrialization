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

package aztech.modern_industrialization.datagen.recipe.compat;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.datagen.recipe.MIRecipesProvider;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.recipe.json.MIRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public abstract class CompatRecipesProvider extends MIRecipesProvider {
    private RecipeOutput consumer;
    private final String compatModId;
    protected ICondition[] conditions = null;

    public CompatRecipesProvider(PackOutput packOutput, String compatModId) {
        super(packOutput);
        this.compatModId = compatModId;
        conditions = new ICondition[] { new ModLoadedCondition(compatModId) };
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        this.consumer = consumer;
        generate();
    }

    protected abstract void generate();

    protected final String formatRecipePath(String input, String output) {
        return "%s_to_%s".formatted(input.replace('#', '_').replace(':', '_').replace('/', '_'), output.replace(':', '_'));
    }

    protected final void addCuttingRecipe(String input, String output, int outputAmount) {
        addCompatRecipe("cutting_machine/%s".formatted(formatRecipePath(input, output)), new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 200)
                .addItemInput(input, 1)
                .addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemOutput(output, outputAmount));
    }

    protected final void addMiRecipe(MachineRecipeType machine, String input, String output, int outputAmount) {
        addMiRecipe(machine, input, output, outputAmount, 2, 200);
    }

    protected final void addMiRecipe(MachineRecipeType machine, String input, String output, int outputAmount, int eu, int duration) {
        addMiRecipe(machine, input, 1, output, outputAmount, eu, duration);
    }

    protected final void addMiRecipe(MachineRecipeType machine, String input, int inputAmount, String output, int outputAmount, int eu, int duration) {
        String id = "%s/%s".formatted(machine.getPath(), formatRecipePath(input, output));
        addCompatRecipe(id, new MachineRecipeBuilder(machine, eu, duration).addItemInput(input, inputAmount).addItemOutput(output, outputAmount));
    }

    protected final void addCompatRecipe(String id, MIRecipeBuilder recipeJson) {
        id = "compat/%s/%s".formatted(compatModId, id);
        recipeJson.offerTo(consumer.withConditions(conditions), id);
    }
}

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

package aztech.modern_industrialization.datagen.recipe;

import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

public class AlloyRecipeProvider extends MIRecipeProvider {
    protected AlloyRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        new AlloyBuilder("bronze").addIngredient("tin", 1).addIngredient("copper", 3).Build(output);
        new AlloyBuilder("battery_alloy").addIngredient("lead", 1).addIngredient("antimony", 1).Build(output);
        new AlloyBuilder("cupronickel").addIngredient("copper", 1).addIngredient("nickel", 1).Build(output);
        new AlloyBuilder("invar").addIngredient("iron", 2).addIngredient("nickel", 1).Build(output);
        new AlloyBuilder("electrum").addIngredient("gold", 1).addIngredient("silver", 1).Build(output);
        new AlloyBuilder("stainless_steel").addIngredient("iron", 6).addIngredient("chromium", 1).addIngredient("nickel", 1)
                .addIngredient("manganese", 1).Build(output);
        new AlloyBuilder("kanthal").addIngredient("stainless_steel", 1).addIngredient("chromium", 1).addIngredient("aluminum", 1).Build(output);
        new AlloyBuilder("soldering_alloy").addIngredient("tin", 1).addIngredient("lead", 1).Build(output);

        new AlloyBuilder("le_uranium").addIngredient("uranium_238", 8).addIngredient("uranium_235", 1).Build(output);
        new AlloyBuilder("he_uranium").addIngredient("uranium_238", 6).addIngredient("uranium_235", 3).Build(output);

        new AlloyBuilder("le_mox").addIngredient("uranium_238", 8).addIngredient("plutonium", 1).Build(output);
        new AlloyBuilder("he_mox").addIngredient("uranium_238", 6).addIngredient("plutonium", 3).Build(output);

        new AlloyBuilder("superconductor").addIngredient("iridium", 1).addIngredient("annealed_copper", 3).addIngredient("yttrium", 3)
                .addIngredient("neodymium", 2).Build(output);
    }

    public class AlloyBuilder {
        public final String output;
        private int totalAmount;
        private final List<String> ingredients = new ArrayList<>();
        private final List<Integer> ingredientAmounts = new ArrayList<>();

        public AlloyBuilder(String output) {
            this.output = output;
        }

        public AlloyBuilder addIngredient(String ingredient, int amount) {
            if (totalAmount + amount <= 9) {
                if (!ingredients.contains(ingredient)) {
                    totalAmount += amount;
                    ingredients.add(ingredient);
                    ingredientAmounts.add(amount);

                } else {
                    throw new IllegalArgumentException("Trying to add the same ingredient in alloy twice : " + ingredient);
                }

            } else {
                throw new IllegalArgumentException("Alloy with more than 9 dusts");
            }
            return this;
        }

        public void Build(RecipeOutput recipeOutput) {
            MachineRecipeBuilder dusts = new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100);
            dusts.addItemOutput("modern_industrialization:" + output + "_dust", totalAmount);

            MachineRecipeBuilder tinyDusts = new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100);
            tinyDusts.addItemOutput("modern_industrialization:" + output + "_tiny_dust", totalAmount);

            for (int i = 0; i < ingredients.size(); i++) {
                int n = ingredientAmounts.get(i);
                dusts.addItemInput(conventionTag("dusts/" + ingredients.get(i)), n);
                tinyDusts.addItemInput(conventionTag("tiny_dusts/" + ingredients.get(i)), n);
            }

            dusts.offerTo(recipeOutput, "alloy/mixer/" + output + "/dust");
            tinyDusts.offerTo(recipeOutput, "alloy/mixer/" + output + "/tiny_dust");
        }
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new AlloyRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Alloy Recipes";
        }
    }
}

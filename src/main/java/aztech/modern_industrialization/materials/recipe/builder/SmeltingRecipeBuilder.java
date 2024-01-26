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
package aztech.modern_industrialization.materials.recipe.builder;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.part.PartKeyProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration" })
public class SmeltingRecipeBuilder implements MaterialRecipeBuilder {
    public final String recipeId;
    private final MaterialBuilder.RecipeContext context;
    private boolean canceled = false;
    @Nullable
    private final AbstractCookingRecipe smeltingRecipe;

    public static void smeltAndBlast(MaterialBuilder.RecipeContext context, PartKeyProvider inputPart, PartKeyProvider outputPart,
            float experience) {
        if (context.hasInternalPart(inputPart) || context.hasInternalPart(outputPart)) {
            // Skip if both are external, vanilla already has the recipe
            new SmeltingRecipeBuilder(context, inputPart, outputPart, experience, false);
            new SmeltingRecipeBuilder(context, inputPart, outputPart, experience, true);
        }
    }

    public SmeltingRecipeBuilder(MaterialBuilder.RecipeContext context, PartKeyProvider inputPart, PartKeyProvider outputPart, int cookingtime,
            float experience,
            boolean blasting) {
        if (blasting) {
            this.recipeId = "smelting/" + inputPart.key() + "_to_" + outputPart.key() + "_blasting";
        } else {
            this.recipeId = "smelting/" + inputPart.key() + "_to_" + outputPart.key() + "_smelting";
        }

        this.context = context;
        var input = context.getPart(inputPart);
        var output = context.getPart(outputPart);
        if (input == null || output == null) {
            canceled = true;
            this.smeltingRecipe = null;
        } else {
            AbstractCookingRecipe.Factory<?> factory = blasting ? BlastingRecipe::new : SmeltingRecipe::new;
            this.smeltingRecipe = factory.create(
                    "",
                    CookingBookCategory.MISC,
                    Ingredient.of(input.asItem()),
                    output.asItem().getDefaultInstance(),
                    experience,
                    cookingtime);
            context.addRecipe(this);
        }
    }

    public SmeltingRecipeBuilder(MaterialBuilder.RecipeContext context, PartKeyProvider partInput, PartKeyProvider partOutput, float experience,
            boolean blasting) {
        this(context, partInput, partOutput, blasting ? 100 : 200, experience, blasting);
    }

    @Override
    public String getRecipeId() {
        return recipeId;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void save(RecipeOutput recipeOutput) {
        if (!canceled) {
            String fullId = "materials/" + context.getMaterialName() + "/" + recipeId;
            recipeOutput.accept(MI.id(fullId), smeltingRecipe, null);
        }
    }
}

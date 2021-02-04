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

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.materials.MaterialBuilder;
import com.google.gson.Gson;
import net.minecraft.util.Identifier;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration" })
public class SmeltingRecipeBuilder implements MaterialRecipeBuilder {
    private static final transient Gson GSON = new Gson();

    public final transient String recipeId;
    private final transient MaterialBuilder.RecipeContext context;
    private transient boolean canceled = false;
    private final String type;
    private final int cookingtime;
    private final double experience;
    private final Ingredient ingredient;
    private final String result;

    public static class Ingredient {
        String item;
    }

    @SuppressWarnings("ConstantConditions")
    public SmeltingRecipeBuilder(MaterialBuilder.RecipeContext context, String inputPart, String outputPart, int cookingtime, double experience,
            boolean blasting) {
        if (blasting) {
            this.type = "minecraft:blasting";
            this.recipeId = "smelting/" + inputPart + "_blasting";
        } else {
            this.type = "minecraft:smelting";
            this.recipeId = "smelting/" + inputPart + "_smelting";
        }
        this.cookingtime = cookingtime;
        this.experience = experience;

        this.context = context;
        if (context.getPart(inputPart) == null || context.getPart(outputPart) == null) {
            this.ingredient = null;
            this.result = null;
            canceled = true;
        } else {
            this.ingredient = new Ingredient();
            this.ingredient.item = context.getPart(inputPart).getItemId();
            this.result = context.getPart(outputPart).getItemId();
            context.addRecipe(this);
        }
    }

    public SmeltingRecipeBuilder(MaterialBuilder.RecipeContext context, String partInput, String partOutput, double experience, boolean blasting) {
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

    @Override
    public void save() {
        if (!canceled) {
            String fullId = "modern_industrialization:recipes/generated/materials/" + context.getMaterialName() + "/" + recipeId + ".json";
            String json = GSON.toJson(this);
            ModernIndustrialization.RESOURCE_PACK.addData(new Identifier(fullId), json.getBytes());
        }
    }
}

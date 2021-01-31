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
package aztech.modern_industrialization.materials.recipe;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.part.MaterialPart;
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

    public SmeltingRecipeBuilder(MaterialBuilder.RecipeContext context, String partInput, String partOutput, int cookingtime, double experience,
            boolean blasting) {
        if (blasting) {
            this.type = "minecraft:blasting";
            this.recipeId = "smelting/" + partInput + "_blasting";
        } else {
            this.type = "minecraft:smelting";
            this.recipeId = "smelting/" + partInput + "_smelting";
        }
        this.cookingtime = cookingtime;
        this.experience = experience;

        this.context = context;
        MaterialPart part_output = context.getPart(partOutput);
        if (part_output != null) {
            this.result = part_output.getItemId();

            MaterialPart part_input = context.getPart(partInput);
            if (part_input != null) {
                this.ingredient = new Ingredient();
                this.ingredient.item = part_input.getItemId();
                context.addRecipe(this);
            } else {
                this.ingredient = null;
                cancel();
            }

        } else {
            this.ingredient = null;
            this.result = "";
            cancel();
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

    @Override
    public void save() {
        if (!canceled) {
            String fullId = "modern_industrialization:recipes/generated/materials/" + context.getMaterialName() + "/" + recipeId + ".json";
            String json = GSON.toJson(this);
            ModernIndustrialization.RESOURCE_PACK.addData(new Identifier(fullId), json.getBytes());
        }
    }
}

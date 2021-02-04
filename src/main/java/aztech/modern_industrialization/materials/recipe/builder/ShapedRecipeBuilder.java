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
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import com.google.gson.Gson;
import java.util.Map;
import net.minecraft.util.Identifier;

public class ShapedRecipeBuilder implements MaterialRecipeBuilder {
    private static final Gson GSON = new Gson();

    public final String recipeId;
    private final MaterialBuilder.RecipeContext context;
    private boolean canceled = false;
    private final String id;
    private final ShapedRecipeJson json;

    public ShapedRecipeBuilder(MaterialBuilder.RecipeContext context, String result, int count, String id, String... pattern) {
        this.recipeId = "craft/" + id;
        this.context = context;
        this.id = id;
        if (context.getPart(result) == null) {
            this.json = null;
            canceled = true;
        } else {
            this.json = new ShapedRecipeJson(context.getPart(result).getItemId(), count, pattern);
        }
        context.addRecipe(this);
    }

    public ShapedRecipeBuilder addPart(char key, String part) {
        if (context.getPart(part) != null) {
            addInput(key, context.getPart(part).getItemId());
        } else {
            canceled = true;
        }
        return this;
    }

    public ShapedRecipeBuilder addTaggedPart(char key, String part) {
        if (context.getPart(part) != null) {
            addInput(key, context.getPart(part).getTaggedItemId());
        } else {
            canceled = true;
        }
        return this;
    }

    public ShapedRecipeBuilder addInput(char key, String maybeTag) {
        if (!canceled) {
            json.addInput(key, maybeTag);
        }
        return this;
    }

    public ShapedRecipeBuilder exportToAssembler(int eu, int duration) {
        return exportToMachine("assembler", eu, duration, 1);
    }

    public ShapedRecipeBuilder exportToAssembler() {
        return exportToAssembler(8, 200);
    }

    public ShapedRecipeBuilder exportToMachine(String machine, int eu, int duration, int division) {
        if (canceled) {
            return this;
        }

        if (json.result.count % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }

        MIRecipeBuilder assemblerRecipe = new MIRecipeBuilder(context, machine, id, eu, duration).addPartOutput(json.result.item,
                json.result.count / division);
        for (Map.Entry<Character, ShapedRecipeJson.ItemInput> entry : json.key.entrySet()) {
            int count = 0;
            for (String row : json.pattern) {
                for (char c : row.toCharArray()) {
                    if (c == entry.getKey()) {
                        count++;
                    }
                }
            }

            if (count % division != 0) {
                throw new IllegalArgumentException("Input must be divisible by division");
            }

            ShapedRecipeJson.ItemInput input = entry.getValue();
            if (input.item != null) {
                assemblerRecipe.addItemInput(input.item, count / division);
            } else if (input.tag != null) {
                assemblerRecipe.addItemInput("#" + input.tag, count / division);
            }
        }

        return this;
    }

    public ShapedRecipeBuilder exportToMachine(String machine) {
        return exportToMachine(machine, 2, 200, 1);
    }

    public ShapedRecipeBuilder exportToMachine(String machine, int division) {
        return exportToMachine(machine, 2, 200, division);
    }

    public boolean isCanceled() {
        return canceled;
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
            json.validate();
            String fullId = "modern_industrialization:recipes/generated/materials/" + context.getMaterialName() + "/" + recipeId + ".json";
            String json = GSON.toJson(this.json);
            ModernIndustrialization.RESOURCE_PACK.addData(new Identifier(fullId), json.getBytes());
        }
    }
}

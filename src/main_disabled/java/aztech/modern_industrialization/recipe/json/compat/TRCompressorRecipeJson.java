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
package aztech.modern_industrialization.recipe.json.compat;

import aztech.modern_industrialization.materials.property.MaterialHardness;
import aztech.modern_industrialization.recipe.json.RecipeJson;

@SuppressWarnings({ "unused", "FieldCanBeLocal" })
public class TRCompressorRecipeJson extends RecipeJson {
    private final String type = "techreborn:compressor";
    private final int power = 10;
    private int time = 300;
    private final TagIngredient[] ingredients;
    private final ItemResult[] results;

    public TRCompressorRecipeJson(String inputTag, String outputItem) {
        this.ingredients = new TagIngredient[] { new TagIngredient() };
        this.ingredients[0].tag = inputTag;
        this.results = new ItemResult[] { new ItemResult() };
        this.results[0].item = outputItem;
    }

    public TRCompressorRecipeJson scaleTime(MaterialHardness hardness) {
        this.time = (int) (this.time * hardness.timeFactor);
        return this;
    }

    private static class TagIngredient {
        private String tag;
    }

    private static class ItemResult {
        private String item;
    }
}

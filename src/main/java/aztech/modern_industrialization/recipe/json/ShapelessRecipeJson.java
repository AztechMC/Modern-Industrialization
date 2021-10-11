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
package aztech.modern_industrialization.recipe.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapelessRecipeJson implements RecipeJson {

    @SuppressWarnings("unused")
    private final String type;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final Result result;

    public ShapelessRecipeJson(String result, int count) {
        this.type = "minecraft:crafting_shapeless";
        this.result = new Result();
        this.result.item = result;
        this.result.count = count;
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public static class Ingredient {
        public String item;
        public String tag;

    }

    private static class Result {
        String item;
        int count;
    }

    public MIRecipeJson exportToMachine(String machine, int eu, int duration, int division) {
        if (result.count % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }

        MIRecipeJson machineJson = new MIRecipeJson(machine, eu, duration).addOutput(result.item, result.count / division);

        Map<String, Integer> tags = new HashMap<>();
        Map<String, Integer> items = new HashMap<>();

        for (Ingredient i : ingredients) {
            if (i.item != null) {
                items.put(i.item, items.getOrDefault(i.item, 0) + 1);
            } else if (i.tag != null) {
                tags.put(i.tag, tags.getOrDefault(i.tag, 0) + 1);
            }
        }
        for (String tag : tags.keySet()) {
            int count = tags.get(tag);
            if (count % division != 0) {
                throw new IllegalArgumentException("Input must be divisible by division");
            }
            machineJson.addItemInput("#" + tag, count / division);
        }

        for (String item : items.keySet()) {
            int count = items.get(item);
            if (count % division != 0) {
                throw new IllegalArgumentException("Input must be divisible by division");
            }
            machineJson.addItemInput(item, count / division);
        }

        return machineJson;
    }
}

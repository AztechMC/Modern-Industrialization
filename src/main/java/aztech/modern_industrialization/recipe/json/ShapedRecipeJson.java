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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration" })
public class ShapedRecipeJson implements IMIRecipeBuilder {
    public final String type = "minecraft:crafting_shaped";
    public final String[] pattern;
    public final Map<Character, Ingredient> key = new HashMap<>();
    public final ItemStack result;

    public ShapedRecipeJson(String resultItem, int count, String... pattern) {
        this.pattern = pattern;
        this.result = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(resultItem)), count);
    }

    public ShapedRecipeJson addInput(char key, String maybeTag) {
        Ingredient input;
        if (maybeTag.startsWith("#")) {
            input = Ingredient.of(ItemTags.create(new ResourceLocation(maybeTag.substring(1))));
        } else {
            input = Ingredient.of(BuiltInRegistries.ITEM.get(new ResourceLocation(maybeTag)));
        }
        if (this.key.put(key, input) != null) {
            throw new IllegalStateException("Key mapping is already registered: " + key);
        }
        return this;
    }

    public void validate() {
        // check pattern size
        if (pattern.length == 0 || pattern.length > 3) {
            throw new IllegalArgumentException("Invalid length " + pattern.length);
        }
        for (String string : pattern) {
            if (string.length() != pattern[0].length()) {
                throw new IllegalArgumentException("Pattern length mismatch: " + string.length() + ", expected " + pattern[0].length());
            }
        }
        // check mapping
        for (String string : pattern) {
            for (int i = 0; i < string.length(); ++i) {
                if (string.charAt(i) != ' ' && !key.containsKey(string.charAt(i))) {
                    throw new IllegalArgumentException("Key " + string.charAt(i) + " is missing a mapping.");
                }
            }
        }
        for (char c : key.keySet()) {
            boolean ok = false;
            for (String string : pattern) {
                for (int i = 0; i < string.length(); ++i) {
                    if (string.charAt(i) == c) {
                        ok = true;
                        break;
                    }
                }
            }
            if (!ok) {
                throw new IllegalArgumentException("Key mapping '" + c + "' is not used in the pattern.");
            }
        }
    }

    public MachineRecipeBuilder exportToAssembler() {
        return exportToMachine(MIMachineRecipeTypes.ASSEMBLER, 8, 200, 1);
    }

    public MachineRecipeBuilder exportToMachine(MachineRecipeType machine, int eu, int duration, int division) {
        if (result.getCount() % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }

        var assemblerJson = new MachineRecipeBuilder(machine, eu, duration).addItemOutput(result.getItem(), result.getCount() / division);
        for (Map.Entry<Character, Ingredient> entry : key.entrySet()) {
            int count = 0;
            for (String row : pattern) {
                for (char c : row.toCharArray()) {
                    if (c == entry.getKey()) {
                        count++;
                    }
                }
            }

            if (count % division != 0) {
                throw new IllegalArgumentException("Input must be divisible by division");
            }

            assemblerJson.addItemInput(entry.getValue(), count / division, 1);
        }

        return assemblerJson;
    }

    @Override
    public void offerTo(RecipeOutput recipeOutput, String path) {
        recipeOutput.accept(MI.id(path), new ShapedRecipe(
                "",
                CraftingBookCategory.MISC,
                ShapedRecipePattern.of(key, pattern),
                result), null);
    }
}

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration" })
public class SmeltingRecipeBuilder implements MaterialRecipeBuilder {
    private static final transient Gson GSON = new Gson();

    public final transient String recipeId;
    public final transient String part;
    private final transient MaterialBuilder.RecipeContext context;
    private transient boolean canceled = false;
    private final String type = "minecraft:crafting_shaped";
    private final Map<Character, ItemKey> key = new HashMap<>();
    private final ItemOutput result = new ItemOutput();
    private final List<String> pattern = new ArrayList<>();

    private static class ItemKey {
        String item;
        String tag;
    }

    private static class ItemOutput {
        String item;
        int count;
    }

    public SmeltingRecipeBuilder(MaterialBuilder.RecipeContext context, String part, int amount) {
        this.part = part;
        this.recipeId = "craft/" + part;
        this.context = context;
        MaterialPart part_output = context.getPart(part);
        if (part_output != null) {
            this.result.item = part_output.getItemId();
            this.result.count = amount;
            context.addRecipe(this);
        } else {
            cancel();
        }
    }

    public SmeltingRecipeBuilder setPattern(boolean tagPart, Object[]... elements) {
        int n = elements.length;
        if (n > 3) {
            throw new IllegalArgumentException("ShapedRecipe with more than 3 rows");
        }
        char current_element_key = 'a';

        for (int i = 0; i < n; i++) {
            Object[] element_row = elements[i];
            String pattern_row = "";
            int m = element_row.length;
            if (m > 3) {
                throw new IllegalArgumentException("ShapedRecipe with more than 3 columns");
            }
            for (int j = 0; j < m; j++) {
                Object element = element_row[j];
                if (element == "" || element == null) {
                    pattern_row += " ";
                    continue;
                }
                String element_str = "";
                boolean isTag = false;

                if (element instanceof String) {
                    element_str = ((String) element);
                    if (!element_str.contains(":")) {
                        MaterialPart part = context.getPart(element_str);
                        if (part != null) {
                            if (tagPart) {
                                isTag = true;
                                element_str = part.getTaggedItemId();
                            } else {
                                element_str = part.getItemId();
                            }
                        } else {
                            cancel();
                        }
                    } else {
                        if (element_str.startsWith("#")) {
                            isTag = true;
                            element_str = element_str.substring(1);
                        }
                    }
                } else if (element instanceof Item) {
                    element_str = Registry.ITEM.getId((Item) element).toString();
                }

                char current_key = 'z';

                for (Character char_key : key.keySet()) {
                    ItemKey itemKey = key.get(char_key);
                    if (itemKey.item != null && itemKey.item.equals(element_str) && !isTag) {
                        current_key = char_key;
                    } else if (itemKey.tag != null && itemKey.tag.equals(element_str) && isTag) {
                        current_key = char_key;
                    }
                }

                if (current_key == 'z') {
                    ItemKey itemKey = new ItemKey();
                    if (isTag) {
                        itemKey.tag = element_str;
                    } else {
                        itemKey.item = element_str;
                    }
                    key.put(current_element_key, itemKey);
                    pattern_row += current_element_key;
                    current_element_key += 1;
                } else {
                    pattern_row += current_key;
                }
            }
            pattern.add(pattern_row);
        }

        return this;
    }

    public SmeltingRecipeBuilder setPattern(Object[]... elements) {
        return setPattern(false, elements);
    }

    public SmeltingRecipeBuilder exportToAssembler(int eu, int duration) {
        return exportToMachine("assembler", eu, duration, 1);
    }

    public SmeltingRecipeBuilder exportToAssembler() {
        return exportToAssembler(8, 200);
    }

    public SmeltingRecipeBuilder exportToMachine(String machine, int eu, int duration, int division) {
        if (this.result.count % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }

        MIRecipeBuilder assemblerRecipe = new MIRecipeBuilder(context, machine, part, eu, duration).addPartOutput(this.part,
                this.result.count / division);
        for (Character char_key : key.keySet()) {

            int count = 0;
            for (String pattern_row : pattern) {
                for (char c : pattern_row.toCharArray()) {
                    if (c == char_key) {
                        count++;
                    }
                }
            }

            if (count % division != 0) {
                throw new IllegalArgumentException("Input must be divisible by division");
            }

            ItemKey itemKey = key.get(char_key);
            if (itemKey.item != null) {
                assemblerRecipe.addItemInput(itemKey.item, count / division);
            } else if (itemKey.tag != null) {
                assemblerRecipe.addItemInput("#" + itemKey.tag, count / division);
            }

        }

        return this;
    }

    public SmeltingRecipeBuilder exportToMachine(String machine) {
        return exportToMachine(machine, 2, 200, 1);
    }

    public SmeltingRecipeBuilder exportToMachine(String machine, int division) {
        return exportToMachine(machine, 2, 200, division);
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

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

import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration" })
public class ShapedRecipeJson extends RecipeJson {
    public final String type = "minecraft:crafting_shaped";
    public final String[] pattern;
    public final Map<Character, ItemInput> key = new HashMap<>();
    public final Result result;

    public static class ItemInput {
        public final String item;
        public final String tag;

        private ItemInput(String item, String tag) {
            this.item = item;
            this.tag = tag;
        }

        public static ItemInput withItem(String item) {
            return new ItemInput(item, null);
        }

        public static ItemInput withTag(String tag) {
            return new ItemInput(null, tag);
        }
    }

    public static class Result {
        public final String item;
        public int count;

        public Result(String item, int count) {
            this.item = item;
            this.count = count;
        }
    }

    public ShapedRecipeJson(String resultItem, int count, String... pattern) {
        this.pattern = pattern;
        this.result = new Result(resultItem, count);
    }

    public ShapedRecipeJson addInput(char key, String maybeTag) {
        ItemInput input;
        if (maybeTag.startsWith("#")) {
            input = ItemInput.withTag(maybeTag.substring(1));
        } else {
            input = ItemInput.withItem(maybeTag);
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

    public MIRecipeJson<?> exportToAssembler() {
        return exportToMachine(MIMachineRecipeTypes.ASSEMBLER, 8, 200, 1);
    }

    public MIRecipeJson<?> exportToMachine(MachineRecipeType machine, int eu, int duration, int division) {
        if (result.count % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }

        MIRecipeJson<?> assemblerJson = MIRecipeJson.create(machine, eu, duration).addItemOutput(result.item, result.count / division);
        for (Map.Entry<Character, ItemInput> entry : key.entrySet()) {
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

            ItemInput input = entry.getValue();
            if (input.item != null) {
                assemblerJson.addItemInput(input.item, count / division);
            } else if (input.tag != null) {
                assemblerJson.addItemInput("#" + input.tag, count / division);
            }
        }

        return assemblerJson;
    }
}

package aztech.modern_industrialization.recipe.json;

import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;
import aztech.modern_industrialization.materials.recipe.builder.ShapedRecipeBuilder;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration" })
public class ShapedRecipeJson {
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
                    }
                }
            }
            if (!ok) {
                throw new IllegalArgumentException("Key mapping '" + c + "' is not used in the pattern.");
            }
        }
    }

    public MIRecipeJson exportToMachine(String machine, int eu, int duration, int division) {
        if (result.count % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }

        MIRecipeJson assemblerJson = new MIRecipeJson(machine, eu, duration).addOutput(result.item, result.count / division);
        for (Map.Entry<Character, ShapedRecipeJson.ItemInput> entry : key.entrySet()) {
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

            ShapedRecipeJson.ItemInput input = entry.getValue();
            if (input.item != null) {
                assemblerJson.addItemInput(input.item, count / division);
            } else if (input.tag != null) {
                assemblerJson.addItemInput("#" + input.tag, count / division);
            }
        }

        return assemblerJson;
    }
}

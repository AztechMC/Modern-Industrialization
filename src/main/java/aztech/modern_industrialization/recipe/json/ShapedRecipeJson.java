package aztech.modern_industrialization.recipe.json;

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
        public final int count;

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
}

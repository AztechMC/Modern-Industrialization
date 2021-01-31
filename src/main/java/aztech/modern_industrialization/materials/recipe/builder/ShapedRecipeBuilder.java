package aztech.modern_industrialization.materials.recipe.builder;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.materials.MaterialBuilder;
import com.google.gson.Gson;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration" })
public class ShapedRecipeBuilder implements MaterialRecipeBuilder {
    private static final transient Gson GSON = new Gson();

    public final transient String recipeId;
    private final transient MaterialBuilder.RecipeContext context;
    private transient boolean canceled = false;
    private final transient String id;
    private final String type = "minecraft:crafting_shaped";
    private final String[] pattern;
    private final Map<Character, ItemInput> key;
    private final Result result;

    private static class ItemInput {
        String item;
        String tag;
    }

    private static class Result {
        String item;
        int count;
    }

    public ShapedRecipeBuilder(MaterialBuilder.RecipeContext context, String result, int count, String id, String... pattern) {
        this.recipeId = "craft/" + id;
        this.context = context;
        this.id = id;
        this.result = new Result();
        this.pattern = pattern;
        this.key = new HashMap<>();
        if (context.getPart(result) == null) {
            canceled = true;
        } else {
            this.result.item = context.getPart(result).getItemId();
            this.result.count = count;
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
        ItemInput input = new ItemInput();
        if (maybeTag.startsWith("#")) {
            input.tag = maybeTag.substring(1);
        } else {
            input.item = maybeTag;
        }
        if (this.key.put(key, input) != null) {
            throw new IllegalStateException("Key mapping is already registered: " + key);
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
        if (this.result.count % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }
        if (canceled) {
            return this;
        }

        MIRecipeBuilder assemblerRecipe = new MIRecipeBuilder(context, machine, id, eu, duration).addPartOutput(result.item,
                this.result.count / division);
        for (Map.Entry<Character, ItemInput> entry : this.key.entrySet()) {
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

    @Override
    public String getRecipeId() {
        return recipeId;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    private void validate() {
        try {
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
        } catch (Throwable throwable) {
            throw new RuntimeException("Couldn't build shaped recipe " + recipeId, throwable);
        }
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

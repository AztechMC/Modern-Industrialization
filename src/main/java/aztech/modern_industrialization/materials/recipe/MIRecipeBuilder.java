package aztech.modern_industrialization.materials.recipe;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.part.MaterialPart;
import com.google.gson.Gson;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration"})
public class MIRecipeBuilder implements MaterialRecipeBuilder {
    private static final transient Gson GSON = new Gson();

    public final transient String recipeId;
    private final transient MaterialBuilder.RecipeContext context;
    private transient boolean canceled = false;
    private final String type;
    private final int eu;
    private final int duration;
    private final List<MIItemInput> item_inputs = new ArrayList<>();
    private final List<MIItemOutput> item_outputs = new ArrayList<>();

    private static class MIItemInput {
        String item;
        String tag;
        int amount;
    }

    private static class MIItemOutput {
        String item;
        int amount;
    }

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, String type, String recipeSuffix, int eu, int duration) {
        this.recipeId = type + "/" + recipeSuffix;
        this.context = context;
        this.type = "modern_industrialization:" + type;
        this.eu = eu;
        this.duration = duration;
        context.addRecipe(this);
    }

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, String type, String recipeSuffix) {
        this(context, type, recipeSuffix, 2, 200);
    }

    public MIRecipeBuilder addPartInput(String part, int amount) {
        return addPartInput(context.getPart(part), amount);
    }

    public MIRecipeBuilder addTaggedPartInput(String part, int amount) {
        return addTaggedPartInput(context.getPart(part), amount);
    }

    // TODO: remove these two if the part is always a string passed through addPartInput
    public MIRecipeBuilder addPartInput(MaterialPart part, int amount) {
        if (part == null) {
            canceled = true;
        } else {
            addItemInput(part.getItemId(), amount);
        }
        return this;
    }

    public MIRecipeBuilder addTaggedPartInput(MaterialPart part, int amount) {
        if (part == null) {
            canceled = true;
        } else {
            addItemInput(part.getTaggedItemId(), amount);
        }
        return this;
    }

    /**
     * Also supports tags prefixed by #.
     */
    public MIRecipeBuilder addItemInput(String maybeTag, int amount) {
        MIItemInput input = new MIItemInput();
        input.amount = amount;
        if (maybeTag.startsWith("#")) {
            input.tag = maybeTag.substring(1);
        } else {
            input.item = maybeTag;
        }
        item_inputs.add(input);
        return this;
    }

    public MIRecipeBuilder addPartOutput(String part, int amount) {
        return addPartOutput(context.getPart(part), amount);
    }

    public MIRecipeBuilder addPartOutput(MaterialPart part, int amount) {
        if (part == null) {
            canceled = true;
        } else {
            MIItemOutput output = new MIItemOutput();
            output.item = part.getItemId();
            output.amount = amount;
            item_outputs.add(output);
        }
        return this;
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
            ModernIndustrialization.RESOURCE_PACK.addData(new Identifier(fullId), GSON.toJson(this).getBytes());
        }
    }
}

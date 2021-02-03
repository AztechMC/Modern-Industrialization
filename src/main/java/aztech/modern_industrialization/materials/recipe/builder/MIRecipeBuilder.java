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
import aztech.modern_industrialization.materials.part.MaterialPart;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration" })
public class MIRecipeBuilder implements MaterialRecipeBuilder {
    private static final transient Gson GSON = new Gson();

    public final transient String recipeId;
    private final transient MaterialBuilder.RecipeContext context;
    private transient boolean canceled = false;
    private final String type;
    private final int eu;
    private final int duration;
    private final List<MIItemInput> item_inputs = new ArrayList<>();
    private final List<MIFluidInput> fluid_inputs = new ArrayList<>();
    private final List<MIItemOutput> item_outputs = new ArrayList<>();

    private static class MIItemInput {
        String item;
        String tag;
        int amount;
    }

    private static class MIFluidInput {
        String fluid;
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

    // TODO: remove these two if the part is always a string passed through
    // addPartInput
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

    public MIRecipeBuilder addFluidInput(String fluid, int amount) {
        MIFluidInput input = new MIFluidInput();
        input.fluid = fluid;
        input.amount = amount;
        fluid_inputs.add(input);
        return this;
    }

    public MIRecipeBuilder addPartOutput(String part, int amount) {
        return addPartOutput(context.getPart(part), amount);
    }

    public MIRecipeBuilder addPartOutput(MaterialPart part, int amount) {
        if (part == null) {
            canceled = true;
        } else {
            return addOutput(part.getItemId(), amount);
        }
        return this;
    }

    public MIRecipeBuilder addOutput(String itemId, int amount){
        MIItemOutput output = new MIItemOutput();
        output.item =  itemId;
        output.amount = amount;
        item_outputs.add(output);
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

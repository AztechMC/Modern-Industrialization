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

import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.part.MaterialPart;
import aztech.modern_industrialization.materials.part.Part;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import com.google.gson.Gson;
import java.util.function.Consumer;
import net.minecraft.data.recipes.FinishedRecipe;

public class MIRecipeBuilder extends MIRecipeJson<MIRecipeBuilder> implements MaterialRecipeBuilder {
    private static final Gson GSON = new Gson();

    public transient final String recipeId;
    private transient final MaterialBuilder.RecipeContext context;
    private transient boolean canceled = false;

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, MachineRecipeType type, String recipeSuffix, int eu, int duration) {
        super(type, eu, duration);
        this.recipeId = type.getPath() + "/" + recipeSuffix;
        this.context = context;
        context.addRecipe(this);
    }

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, String recipeSuffix,
            MIRecipeJson<?> otherWithSameRecipeData) {
        super(otherWithSameRecipeData);
        this.recipeId = this.machineRecipeType.getPath() + "/" + recipeSuffix;
        this.context = context;
        context.addRecipe(this);
    }

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, MachineRecipeType type, String recipeSuffix) {
        this(context, type, recipeSuffix, 2, (int) (200 * context.getHardness().timeFactor));
    }

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, MachineRecipeType type, Part recipeSuffix) {
        this(context, type, recipeSuffix.key);
    }

    public MIRecipeBuilder addPartInput(Part part, int amount) {
        return addPartInput(context.getPart(part), amount);
    }

    public MIRecipeBuilder addTaggedPartInput(Part part, int amount) {
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

    public MIRecipeBuilder addPartOutput(Part part, int amount) {
        return addPartOutput(context.getPart(part), amount);
    }

    public MIRecipeBuilder addPartOutput(MaterialPart part, int amount) {
        if (part == null) {
            canceled = true;
        } else {
            return addItemOutput(part.getItemId(), amount);
        }
        return this;
    }

    public MIRecipeBuilder addPartOutput(Part part, int amount, double probability) {
        return addPartOutput(context.getPart(part), amount, probability);
    }

    public MIRecipeBuilder addPartOutput(MaterialPart part, int amount, double probability) {
        if (part == null) {
            canceled = true;
        } else {
            return addItemOutput(part.getItemId(), amount, probability);
        }
        return this;
    }

    public boolean isCanceled() {
        return canceled;
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
    public void save(Consumer<FinishedRecipe> consumer) {
        if (!canceled) {
            String fullId = "materials/" + context.getMaterialName() + "/" + recipeId;
            this.offerTo(consumer, fullId);
        }
    }

}

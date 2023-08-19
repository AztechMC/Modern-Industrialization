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

import static aztech.modern_industrialization.materials.property.MaterialProperty.HARDNESS;

import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.part.PartKeyProvider;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import com.google.gson.Gson;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class ShapedRecipeBuilder implements MaterialRecipeBuilder {
    private static final Gson GSON = new Gson();

    public final String recipeId;
    private final MaterialBuilder.RecipeContext context;
    private boolean canceled = false;
    private final String id;
    private final ShapedRecipeJson json;

    public ShapedRecipeBuilder(MaterialBuilder.RecipeContext context, PartKeyProvider result, int count, String id, String... pattern) {
        this.recipeId = "craft/" + id;
        this.context = context;
        this.id = id;
        if (context.getPart(result) == null) {
            this.json = null;
            canceled = true;
        } else {
            this.json = new ShapedRecipeJson(context.getPart(result).getItemId(), count, pattern);
        }
        context.addRecipe(this);
    }

    public ShapedRecipeBuilder addPart(char key, PartKeyProvider part) {
        if (context.getPart(part) != null) {
            addInput(key, context.getPart(part).getItemId());
        } else {
            canceled = true;
        }
        return this;
    }

    public ShapedRecipeBuilder addTaggedPart(char key, PartKeyProvider part) {
        if (context.getPart(part) != null) {
            addInput(key, context.getPart(part).getTaggedItemId());
        } else {
            canceled = true;
        }
        return this;
    }

    public ShapedRecipeBuilder addInput(char key, TagKey<Item> tag) {
        return addInput(key, "#" + tag.location().toString());
    }

    public ShapedRecipeBuilder addInput(char key, String maybeTag) {
        if (!canceled) {
            json.addInput(key, maybeTag);
        }
        return this;
    }

    public ShapedRecipeBuilder addInput(char key, ItemLike item) {
        return addInput(key, BuiltInRegistries.ITEM.getKey(item.asItem()).toString());
    }

    public ShapedRecipeBuilder exportToAssembler(int eu, int duration) {
        return exportToMachine(MIMachineRecipeTypes.ASSEMBLER, eu, duration, 1);
    }

    public ShapedRecipeBuilder exportToAssembler() {
        return exportToAssembler(8, 200);
    }

    public ShapedRecipeBuilder exportToMachine(MachineRecipeType machine, int eu, int duration, int division) {
        if (!canceled) {
            new MIRecipeBuilder(context, id, json.exportToMachine(machine, eu, duration, division));
        }

        return this;
    }

    public ShapedRecipeBuilder exportToMachine(MachineRecipeType machine) {
        return exportToMachine(machine, 2, (int) (200 * context.get(HARDNESS).timeFactor), 1);
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
            json.validate();
            String fullId = "materials/" + context.getMaterialName() + "/" + recipeId;
            json.offerTo(consumer, fullId);
        }
    }
}

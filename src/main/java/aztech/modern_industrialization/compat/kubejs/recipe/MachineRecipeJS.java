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
package aztech.modern_industrialization.compat.kubejs.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.item.ingredient.IngredientStack;
import dev.latvian.mods.kubejs.platform.IngredientPlatformHelper;
import dev.latvian.mods.kubejs.recipe.IngredientMatch;
import dev.latvian.mods.kubejs.recipe.ItemInputTransformer;
import dev.latvian.mods.kubejs.recipe.ItemOutputTransformer;
import dev.latvian.mods.kubejs.recipe.RecipeArguments;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public class MachineRecipeJS extends RecipeJS {
    public final List<ProbabilityValue<Ingredient>> inputs = new ArrayList<>();
    public final List<ProbabilityValue<ItemStack>> outputs = new ArrayList<>();

    @Override
    public void create(RecipeArguments recipeArguments) {
        throw new RecipeExceptionJS("MachineRecipe#create should never be called");
    }

    @Override
    public void deserialize() {
        readItemInput(json.get("item_inputs"));
        readItemOutput(json.get("item_outputs"));

        if (json.get("id") instanceof JsonPrimitive string) {
            id = new ResourceLocation(string.getAsString());
        }
    }

    private void readItemInput(JsonElement element) {
        if (element instanceof JsonObject object) {
            var amount = 1;

            if (object.has("amount")) {
                amount = object.get("amount").getAsInt();
            }

            if (object.has("count")) {
                amount = object.get("count").getAsInt();
            }

            inputs.add(new ProbabilityValue<>(IngredientPlatformHelper.get().stack(parseItemInput(element, ""), amount), readProbability(object)));
        } else if (element instanceof JsonArray array) {
            for (var entry : array) {
                readItemInput(entry);
            }
        } else if (element != null) {
            throw new UnsupportedOperationException();
        }
    }

    private void readItemOutput(JsonElement element) {
        if (element instanceof JsonObject object) {
            outputs.add(new ProbabilityValue<>(parseItemOutput(object), readProbability(object)));
        } else if (element instanceof JsonArray array) {
            for (var entry : array) {
                readItemOutput(entry);
            }
        } else if (element != null) {
            throw new UnsupportedOperationException();
        }
    }

    private float readProbability(JsonObject object) {
        if (object.get("probability") instanceof JsonPrimitive probability) {
            return probability.getAsFloat();
        } else {
            return 1.0f;
        }
    }

    @Override
    public void serialize() {
        if (serializeInputs && !inputs.isEmpty()) {
            var itemInputs = new JsonArray();

            for (var input : inputs) {
                var o = input.value.toJson().getAsJsonObject();
                o.addProperty("probability", input.probability);
                itemInputs.add(o);
            }

            json.add("item_inputs", itemInputs);
        }

        if (serializeOutputs && !outputs.isEmpty()) {
            var itemOutputs = new JsonArray();

            for (var output : outputs) {
                var o = new JsonObject();
                o.addProperty("probability", output.probability);
                o.addProperty("item", Registry.ITEM.getKey(output.value.getItem()).toString());
                o.addProperty("amount", output.value.getCount());
                itemOutputs.add(o);
            }

            json.add("item_outputs", itemOutputs);
        }
    }

    @Override
    public boolean hasInput(IngredientMatch match) {
        for (var ingredient : inputs) {
            if (match.contains(ingredient.value)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean replaceInput(IngredientMatch match, Ingredient with, ItemInputTransformer transformer) {
        boolean changedSomething = false;

        for (var input : inputs) {
            if (match.contains(input.value)) {
                input.value = transformer.transform(this, match, input.value, with);
                changedSomething = true;
            }
        }

        return changedSomething;
    }

    @Override
    public boolean hasOutput(IngredientMatch match) {
        for (var output : outputs) {
            if (match.contains(output.value)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean replaceOutput(IngredientMatch match, ItemStack with, ItemOutputTransformer transformer) {
        boolean changedSomething = false;

        for (var output : outputs) {
            if (match.contains(output.value)) {
                output.value = transformer.transform(this, match, output.value, with);
                changedSomething = true;
            }
        }

        return changedSomething;
    }

    @Override
    public @Nullable JsonElement serializeIngredientStack(IngredientStack in) {
        var json = in.getIngredient().toJson().getAsJsonObject();
        json.addProperty("count", in.getCount());
        return json;
    }
}

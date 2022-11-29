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
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class MachineRecipeJS extends RecipeJS {
    private final List<ProbabilityValue<Ingredient>> inputs = new ArrayList<>();
    private final List<ProbabilityValue<ItemStack>> outputs = new ArrayList<>();

    @Override
    public void create(RecipeArguments args) {
        if (args.size() != 2) {
            throw new RecipeExceptionJS("Machine recipe should have exactly 2 arguments, EU/t and duration, received: " + args.size());
        }

        json.addProperty("eu", forceGetInt(args, 0));
        json.addProperty("duration", forceGetInt(args, 1));
    }

    public MachineRecipeJS itemIn(Ingredient ingredient) {
        return itemIn(ingredient, 1);
    }

    public MachineRecipeJS itemIn(Ingredient ingredient, float chance) {
        inputs.add(new ProbabilityValue<>(ingredient, chance));
        return this;
    }

    public MachineRecipeJS itemOut(ItemStack output) {
        return itemOut(output, 1);
    }

    public MachineRecipeJS itemOut(ItemStack output, float chance) {
        outputs.add(new ProbabilityValue<>(output, chance));
        return this;
    }

    public MachineRecipeJS fluidIn(Fluid fluid, int amount) {
        return fluidIn(fluid, amount, 1);
    }

    public MachineRecipeJS fluidIn(Fluid fluid, double mbs, float chance) {
        if (!json.has("fluid_inputs")) {
            json.add("fluid_inputs", new JsonArray());
        }

        var input = new JsonObject();
        input.addProperty("fluid", Registry.FLUID.getKey(fluid).toString());
        input.addProperty("amount", mbs);
        input.addProperty("probability", chance);

        json.get("fluid_inputs").getAsJsonArray().add(input);
        return this;
    }

    public MachineRecipeJS fluidOut(Fluid fluid, int amount) {
        return fluidOut(fluid, amount, 1);
    }

    public MachineRecipeJS fluidOut(Fluid fluid, double mbs, float chance) {
        if (!json.has("fluid_outputs")) {
            json.add("fluid_outputs", new JsonArray());
        }

        var output = new JsonObject();
        output.addProperty("fluid", Registry.FLUID.getKey(fluid).toString());
        output.addProperty("amount", mbs);
        output.addProperty("probability", chance);

        json.get("fluid_outputs").getAsJsonArray().add(output);
        return this;
    }

    private static int forceGetInt(RecipeArguments args, int index) {
        if (args.get(index) instanceof Number n) {
            return n.intValue();
        } else {
            throw new RecipeExceptionJS("Expected an integer at index " + index + ", got " + args.get(index));
        }
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

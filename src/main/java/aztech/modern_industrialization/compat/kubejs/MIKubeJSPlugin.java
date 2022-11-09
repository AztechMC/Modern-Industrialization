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
package aztech.modern_industrialization.compat.kubejs;

import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.item.ingredient.IngredientStack;
import dev.latvian.mods.kubejs.platform.IngredientPlatformHelper;
import dev.latvian.mods.kubejs.recipe.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public class MIKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void registerRecipeTypes(RegisterRecipeTypesEvent event) {
        for (var mrt : MIMachineRecipeTypes.getRecipeTypes()) {
            event.register(mrt.getId(), MachineRecipe::new);
        }
    }

    private static class MachineRecipe extends RecipeJS {
        public final Map<ItemStack, Float> outputItems = new LinkedHashMap<>();
        public final Map<Ingredient, Float> inputItems = new LinkedHashMap<>();

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

                inputItems.put(IngredientPlatformHelper.get().stack(parseItemInput(element, ""), amount),
                        readProbability(object));
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
                outputItems.put(parseItemOutput(object), readProbability(object));
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
            if (serializeInputs && !inputItems.isEmpty()) {
                var itemInputs = new JsonArray();

                for (var entry : inputItems.entrySet()) {
                    var o = entry.getKey().toJson().getAsJsonObject();
                    o.addProperty("probability", entry.getValue());
                    itemInputs.add(o);
                }

                json.add("item_inputs", itemInputs);
            }

            if (serializeOutputs && !outputItems.isEmpty()) {
                var itemOutputs = new JsonArray();

                for (var entry : outputItems.entrySet()) {
                    var o = new JsonObject();
                    o.addProperty("probability", entry.getValue());
                    o.addProperty("item", Registry.ITEM.getKey(entry.getKey().getItem()).toString());
                    o.addProperty("amount", entry.getKey().getCount());
                    itemOutputs.add(o);
                }

                json.add("item_outputs", itemOutputs);
            }
        }

        @Override
        public boolean hasInput(IngredientMatch match) {
            for (var ingredient : inputItems.keySet()) {
                if (match.contains(ingredient)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean replaceInput(IngredientMatch match, Ingredient with, ItemInputTransformer transformer) {
            var changes = new HashMap<Ingredient, Ingredient>();

            for (var item : inputItems.keySet()) {
                changes.put(item, transformer.transform(this, match, item, with));
            }

            for (var entry : changes.entrySet()) {
                inputItems.put(entry.getValue(), inputItems.remove(entry.getKey()));
            }

            return !changes.isEmpty();
        }

        @Override
        public boolean hasOutput(IngredientMatch match) {
            for (var item : outputItems.keySet()) {
                if (match.contains(item)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean replaceOutput(IngredientMatch match, ItemStack with, ItemOutputTransformer transformer) {
            var changes = new HashMap<ItemStack, ItemStack>();

            for (var item : outputItems.keySet()) {
                changes.put(item, transformer.transform(this, match, item, with));
            }

            for (var entry : changes.entrySet()) {
                outputItems.put(entry.getValue(), outputItems.remove(entry.getKey()));
            }

            return !changes.isEmpty();
        }

        @Override
        public @Nullable JsonElement serializeIngredientStack(IngredientStack in) {
            var json = in.getIngredient().toJson().getAsJsonObject();
            json.addProperty("count", in.getCount());
            return json;
        }
    }
}

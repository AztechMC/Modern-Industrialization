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
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientStackJS;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MIKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        for (MachineRecipeType mrt : MIMachineRecipeTypes.getRecipeTypes()) {
            event.register(mrt.getId(), MachineRecipe::new);
        }
    }

    private static class MachineRecipe extends RecipeJS {
        private float[] itemInputProbabilities;
        private float[] itemOutputProbabilities;

        @Override
        public void create(@NotNull ListJS listJS) {
            throw new RecipeExceptionJS("MachineRecipe#create should never be called");
        }

        @Override
        public void deserialize() {
            JsonElement j = json.get("item_inputs");
            if (j != null) {
                if (j.isJsonArray()) {
                    JsonArray arr = j.getAsJsonArray();
                    itemInputProbabilities = new float[arr.size()];
                    for (int i = 0; i < arr.size(); ++i) {
                        readItemInput(arr.get(i), i);
                    }
                } else {
                    itemInputProbabilities = new float[1];
                    readItemInput(j, 0);
                }
            }

            JsonElement o = json.get("item_outputs");
            if (o != null) {
                if (o.isJsonArray()) {
                    JsonArray arr = o.getAsJsonArray();
                    itemOutputProbabilities = new float[arr.size()];
                    for (int i = 0; i < arr.size(); ++i) {
                        readItemOutput(arr.get(i), i);
                    }
                } else {
                    itemOutputProbabilities = new float[1];
                    readItemOutput(o, 0);
                }
            }

            if (json.has("id")) {
                id = new ResourceLocation(json.get("id").getAsString());
            }
        }

        private void readItemInput(JsonElement el, int index) {
            JsonObject obj = el.getAsJsonObject();
            int amount = 1;
            if (obj.has("amount"))
                amount = obj.get("amount").getAsInt();
            if (obj.has("count"))
                amount = obj.get("count").getAsInt();
            IngredientJS ing = IngredientJS.of(obj);
            ing = ing.withCount(amount);
            inputItems.add(ing);
            itemInputProbabilities[index] = readProbability(obj);
        }

        private void readItemOutput(JsonElement el, int index) {
            JsonObject obj = el.getAsJsonObject();
            ItemStackJS stack = ItemStackJS.resultFromRecipeJson(obj);
            if (obj.has("amount")) {
                stack.setCount(obj.get("amount").getAsInt());
            } else {
                stack.setCount(1);
            }
            outputItems.add(stack);
            itemOutputProbabilities[index] = readProbability(obj);
        }

        private float readProbability(JsonObject o) {
            if (o.has("probability")) {
                return o.get("probability").getAsFloat();
            } else {
                return 1.0f;
            }
        }

        @Override
        public void serialize() {
            if (inputItems.size() > 0) {
                JsonArray itemInputs = new JsonArray();
                for (int i = 0; i < inputItems.size(); ++i) {
                    IngredientJS ingredient = inputItems.get(i).asIngredientStack();
                    JsonObject o = (JsonObject) ingredient.toJson();
                    o.addProperty("probability", itemInputProbabilities[i]);
                    itemInputs.add(o);
                }
                json.add("item_inputs", itemInputs);
            }

            if (outputItems.size() > 0) {
                JsonArray itemOutputs = new JsonArray();
                for (int i = 0; i < outputItems.size(); ++i) {
                    ItemStackJS stack = outputItems.get(i);
                    JsonObject o = new JsonObject();
                    o.addProperty("probability", itemOutputProbabilities[i]);
                    o.addProperty("item", stack.getId());
                    o.addProperty("amount", stack.getCount());
                    itemOutputs.add(o);
                }
                json.add("item_outputs", itemOutputs);
            }
        }

        @Override
        public JsonElement serializeIngredientStack(IngredientStackJS in) {
            JsonObject json = new JsonObject();
            json.add(in.ingredientKey, in.ingredient.toJson());
            json.addProperty(in.countKey, in.getCount());
            return json;
        }
    }
}

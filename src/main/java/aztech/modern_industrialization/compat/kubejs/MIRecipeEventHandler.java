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

import aztech.modern_industrialization.machines.MIMachines;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.kubejs.KubeJSInitializer;
import dev.latvian.kubejs.item.ItemStackJS;
import dev.latvian.kubejs.item.ingredient.IngredientJS;
import dev.latvian.kubejs.recipe.RecipeJS;
import dev.latvian.kubejs.recipe.RecipeTypeJS;
import dev.latvian.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.kubejs.util.ListJS;
import dev.latvian.kubejs.util.UtilsJS;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public class MIRecipeEventHandler implements KubeJSInitializer {
    @Override
    public void onKubeJSInitialization() {
        RegisterRecipeHandlersEvent.EVENT.register(event -> {
            MIMachines.RECIPE_TYPES.keySet().forEach(t -> event.register(new MachineRecipeType(t.getId().toString(), MachineRecipe::new)));
            event.register(new MachineRecipeType("modern_industrialization:forge_hammer_hammer", MachineRecipe::new));
            event.register(new MachineRecipeType("modern_industrialization:forge_hammer_saw", MachineRecipe::new));
        });
    }

    private static class MachineRecipeType extends RecipeTypeJS {
        public MachineRecipeType(String id, Supplier<RecipeJS> f) {
            super(Objects.requireNonNull(Registry.RECIPE_SERIALIZER.get(UtilsJS.getMCID(id))), id, f);
        }

        @Override
        public boolean isCustom() {
            return true;
        }
    }

    private static class MachineRecipe extends RecipeJS {
        private int eu;
        private int duration;
        private float[] itemInputProbabilities;
        private float[] itemOutputProbabilities;
        private JsonElement fluidInputs, fluidOutputs;

        @Override
        public void create(@NotNull ListJS listJS) {
            throw new UnsupportedOperationException("MachineRecipe#create should never be called");
        }

        @Override
        public void deserialize() {
            eu = json.get("eu").getAsInt();
            duration = json.get("duration").getAsInt();
            fluidInputs = json.get("fluid_inputs");
            fluidOutputs = json.get("fluid_outputs");

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
                id = new Identifier(json.get("id").getAsString());
            } else if (id == null) {
                throw new RuntimeException("You must specify an id for custom MI recipes! Recipe: " + json.toString());
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
            ing.count(amount);
            inputItems.add(ing);
            itemInputProbabilities[index] = readProbability(obj);
        }

        private void readItemOutput(JsonElement el, int index) {
            JsonObject obj = el.getAsJsonObject();
            ItemStackJS stack = ItemStackJS.resultFromRecipeJson(obj);
            stack.setCount(obj.get("amount").getAsInt());
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
        protected void serialize() {
            json.addProperty("eu", eu);
            json.addProperty("duration", duration);
            if (fluidInputs != null)
                json.add("fluid_inputs", fluidInputs);
            if (fluidOutputs != null)
                json.add("fluid_outputs", fluidOutputs);

            if (inputItems.size() > 0) {
                JsonArray itemInputs = new JsonArray();
                for (int i = 0; i < inputItems.size(); ++i) {
                    IngredientJS ingredient = inputItems.get(i);
                    JsonObject o = new JsonObject();
                    o.addProperty("probability", itemInputProbabilities[i]);
                    o.add("ingredient", ingredient.toJson());
                    o.addProperty("amount", ingredient.getCount());
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
                    o.addProperty("item", Registry.ITEM.getId(stack.getItem()).toString());
                    o.addProperty("amount", stack.getCount());
                    itemOutputs.add(o);
                }
                json.add("item_outputs", itemOutputs);
            }
        }
    }
}

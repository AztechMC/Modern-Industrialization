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

import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.ItemMatch;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.ArrayUtils;

public final class MachineRecipeSchema {
    private static final RecipeComponent<Integer> POSITIVE_INTEGER = NumberComponent.intRange(1, Integer.MAX_VALUE);
    private static final RecipeComponent<Float> PROBABILITY = NumberComponent.floatRange(0, 1);

    public record ChancedInputItem(InputItem input, float probability) {
    }

    private static final RecipeComponent<ChancedInputItem> ITEM_INPUT = new RecipeComponent<>() {
        @Override
        public String componentType() {
            return "modern_industrialization_item_input";
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.INPUT;
        }

        @Override
        public Class<?> componentClass() {
            return ChancedInputItem.class;
        }

        @Override
        public boolean hasPriority(RecipeJS recipe, Object from) {
            return recipe.inputItemHasPriority(from);
        }

        @Override
        public JsonElement write(RecipeJS recipe, ChancedInputItem value) {
            JsonObject obj = new JsonObject();
            obj.add("ingredient", value.input().ingredient.toJson());
            obj.addProperty("amount", value.input().count);
            obj.addProperty("probability", value.probability());
            return obj;
        }

        @Override
        public ChancedInputItem read(RecipeJS recipe, Object from) {
            if (!(from instanceof JsonObject json)) {
                throw new IllegalArgumentException("Expected an object, got " + from);
            }

            var input = MachineRecipeType.readItemInput(json);
            return new ChancedInputItem(InputItem.of(input.ingredient, input.amount), input.probability);
        }

        @Override
        public boolean isInput(RecipeJS recipe, ChancedInputItem value, ReplacementMatch match) {
            return match instanceof ItemMatch m && !value.input().isEmpty() && m.contains(value.input().ingredient);
        }

        @Override
        public ChancedInputItem replaceInput(RecipeJS recipe, ChancedInputItem original, ReplacementMatch match, InputReplacement with) {
            if (isInput(recipe, original, match) && with instanceof InputItem w) {
                return new ChancedInputItem(w.withCount(original.input().count), original.probability());
            } else {
                return original;
            }
        }

        @Override
        public String checkEmpty(RecipeKey<ChancedInputItem> key, ChancedInputItem value) {
            if (value.input().isEmpty()) {
                return "Ingredient '" + key.name + "' can't be empty!";
            }

            return "";
        }

        @Override
        public String toString() {
            return componentType();
        }
    };

    private static final RecipeKey<Integer> EU = NumberComponent.intRange(1, Integer.MAX_VALUE).key("eu");
    private static final RecipeKey<Integer> DURATION = NumberComponent.intRange(1, Integer.MAX_VALUE).key("duration");
    private static final RecipeKey<ChancedInputItem[]> ITEM_INPUTS = ITEM_INPUT.asArrayOrSelf().key("item_inputs");
    private static final RecipeKey<OutputItem[]> ITEM_OUTPUTS = ItemComponents.OUTPUT.asArrayOrSelf().key("item_outputs");

    public static final RecipeSchema SCHEMA = new RecipeSchema(
            MachineRecipeJS.class,
            MachineRecipeJS::new,
            EU,
            DURATION,
            ITEM_INPUTS.optional(ty -> new ChancedInputItem[0]),
            ITEM_OUTPUTS.optional(ty -> new OutputItem[0]))
                    .constructor(EU, DURATION);

    private static final RecipeKey<Integer> HAMMER_DAMAGE = NumberComponent.INT.key("hammer_damage").optional(0);
    public static final RecipeSchema FORGE_HAMMER_SCHEMA = new RecipeSchema(
            MachineRecipeJS.class,
            MachineRecipeJS::new,
            HAMMER_DAMAGE,
            ITEM_INPUTS,
            ITEM_OUTPUTS)
                    .constructor(HAMMER_DAMAGE);

    private MachineRecipeSchema() {
    }

    public static class MachineRecipeJS extends RecipeJS implements ProcessConditionHelper {

        public MachineRecipeJS itemIn(InputItem ingredient) {
            return itemIn(ingredient, 1);
        }

        public MachineRecipeJS itemIn(InputItem ingredient, float chance) {
            setValue(ITEM_INPUTS, ArrayUtils.add(getValue(ITEM_INPUTS), new ChancedInputItem(ingredient, chance)));
            return this;
        }

        public MachineRecipeJS itemOut(OutputItem output) {
            return itemOut(output, 1);
        }

        public MachineRecipeJS itemOut(OutputItem output, float chance) {
            setValue(ITEM_OUTPUTS, ArrayUtils.add(getValue(ITEM_OUTPUTS), output.withChance(chance)));
            return this;
        }

        public MachineRecipeJS fluidIn(Fluid fluid, double mbs) {
            return fluidIn(fluid, mbs, 1);
        }

        public MachineRecipeJS fluidIn(Fluid fluid, double mbs, float chance) {
            if (!json.has("fluid_inputs")) {
                json.add("fluid_inputs", new JsonArray());
            }

            var input = new JsonObject();
            input.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
            input.addProperty("amount", mbs);
            input.addProperty("probability", chance);

            json.get("fluid_inputs").getAsJsonArray().add(input);
            changed = true;
            return this;
        }

        public MachineRecipeJS fluidOut(Fluid fluid, double mbs) {
            return fluidOut(fluid, mbs, 1);
        }

        public MachineRecipeJS fluidOut(Fluid fluid, double mbs, float chance) {
            if (!json.has("fluid_outputs")) {
                json.add("fluid_outputs", new JsonArray());
            }

            var output = new JsonObject();
            output.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
            output.addProperty("amount", mbs);
            output.addProperty("probability", chance);

            json.get("fluid_outputs").getAsJsonArray().add(output);
            changed = true;
            return this;
        }

        @Override
        public MachineRecipeJS processCondition(MachineProcessCondition condition) {
            if (!json.has("process_conditions")) {
                json.add("process_conditions", new JsonArray());
            }

            json.get("process_conditions").getAsJsonArray().add(condition.toJson());
            changed = true;
            return this;
        }

        private float readProbability(JsonObject object) {
            if (object.get("probability") instanceof JsonPrimitive probability) {
                return probability.getAsFloat();
            } else {
                return 1.0f;
            }
        }

        @Override
        public JsonElement writeOutputItem(OutputItem value) {
            var json = new JsonObject();
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(value.item.getItem()).toString());
            json.addProperty("amount", value.item.getCount());

            if (value.hasChance()) {
                json.addProperty("probability", value.getChance());
            }

            return json;
        }
    }
}

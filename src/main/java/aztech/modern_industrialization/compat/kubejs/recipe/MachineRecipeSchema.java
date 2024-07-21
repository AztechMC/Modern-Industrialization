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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.MIExtraCodecs;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.bindings.IngredientWrapper;
import dev.latvian.mods.kubejs.bindings.SizedIngredientWrapper;
import dev.latvian.mods.kubejs.core.IngredientKJS;
import dev.latvian.mods.kubejs.core.ItemStackKJS;
import dev.latvian.mods.kubejs.core.RegistryObjectKJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public final class MachineRecipeSchema {
    private static final RecipeComponent<Integer> POSITIVE_INTEGER = NumberComponent.intRange(1, Integer.MAX_VALUE);
    private static final RecipeComponent<Float> PROBABILITY = NumberComponent.floatRange(0, 1);

    private static final RecipeComponent<MachineRecipe.ItemInput> ITEM_INPUT = new RecipeComponent<>() {
        @Override
        public Codec<MachineRecipe.ItemInput> codec() {
            return MachineRecipe.ItemInput.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return SizedIngredientWrapper.TYPE_INFO;
        }

        @Override
        public MachineRecipe.ItemInput wrap(Context cx, KubeRecipe recipe, Object from) {
            var sizedIngredient = (SizedIngredient) cx.jsToJava(from, typeInfo());
            return new MachineRecipe.ItemInput(sizedIngredient.ingredient(), sizedIngredient.count(), 1);
        }

        @Override
        public boolean matches(Context cx, KubeRecipe recipe, MachineRecipe.ItemInput value, ReplacementMatchInfo match) {
            return match.match() instanceof ItemMatch m && m.matches(cx, value.ingredient(), match.exact());
        }

        @Override
        public MachineRecipe.ItemInput replace(Context cx, KubeRecipe recipe, MachineRecipe.ItemInput original, ReplacementMatchInfo match,
                Object with) {
            if (matches(cx, recipe, original, match)) {
                var withJava = (SizedIngredient) cx.jsToJava(with, typeInfo());
                return new MachineRecipe.ItemInput(withJava.ingredient(), withJava.count(), original.probability());
            } else {
                return original;
            }
        }

        @Override
        public String checkEmpty(RecipeKey<MachineRecipe.ItemInput> key, MachineRecipe.ItemInput value) {
            if (value.ingredient().isEmpty() || value.amount() <= 0) {
                return "ItemInput '" + key.name + "' can't be empty!";
            }

            return "";
        }

        @Override
        public void buildUniqueId(UniqueIdBuilder builder, MachineRecipe.ItemInput value) {
            var tag = IngredientWrapper.tagKeyOf(value.ingredient());

            if (tag != null) {
                builder.append(tag.location());
            } else {
                var first = IngredientKJS.class.cast(value.ingredient()).kjs$getFirst();

                if (!first.isEmpty()) {
                    builder.append(RegistryObjectKJS.class.cast(first).kjs$getIdLocation());
                }
            }
        }
    };
    private static final RecipeComponent<MachineRecipe.ItemOutput> ITEM_OUTPUT = new RecipeComponent<>() {
        @Override
        public Codec<MachineRecipe.ItemOutput> codec() {
            return MachineRecipe.ItemOutput.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return ItemStackJS.TYPE_INFO;
        }

        @Override
        public MachineRecipe.ItemOutput wrap(Context cx, KubeRecipe recipe, Object from) {
            var itemStack = (ItemStack) cx.jsToJava(from, typeInfo());
            return new MachineRecipe.ItemOutput(ItemVariant.of(itemStack), itemStack.getCount(), 1);
        }

        @Override
        public boolean matches(Context cx, KubeRecipe recipe, MachineRecipe.ItemOutput value, ReplacementMatchInfo match) {
            return match.match() instanceof ItemMatch m && !value.variant().isBlank() && value.amount() > 0
                    && m.matches(cx, value.getStack(), match.exact());
        }

        @Override
        public MachineRecipe.ItemOutput replace(Context cx, KubeRecipe recipe, MachineRecipe.ItemOutput original, ReplacementMatchInfo match,
                Object with) {
            if (matches(cx, recipe, original, match)) {
                var withJava = (ItemStack) cx.jsToJava(with, typeInfo());
                return new MachineRecipe.ItemOutput(ItemVariant.of(withJava), withJava.getCount(), original.probability());
            } else {
                return original;
            }
        }

        @Override
        public String checkEmpty(RecipeKey<MachineRecipe.ItemOutput> key, MachineRecipe.ItemOutput value) {
            if (value.getStack().isEmpty()) {
                return "ItemOutput '" + key.name + "' can't be empty!";
            }

            return "";
        }

        @Override
        public void buildUniqueId(UniqueIdBuilder builder, MachineRecipe.ItemOutput value) {
            if (!value.getStack().isEmpty()) {
                builder.append(ItemStackKJS.class.cast(value.getStack()).kjs$getIdLocation());
            }
        }
    };

    private static final RecipeKey<Integer> EU = NumberComponent.intRange(1, Integer.MAX_VALUE).key("eu", ComponentRole.OTHER);
    private static final RecipeKey<Integer> DURATION = NumberComponent.intRange(1, Integer.MAX_VALUE).key("duration", ComponentRole.OTHER);
    private static final RecipeKey<List<MachineRecipe.ItemInput>> ITEM_INPUTS = maybeList(ITEM_INPUT).key("item_inputs", ComponentRole.INPUT);
    private static final RecipeKey<List<MachineRecipe.ItemOutput>> ITEM_OUTPUTS = maybeList(ITEM_OUTPUT).key("item_outputs",
            ComponentRole.OUTPUT);

    private static <T> ListRecipeComponent<T> maybeList(RecipeComponent<T> component) {
        // Don't use component.asListOrSelf() because we want to support conditions in list elements!
        return new ListRecipeComponent<>(component, true, TypeInfo.RAW_LIST.withParams(component.typeInfo()).or(component.typeInfo()),
                MIExtraCodecs.maybeList(component.codec()));
    }

    public static final KubeRecipeFactory MACHINE_RECIPE_FACTORY = new KubeRecipeFactory(
            MI.id("machine"), MachineRecipeJS.class, MachineRecipeJS::new);
    public static final RecipeSchema SCHEMA = new RecipeSchema(
            EU,
            DURATION,
            ITEM_INPUTS.optional(ty -> List.of()),
            ITEM_OUTPUTS.optional(ty -> List.of()))
                    .factory(MACHINE_RECIPE_FACTORY)
                    .constructor(EU, DURATION);

    private static final RecipeKey<Integer> HAMMER_DAMAGE = NumberComponent.INT.key("hammer_damage", ComponentRole.OTHER).optional(0);
    public static final RecipeSchema FORGE_HAMMER_SCHEMA = new RecipeSchema(
            HAMMER_DAMAGE,
            ITEM_INPUTS,
            ITEM_OUTPUTS)
                    .factory(MACHINE_RECIPE_FACTORY)
                    .constructor(HAMMER_DAMAGE);

    private MachineRecipeSchema() {
    }

    public static class MachineRecipeJS extends KubeRecipe implements ProcessConditionHelper {

        public MachineRecipeJS itemIn(SizedIngredient ingredient) {
            return itemIn(ingredient, 1);
        }

        public MachineRecipeJS itemIn(SizedIngredient ingredient, float chance) {
            var newList = new ArrayList<>(getValue(ITEM_INPUTS));
            newList.add(new MachineRecipe.ItemInput(ingredient.ingredient(), ingredient.count(), chance));
            setValue(ITEM_INPUTS, newList);
            return this;
        }

        public MachineRecipeJS itemOut(ItemStack output) {
            return itemOut(output, 1);
        }

        public MachineRecipeJS itemOut(ItemStack output, float chance) {
            var newList = new ArrayList<>(getValue(ITEM_OUTPUTS));
            newList.add(new MachineRecipe.ItemOutput(ItemVariant.of(output), output.getCount(), chance));
            setValue(ITEM_OUTPUTS, newList);
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

            var condJson = MachineProcessCondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow(RuntimeException::new);
            json.get("process_conditions").getAsJsonArray().add(condJson);
            changed = true;
            return this;
        }
    }
}

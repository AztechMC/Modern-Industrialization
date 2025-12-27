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

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.IntBounds;
import dev.latvian.mods.kubejs.util.TickDuration;
import java.util.List;

public final class MachineRecipeSchema {
    private MachineRecipeSchema() {}

    private static <T> RecipeKey<List<T>> optionalList(RecipeComponentType<T> component, String name, ComponentRole role) {
        return component.instance()
                .asConditionalList()
                .orSelf()
                .withBounds(IntBounds.OPTIONAL)
                .key(name, role)
                .optional(List.of());
    }

    public static final RecipeKey<Integer> EU = NumberComponent.intRange(1, Integer.MAX_VALUE).inputKey("eu");
    public static final RecipeKey<TickDuration> DURATION = TimeComponent.TICKS.inputKey("duration");
    public static final RecipeKey<List<MachineRecipe.ItemOutput>> ITEM_OUTPUTS = optionalList(ItemOutputComponent.TYPE, "item_outputs",
            ComponentRole.OUTPUT);
    public static final RecipeKey<List<MachineRecipe.FluidOutput>> FLUID_OUTPUTS = optionalList(FluidOutputComponent.TYPE, "fluid_outputs",
            ComponentRole.OUTPUT);
    public static final RecipeKey<List<MachineRecipe.ItemInput>> ITEM_INPUTS = optionalList(ItemInputComponent.TYPE, "item_inputs",
            ComponentRole.INPUT);
    public static final RecipeKey<List<MachineRecipe.FluidInput>> FLUID_INPUTS = optionalList(FluidInputComponent.TYPE, "fluid_inputs",
            ComponentRole.INPUT);
    public static final RecipeKey<List<MachineProcessCondition>> MACHINE_PROCESS_CONDITIONS = optionalList(
            MachineProcessConditionComponent.TYPE, "process_conditions", ComponentRole.OTHER);

    public static final RecipeSchema SCHEMA = new RecipeSchema(
            EU,
            DURATION,
            ITEM_OUTPUTS,
            FLUID_OUTPUTS,
            ITEM_INPUTS,
            FLUID_INPUTS,
            MACHINE_PROCESS_CONDITIONS)
                    .factory(MachineKubeRecipe.FACTORY)
                    .constructor(EU, DURATION);

    public static final RecipeSchema FORGE_HAMMER_SCHEMA = new RecipeSchema(
            ItemStackComponent.ITEM_STACK.outputKey("result"),
            IngredientComponent.INGREDIENT.inputKey("ingredient"),
            NumberComponent.INT.min(0).otherKey("damage").optional(0),
            NumberComponent.INT.min(1).otherKey("count").optional(1));
}

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
import dev.latvian.mods.kubejs.core.IngredientKJS;
import dev.latvian.mods.kubejs.core.RegistryObjectKJS;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.IngredientWrapper;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.SizedIngredientWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;

public class ItemInputComponent extends SimpleRecipeComponent<MachineRecipe.ItemInput> {
    public static final RecipeComponentType<MachineRecipe.ItemInput> TYPE = RecipeComponentType.unit(MI.id("item_input"), ItemInputComponent::new);

    public ItemInputComponent(RecipeComponentType<?> type) {
        super(type, MachineRecipe.ItemInput.CODEC, SizedIngredientWrapper.TYPE_INFO);
    }

    @Override
    public MachineRecipe.ItemInput wrap(RecipeScriptContext cx, Object from) {
        var sizedIngredient = SizedIngredientWrapper.wrap(cx.cx(), from);
        return new MachineRecipe.ItemInput(sizedIngredient.ingredient(), sizedIngredient.count(), 1);
    }

    @Override
    public boolean matches(RecipeMatchContext cx, MachineRecipe.ItemInput value, ReplacementMatchInfo match) {
        return match.match() instanceof ItemMatch m && m.matches(cx, value.ingredient(), match.exact());
    }

    @Override
    public MachineRecipe.ItemInput replace(RecipeScriptContext cx, MachineRecipe.ItemInput original, ReplacementMatchInfo match,
            Object with) {
        if (matches(cx, original, match)) {
            var withJava = IngredientWrapper.wrap(cx.cx(), with);
            return new MachineRecipe.ItemInput(withJava, original.amount(), original.probability());
        } else {
            return original;
        }
    }

    @Override
    public boolean isEmpty(MachineRecipe.ItemInput value) {
        return value.amount() <= 0 || value.ingredient().isEmpty();
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
}

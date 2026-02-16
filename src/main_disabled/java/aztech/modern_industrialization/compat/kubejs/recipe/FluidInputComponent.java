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
import dev.latvian.mods.kubejs.core.RegistryObjectKJS;
import dev.latvian.mods.kubejs.fluid.FluidWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.FluidMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;

public class FluidInputComponent extends SimpleRecipeComponent<MachineRecipe.FluidInput> {
    public static final RecipeComponentType<MachineRecipe.FluidInput> TYPE = RecipeComponentType.unit(MI.id("fluid_input"), FluidInputComponent::new);

    public FluidInputComponent(RecipeComponentType<?> type) {
        super(type, MachineRecipe.FluidInput.CODEC, FluidWrapper.SIZED_INGREDIENT_TYPE_INFO);
    }

    @Override
    public MachineRecipe.FluidInput wrap(RecipeScriptContext cx, Object from) {
        var fs = FluidWrapper.wrapSizedIngredient(cx.cx(), from);
        return new MachineRecipe.FluidInput(fs.ingredient(), fs.amount(), 1);
    }

    @Override
    public boolean matches(RecipeMatchContext cx, MachineRecipe.FluidInput value, ReplacementMatchInfo match) {
        return match.match() instanceof FluidMatch m && m.matches(cx, value.fluid(), match.exact());
    }

    @Override
    public MachineRecipe.FluidInput replace(RecipeScriptContext cx, MachineRecipe.FluidInput original, ReplacementMatchInfo match,
            Object with) {
        if (matches(cx, original, match)) {
            var fi = FluidWrapper.wrapIngredient(cx.cx(), with);
            return new MachineRecipe.FluidInput(fi, original.amount(), original.probability());
        } else {
            return original;
        }
    }

    @Override
    public boolean isEmpty(MachineRecipe.FluidInput value) {
        return value.amount() <= 0 || value.fluid().isEmpty();
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, MachineRecipe.FluidInput value) {
        if (!isEmpty(value)) {
            builder.append(((RegistryObjectKJS) value.fluid().getStacks()[0].getFluid()).kjs$getIdLocation());
        }
    }
}

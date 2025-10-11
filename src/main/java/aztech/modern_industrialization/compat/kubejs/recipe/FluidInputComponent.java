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
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.match.FluidMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import dev.latvian.mods.rhino.Context;

public class FluidInputComponent extends SimpleRecipeComponent<MachineRecipe.FluidInput> {
    public static final FluidInputComponent FLUID_INPUT = new FluidInputComponent();

    public FluidInputComponent() {
        super(MI.ID + ":fluid_input", MachineRecipe.FluidInput.CODEC, FluidWrapper.SIZED_INGREDIENT_TYPE_INFO);
    }

    @Override
    public MachineRecipe.FluidInput wrap(Context cx, KubeRecipe recipe, Object from) {
        var fs = FluidWrapper.wrapSizedIngredient(RegistryAccessContainer.of(cx), from);
        return new MachineRecipe.FluidInput(fs.ingredient(), fs.amount(), 1);
    }

    @Override
    public boolean matches(Context cx, KubeRecipe recipe, MachineRecipe.FluidInput value, ReplacementMatchInfo match) {
        return match.match() instanceof FluidMatch m && m.matches(cx, value.fluid(), match.exact());
    }

    @Override
    public MachineRecipe.FluidInput replace(Context cx, KubeRecipe recipe, MachineRecipe.FluidInput original, ReplacementMatchInfo match,
            Object with) {
        if (matches(cx, recipe, original, match)) {
            var fi = FluidWrapper.wrapIngredient(RegistryAccessContainer.of(cx), with);
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

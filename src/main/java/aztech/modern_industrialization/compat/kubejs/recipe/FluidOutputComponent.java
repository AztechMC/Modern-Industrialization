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
import dev.latvian.mods.kubejs.fluid.FluidWrapper;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.match.FluidMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import dev.latvian.mods.rhino.Context;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidOutputComponent extends SimpleRecipeComponent<MachineRecipe.FluidOutput> {
    public static final FluidOutputComponent FLUID_OUTPUT = new FluidOutputComponent();

    public FluidOutputComponent() {
        super(MI.ID + ":fluid_output", MachineRecipe.FluidOutput.CODEC, FluidWrapper.TYPE_INFO);
    }

    @Override
    public MachineRecipe.FluidOutput wrap(Context cx, KubeRecipe recipe, Object from) {
        var fs = FluidWrapper.wrap(RegistryAccessContainer.of(cx), from);
        return new MachineRecipe.FluidOutput(fs.getFluid(), fs.getAmount(), 1);
    }

    @Override
    public boolean matches(Context cx, KubeRecipe recipe, MachineRecipe.FluidOutput value, ReplacementMatchInfo match) {
        return match.match() instanceof FluidMatch m && m.matches(cx, new FluidStack(value.fluid(), 1), match.exact());
    }

    @Override
    public MachineRecipe.FluidOutput replace(Context cx, KubeRecipe recipe, MachineRecipe.FluidOutput original, ReplacementMatchInfo match,
            Object with) {
        if (matches(cx, recipe, original, match)) {
            var fs = FluidWrapper.wrap(RegistryAccessContainer.of(cx), with);
            return new MachineRecipe.FluidOutput(fs.getFluid(), original.amount(), original.probability());
        } else {
            return original;
        }
    }

    @Override
    public boolean isEmpty(MachineRecipe.FluidOutput value) {
        return value.amount() <= 0 || value.fluid().isSame(Fluids.EMPTY);
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, MachineRecipe.FluidOutput value) {
        if (!isEmpty(value)) {
            builder.append(value.fluid().builtInRegistryHolder().getKey().location());
        }
    }
}

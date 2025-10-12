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
import dev.latvian.mods.kubejs.bindings.IngredientWrapper;
import dev.latvian.mods.kubejs.bindings.SizedIngredientWrapper;
import dev.latvian.mods.kubejs.core.IngredientKJS;
import dev.latvian.mods.kubejs.core.RegistryObjectKJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.Context;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class ItemInputComponent extends SimpleRecipeComponent<MachineRecipe.ItemInput> {
    public static final ItemInputComponent ITEM_INPUT = new ItemInputComponent();

    public ItemInputComponent() {
        super(MI.ID + ":item_input", MachineRecipe.ItemInput.CODEC, SizedIngredientWrapper.TYPE_INFO);
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
            var withJava = (Ingredient) cx.jsToJava(with, IngredientJS.TYPE_INFO);
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

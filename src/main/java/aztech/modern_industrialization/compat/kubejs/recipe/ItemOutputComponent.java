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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import dev.latvian.mods.kubejs.core.ItemStackKJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.Context;
import net.minecraft.world.item.ItemStack;

public class ItemOutputComponent extends SimpleRecipeComponent<MachineRecipe.ItemOutput> {
    public static final ItemOutputComponent ITEM_OUTPUT = new ItemOutputComponent();

    public ItemOutputComponent() {
        super(MI.ID + ":item_input", MachineRecipe.ItemOutput.CODEC, ItemStackJS.TYPE_INFO);
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
            return new MachineRecipe.ItemOutput(ItemVariant.of(withJava), original.amount(), original.probability());
        } else {
            return original;
        }
    }

    @Override
    public boolean isEmpty(MachineRecipe.ItemOutput value) {
        return value.getStack().isEmpty();
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, MachineRecipe.ItemOutput value) {
        if (!value.getStack().isEmpty()) {
            builder.append(ItemStackKJS.class.cast(value.getStack()).kjs$getIdLocation());
        }
    }
}

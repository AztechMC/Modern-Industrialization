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

package aztech.modern_industrialization.client.compat.guideme.recipe;

import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.block.recipes.LytStandardRecipeBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MIRecipeTypeContributions implements RecipeTypeMappingSupplier {
    @Override
    public void collect(RecipeTypeMappings mappings) {
        for (var recipeType : MIMachineRecipeTypes.getRecipeTypes()) {
            mappings.add(recipeType, MIRecipeTypeContributions::machine);
        }
    }

    private static LytStandardRecipeBox<MachineRecipe> machine(RecipeHolder<MachineRecipe> holder) {
        var recipe = holder.value();
        for (var params : ReiMachineRecipes.categories.values()) {
            if (params.recipeType == recipe.getType() && params.recipePredicate.test(recipe)) {
                var body = new LytMachineRecipe(params, holder.value());
                body.setGap(2);
                return LytStandardRecipeBox.builder()
                        .icon(BuiltInRegistries.ITEM.get(params.workstations.getFirst()).getDefaultInstance())
                        .title(Component.translatable("rei_categories.%s.%s".formatted(params.category.getNamespace(), params.category.getPath())).getString())
                        .customBody(body)
                        .build(holder);
            }
        }
        throw new IllegalStateException("Could not find fitting machine category params for recipe " + holder.id());
    }
}

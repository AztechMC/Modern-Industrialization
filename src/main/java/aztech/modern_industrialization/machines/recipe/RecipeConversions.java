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
package aztech.modern_industrialization.machines.recipe;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class RecipeConversions {

    public static RecipeHolder<MachineRecipe> ofSmelting(RecipeHolder<SmeltingRecipe> holder, MachineRecipeType type, RegistryAccess registryAccess) {
        ResourceLocation id = new ResourceLocation(holder.id().getNamespace(), holder.id().getPath() + "_exported_mi_furnace");
        var smeltingRecipe = holder.value();
        Ingredient ingredient = smeltingRecipe.getIngredients().get(0);
        MachineRecipe recipe = new MachineRecipe(type);
        recipe.eu = 2;
        recipe.duration = smeltingRecipe.getCookingTime();
        recipe.itemInputs = Collections.singletonList(new MachineRecipe.ItemInput(ingredient, 1, 1));
        recipe.fluidInputs = Collections.emptyList();
        recipe.itemOutputs = Collections.singletonList(new MachineRecipe.ItemOutput(smeltingRecipe.getResultItem(registryAccess).getItem(), 1, 1));
        recipe.fluidOutputs = Collections.emptyList();
        return new RecipeHolder<>(id, recipe);
    }

    public static RecipeHolder<MachineRecipe> ofStonecutting(RecipeHolder<StonecutterRecipe> holder, MachineRecipeType type,
            RegistryAccess registryAccess) {
        ResourceLocation id = new ResourceLocation(holder.id().getNamespace(),
                holder.id().getPath() + "_exported_mi_cutting_machine");
        var stonecuttingRecipe = holder.value();
        MachineRecipe recipe = new MachineRecipe(type);
        recipe.eu = 2;
        recipe.duration = 200;
        recipe.itemInputs = Collections.singletonList(new MachineRecipe.ItemInput(stonecuttingRecipe.getIngredients().get(0), 1, 1));
        recipe.fluidInputs = Collections.singletonList(new MachineRecipe.FluidInput(MIFluids.LUBRICANT.asFluid(), 1, 1));
        recipe.itemOutputs = Collections
                .singletonList(
                        new MachineRecipe.ItemOutput(stonecuttingRecipe.getResultItem(null).getItem(),
                                stonecuttingRecipe.getResultItem(registryAccess).getCount(), 1));
        recipe.fluidOutputs = Collections.emptyList();
        return new RecipeHolder<>(id, recipe);
    }

    @Nullable
    public static RecipeHolder<MachineRecipe> ofCompostable(ItemLike compostable) {
        if (compostable == null || compostable.asItem() == null) {
            return null; // apparently bad mods do this
        }

        float probability = ComposterBlock.COMPOSTABLES.getOrDefault(compostable.asItem(), 0.0F);
        if (probability > 0.0F) {
            ResourceLocation id = new MIIdentifier(BuiltInRegistries.ITEM.getKey(compostable.asItem()).getPath() + "_to_plant_oil");
            MachineRecipe plantOil = new MachineRecipe(MIMachineRecipeTypes.CENTRIFUGE);
            plantOil.eu = 8;
            plantOil.duration = 200;

            plantOil.itemInputs = List.of(new MachineRecipe.ItemInput(
                    Ingredient.of(compostable),
                    1,
                    1.0f));

            plantOil.fluidInputs = Collections.emptyList();

            plantOil.fluidOutputs = List.of(new MachineRecipe.FluidOutput(
                    MIFluids.PLANT_OIL.asFluid(),
                    (long) (probability * FluidType.BUCKET_VOLUME),
                    1.0f));
            plantOil.itemOutputs = Collections.emptyList();
            return new RecipeHolder<>(id, plantOil);
        } else {
            return null;
        }
    }
}

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

import aztech.modern_industrialization.definition.FluidLike;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import java.util.Map;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

@SuppressWarnings({ "FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection" })
public class MIRecipeJson<T extends MIRecipeJson<?>> {
    protected final HolderGetter<Fluid> fluids;
    protected final HolderGetter<Item> items;
    protected final MachineRecipe recipe;

    protected MIRecipeJson(HolderGetter<Fluid> fluids, HolderGetter<Item> items, MachineRecipeType machineRecipeType, int eu, int duration) {
        this.fluids = fluids;
        this.items = items;
        this.recipe = new MachineRecipe(machineRecipeType);
        recipe.eu = eu;
        recipe.duration = duration;
    }

    protected MIRecipeJson(MIRecipeJson<?> otherWithSameData) {
        this(otherWithSameData.fluids, otherWithSameData.items, (MachineRecipeType) otherWithSameData.recipe.getType(), otherWithSameData.recipe.eu, otherWithSameData.recipe.duration);

        recipe.itemInputs.addAll(otherWithSameData.recipe.itemInputs);
        recipe.fluidInputs.addAll(otherWithSameData.recipe.fluidInputs);
        recipe.itemOutputs.addAll(otherWithSameData.recipe.itemOutputs);
        recipe.fluidOutputs.addAll(otherWithSameData.recipe.fluidOutputs);
    }

    @Deprecated(forRemoval = true)
    public T itemIn(String id, int amount) {
        return itemIn(id, amount, 1);
    }

    @Deprecated(forRemoval = true)
    public T itemIn(String id, int amount, float probability) {
        return itemIn(Ingredient.of(BuiltInRegistries.ITEM.getValueOrThrow(ResourceKey.create(Registries.ITEM, Identifier.parse(id)))), amount, probability);
    }

    public T itemIn(ItemLike item) {
        return itemIn(item, 1);
    }

    public T itemIn(ItemLike item, int amount) {
        return itemIn(item.asItem(), amount, 1);
    }

    public T itemIn(ItemLike item, int amount, float probability) {
        return itemIn(Ingredient.of(item), amount, probability);
    }

    public T itemIn(TagKey<Item> tag) {
        return itemIn(tag, 1);
    }

    public T itemIn(TagKey<Item> tag, int amount) {
        return itemIn(tag, amount, 1);
    }

    public T itemIn(TagKey<Item> tag, int amount, float probability) {
        return itemIn(Ingredient.of(items.getOrThrow(tag)), amount, probability);
    }

    public T itemIn(Ingredient ingredient) {
        return itemIn(ingredient, 1);
    }

    public T itemIn(Ingredient ingredient, int amount) {
        return itemIn(ingredient, amount, 1);
    }

    public T itemIn(Ingredient ingredient, int amount, float probability) {
        recipe.itemInputs.add(new MachineRecipe.ItemInput(ingredient, amount, probability));
        return (T) this;
    }

    @Deprecated(forRemoval = true)
    public T itemOut(String itemId, int amount) {
        return itemOut(itemId, amount, 1);
    }

    @Deprecated(forRemoval = true)
    public T itemOut(String itemId, int amount, float probability) {
        return itemOut(BuiltInRegistries.ITEM.getValueOrThrow(ResourceKey.create(Registries.ITEM, Identifier.parse(itemId))), amount, probability);
    }

    public T itemOut(ItemLike item) {
        return itemOut(item, 1);
    }

    public T itemOut(ItemLike item, int amount) {
        return itemOut(item, amount, 1);
    }

    public T itemOut(ItemLike item, int amount, float probability) {
        return itemOut(new ItemStackTemplate(item.asItem(), amount), probability);
    }

    public T itemOut(ItemStackTemplate output, float probability) {
        recipe.itemOutputs.add(new MachineRecipe.ItemOutput(output, probability));
        return (T) this;
    }

    @Deprecated(forRemoval = true)
    public T fluidIn(String fluid, int amount) {
        return fluidIn(fluid, amount, 1);
    }

    @Deprecated(forRemoval = true)
    public T fluidIn(String fluid, int amount, float probability) {
        return fluidIn(BuiltInRegistries.FLUID.getValue(Identifier.parse(fluid)), amount, probability);
    }

    public T fluidIn(FluidLike fluid, int amount) {
        return fluidIn(fluid.asFluid(), amount);
    }

    public T fluidIn(FluidLike fluid, int amount, float probability) {
        return fluidIn(fluid.asFluid(), amount, probability);
    }

    public T fluidIn(Fluid fluid, int amount) {
        return fluidIn(fluid, amount, 1);
    }

    public T fluidIn(Fluid fluid, int amount, float probability) {
        return fluidIn(FluidIngredient.of(fluid), amount, probability);
    }

    public T fluidIn(TagKey<Fluid> tag, int amount) {
        return fluidIn(tag, amount, 1);
    }

    public T fluidIn(TagKey<Fluid> tag, int amount, float probability) {
        return fluidIn(FluidIngredient.of(fluids.getOrThrow(tag)), amount, probability);
    }

    public T fluidIn(FluidIngredient ingredient, int amount) {
        return fluidIn(ingredient, amount, 1);
    }

    public T fluidIn(FluidIngredient ingredient, int amount, float probability) {
        recipe.fluidInputs.add(new MachineRecipe.FluidInput(ingredient, amount, probability));
        return (T) this;
    }

    @Deprecated(forRemoval = true)
    public T fluidOut(String fluid, int amount) {
        return fluidOut(fluid, amount, 1);
    }

    public T fluidOut(FluidLike fluid, int amount) {
        return fluidOut(fluid.asFluid(), amount);
    }

    public T fluidOut(FluidLike fluid, int amount, float probability) {
        return fluidOut(fluid.asFluid(), amount);
    }

    @Deprecated(forRemoval = true)
    public T fluidOut(String fluid, int amount, float probability) {
        return fluidOut(BuiltInRegistries.FLUID.getValue(Identifier.parse(fluid)), amount, probability);
    }

    public T fluidOut(Fluid fluid, int amount) {
        return fluidOut(fluid, amount, 1);
    }

    public T fluidOut(Fluid fluid, int amount, float probability) {
        Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id.equals(BuiltInRegistries.FLUID.getDefaultKey())) {
            throw new RuntimeException("Could not find id for fluid " + fluid);
        }
        recipe.fluidOutputs.add(new MachineRecipe.FluidOutput(fluid, amount, probability));
        return (T) this;
    }

    public static MIRecipeJson<?> assemblerFromShaped(ShapedRecipe recipe) {
        return fromShaped(
                MIMachineRecipeTypes.ASSEMBLER,
                8, 200, 1,
                recipe.result,
                recipe.pattern.data.get().pattern().toArray(String[]::new),
                recipe.pattern.data.get().key());
    }

    public static MIRecipeJson<?> fromShaped(
            MachineRecipeType machine,
            int eu, int duration, int division,
            ItemStackTemplate result,
            String[] pattern,
            Map<Character, Ingredient> key) {
        if (result.count() % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }

        MIRecipeJson<?> assemblerJson = new MIRecipeJson<>(null, null, machine, eu, duration).itemOut(result.item().value(), result.count() / division);
        for (Map.Entry<Character, Ingredient> entry : key.entrySet()) {
            int count = 0;
            for (String row : pattern) {
                for (char c : row.toCharArray()) {
                    if (c == entry.getKey()) {
                        count++;
                    }
                }
            }

            if (count % division != 0) {
                throw new IllegalArgumentException("Input must be divisible by division");
            }

            Ingredient input = entry.getValue();
            assemblerJson.itemIn(input, count / division, 1);
        }

        return assemblerJson;
    }
}

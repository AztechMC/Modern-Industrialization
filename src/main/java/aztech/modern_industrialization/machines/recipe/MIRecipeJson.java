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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings({ "FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection" })
public class MIRecipeJson<T extends MIRecipeJson<?>> {
    protected final MachineRecipe recipe;

    protected MIRecipeJson(MachineRecipeType machineRecipeType, int eu, int duration) {
        this.recipe = new MachineRecipe(machineRecipeType);
        recipe.eu = eu;
        recipe.duration = duration;
    }

    protected MIRecipeJson(MIRecipeJson<?> otherWithSameData) {
        this((MachineRecipeType) otherWithSameData.recipe.getType(), otherWithSameData.recipe.eu, otherWithSameData.recipe.duration);

        for (MachineRecipe.ItemInput itemInput : otherWithSameData.recipe.itemInputs) {
            recipe.itemInputs.add(new MachineRecipe.ItemInput(itemInput.ingredient, itemInput.amount, itemInput.probability));
        }

        for (MachineRecipe.FluidInput fluidInput : otherWithSameData.recipe.fluidInputs) {
            recipe.fluidInputs.add(new MachineRecipe.FluidInput(fluidInput.fluid, fluidInput.amount, fluidInput.probability));
        }

        for (MachineRecipe.ItemOutput itemOutput : otherWithSameData.recipe.itemOutputs) {
            recipe.itemOutputs.add(new MachineRecipe.ItemOutput(itemOutput.item, itemOutput.amount, itemOutput.probability));
        }

        for (MachineRecipe.FluidOutput fluidOutput : otherWithSameData.recipe.fluidOutputs) {
            recipe.fluidOutputs.add(new MachineRecipe.FluidOutput(fluidOutput.fluid, fluidOutput.amount, fluidOutput.probability));
        }
    }

    public static MIRecipeJson<MIRecipeJson<?>> create(MachineRecipeType machineRecipeType, int eu, int duration) {
        return new MIRecipeJson<>(machineRecipeType, eu, duration);
    }

    public T addItemInput(TagKey<Item> tag, int amount) {
        return addItemInput("#" + tag.location().toString(), amount);
    }

    public T addItemInput(String maybeTag, int amount) {
        return addItemInput(maybeTag, amount, 1);
    }

    public T addItemInput(ItemLike item, int amount, float probability) {
        return addItemInput(BuiltInRegistries.ITEM.getKey(item.asItem()).toString(), amount, probability);
    }

    public T addItemInput(ItemLike item, int amount) {
        return addItemInput(item.asItem(), amount, 1);
    }

    public T addItemInput(String maybeTag, int amount, float probability) {
        Ingredient ing;
        if (maybeTag.startsWith("#")) {
            ing = Ingredient.of(ItemTags.create(new ResourceLocation(maybeTag.substring(1))));
        } else {
            if (!BuiltInRegistries.ITEM.containsKey(new ResourceLocation(maybeTag))) {
                throw new RuntimeException("Could not find item " + maybeTag);
            }
            ing = Ingredient.of(BuiltInRegistries.ITEM.get(new ResourceLocation(maybeTag)));
        }
        return addItemInput(ing, amount, probability);
    }

    public T addItemInput(Ingredient ingredient, int amount, float probability) {
        recipe.itemInputs.add(new MachineRecipe.ItemInput(ingredient, amount, probability));
        return (T) this;
    }

    public T addItemOutput(String itemId, int amount) {
        return addItemOutput(itemId, amount, 1);
    }

    public T addItemOutput(String itemId, int amount, float probability) {
        recipe.itemOutputs.add(new MachineRecipe.ItemOutput(BuiltInRegistries.ITEM.get(new ResourceLocation(itemId)), amount, probability));
        return (T) this;
    }

    public T addItemOutput(ItemLike item, int amount, float probability) {
        return addItemOutput(BuiltInRegistries.ITEM.getKey(item.asItem()).toString(), amount, probability);
    }

    public T addItemOutput(ItemLike item, int amount) {
        return addItemOutput(BuiltInRegistries.ITEM.getKey(item.asItem()).toString(), amount);
    }

    public T addItemOutput(Item item, int amount) {
        return addItemOutput(item, amount, 1);
    }

    public T addFluidInput(String fluid, int amount) {
        return addFluidInput(fluid, amount, 1);
    }

    public T addFluidInput(String fluid, int amount, float probability) {
        recipe.fluidInputs.add(new MachineRecipe.FluidInput(BuiltInRegistries.FLUID.get(new ResourceLocation(fluid)), amount, probability));
        return (T) this;
    }

    public T addFluidInput(Fluid fluid, int amount, float probability) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id.equals(BuiltInRegistries.FLUID.getDefaultKey())) {
            throw new RuntimeException("Could not find id for fluid " + fluid);
        }
        return addFluidInput(id.toString(), amount, probability);
    }

    public T addFluidInput(FluidLike fluid, int amount, float probability) {
        return addFluidInput(fluid.asFluid(), amount, probability);
    }

    public T addFluidInput(FluidLike fluid, int amount) {
        return addFluidInput(fluid.asFluid(), amount);
    }

    public T addFluidInput(Fluid fluid, int amount) {
        return addFluidInput(fluid, amount, 1);
    }

    public T addFluidOutput(String fluid, int amount) {
        return addFluidOutput(fluid, amount, 1);
    }

    public T addFluidOutput(FluidLike fluid, int amount) {
        return addFluidOutput(fluid.asFluid(), amount);
    }

    public T addFluidOutput(String fluid, int amount, float probability) {
        recipe.fluidOutputs.add(new MachineRecipe.FluidOutput(BuiltInRegistries.FLUID.get(new ResourceLocation(fluid)), amount, probability));
        return (T) this;
    }

    public T addFluidOutput(Fluid fluid, int amount) {
        return addFluidOutput(fluid, amount, 1);
    }

    public T addFluidOutput(Fluid fluid, int amount, float probability) {
        return addFluidOutput(BuiltInRegistries.FLUID.getKey(fluid).toString(), amount, probability);
    }

    public static MIRecipeJson<?> assemblerFromShaped(ShapedRecipe recipe) {
        return fromShaped(
                MIMachineRecipeTypes.ASSEMBLER,
                8, 200, 1,
                recipe.result,
                recipe.pattern.data().get().pattern().toArray(String[]::new),
                recipe.pattern.data().get().key());
    }

    public static MIRecipeJson<?> fromShaped(
            MachineRecipeType machine,
            int eu, int duration, int division,
            ItemStack result,
            String[] pattern,
            Map<Character, Ingredient> key) {
        if (result.getCount() % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }

        MIRecipeJson<?> assemblerJson = MIRecipeJson.create(machine, eu, duration).addItemOutput(result.getItem(), result.getCount() / division);
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
            assemblerJson.addItemInput(input, count / division, 1);
        }

        return assemblerJson;
    }
}

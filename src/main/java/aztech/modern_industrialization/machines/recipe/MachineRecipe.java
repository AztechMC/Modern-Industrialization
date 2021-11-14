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

import aztech.modern_industrialization.util.DefaultedListWrapper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class MachineRecipe implements Recipe<Inventory> {
    final Identifier id;
    final MachineRecipeType type;

    public int eu;
    public int duration;
    public List<ItemInput> itemInputs;
    public List<FluidInput> fluidInputs;
    public List<ItemOutput> itemOutputs;
    public List<FluidOutput> fluidOutputs;

    MachineRecipe(Identifier id, MachineRecipeType type) {
        this.id = id;
        this.type = type;
    }

    public long getTotalEu() {
        return (long) eu * duration;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public boolean matches(Inventory inv, World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack craft(Inventory inv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean fits(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        // This function is implemented for AE2 pattern shift-clicking compat.
        // This is the reason the counts of the ItemStacks in the ingredient are
        // modified.
        // (They should never be used somewhere else anyway)
        return new DefaultedListWrapper<>(itemInputs.stream().filter(i -> i.probability == 1).map(i -> {
            for (ItemStack stack : i.ingredient.getMatchingStacks()) {
                stack.setCount(i.amount);
            }
            return i.ingredient;
        }).collect(Collectors.toList()));
    }

    @Override
    public ItemStack getOutput() {
        for (ItemOutput o : itemOutputs) {
            if (o.probability == 1) {
                return new ItemStack(o.item, o.amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return type;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    public static class ItemInput {
        public final Ingredient ingredient;
        public final int amount;
        public final float probability;

        public ItemInput(Ingredient ingredient, int amount, float probability) {
            this.ingredient = ingredient;
            this.amount = amount;
            this.probability = probability;
        }

        public boolean matches(ItemStack otherStack) {
            return ingredient.test(otherStack);
        }

        public List<Item> getInputItems() {
            return Arrays.stream(ingredient.getMatchingStacks()).map(ItemStack::getItem).distinct().collect(Collectors.toList());
        }

        public List<ItemStack> getInputStacks() {
            return Arrays.asList(ingredient.getMatchingStacks());
        }
    }

    public static class FluidInput {
        public final Fluid fluid;
        public final long amount;
        public final float probability;

        public FluidInput(Fluid fluid, long amount, float probability) {
            this.fluid = fluid;
            this.amount = amount;
            this.probability = probability;
        }
    }

    public static class ItemOutput {
        public final Item item;
        public final int amount;
        public final float probability;

        public ItemOutput(Item item, int amount, float probability) {
            this.item = item;
            this.amount = amount;
            this.probability = probability;
        }
    }

    public static class FluidOutput {
        public final Fluid fluid;
        public final long amount;
        public final float probability;

        public FluidOutput(Fluid fluid, long amount, float probability) {
            this.fluid = fluid;
            this.amount = amount;
            this.probability = probability;
        }
    }
}

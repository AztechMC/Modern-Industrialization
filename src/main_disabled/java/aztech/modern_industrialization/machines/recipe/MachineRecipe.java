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

import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.util.DefaultedListWrapper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public class MachineRecipe implements Recipe<Container> {
    final ResourceLocation id;
    final MachineRecipeType type;

    public int eu; // Also used for forge hammer damage
    public int duration;
    public List<ItemInput> itemInputs;
    public List<FluidInput> fluidInputs;
    public List<ItemOutput> itemOutputs;
    public List<FluidOutput> fluidOutputs;
    public List<MachineProcessCondition> conditions = List.of();

    MachineRecipe(ResourceLocation id, MachineRecipeType type) {
        this.id = id;
        this.type = type;
    }

    public long getTotalEu() {
        return (long) eu * duration;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(Container inv, Level world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        // This function is implemented for AE2 pattern shift-clicking compat.
        // This is the reason the counts of the ItemStacks in the ingredient are
        // modified.
        // (They should never be used somewhere else anyway)
        return new DefaultedListWrapper<>(itemInputs.stream().filter(i -> i.probability == 1).map(i -> {
            for (ItemStack stack : i.ingredient.getItems()) {
                stack.setCount(i.amount);
            }
            return i.ingredient;
        }).collect(Collectors.toList()));
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        for (ItemOutput o : itemOutputs) {
            if (o.probability == 1) {
                return new ItemStack(o.item, o.amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
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

    public boolean conditionsMatch(MachineProcessCondition.Context context) {
        for (var condition : conditions) {
            if (!condition.canProcessRecipe(context, this)) {
                return false;
            }
        }
        return true;
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
            return Arrays.stream(ingredient.getItems()).map(ItemStack::getItem).distinct().collect(Collectors.toList());
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

        public ItemStack getStack() {
            return new ItemStack(item, amount);
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

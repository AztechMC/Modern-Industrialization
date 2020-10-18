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

import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import java.util.Arrays;
import java.util.List;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class MachineRecipe implements Recipe<MachineBlockEntity> {
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

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public boolean matches(MachineBlockEntity inv, World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack craft(MachineBlockEntity inv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean fits(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getOutput() {
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
        public final Item item;
        public final Tag<Item> tag;
        public final int amount;
        public final float probability;

        public ItemInput(Item item, int amount, float probability) {
            this.item = item;
            this.tag = null;
            this.amount = amount;
            this.probability = probability;
        }

        public ItemInput(Tag<Item> tag, int amount, float probability) {
            this.item = null;
            this.tag = tag;
            this.amount = amount;
            this.probability = probability;
        }

        public boolean matches(ItemStack otherStack) {
            return item == null ? tag.contains(otherStack.getItem()) : otherStack.getItem() == item;
        }

        public boolean matches(Item otherItem) {
            return item == null ? tag.contains(otherItem) : otherItem == item;
        }

        Iterable<Item> getInputItems() {
            return tag == null ? Arrays.asList(item) : tag.values();
        }
    }

    public static class FluidInput {
        public final Fluid fluid;
        public final int amount;
        public final float probability;

        public FluidInput(Fluid fluid, int amount, float probability) {
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
        public final int amount;
        public final float probability;

        public FluidOutput(Fluid fluid, int amount, float probability) {
            this.fluid = fluid;
            this.amount = amount;
            this.probability = probability;
        }
    }
}

package aztech.modern_industrialization.machines.recipe;

import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

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

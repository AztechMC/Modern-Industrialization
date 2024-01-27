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
import aztech.modern_industrialization.util.MIExtraCodecs;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public class MachineRecipe implements Recipe<Container> {
    public static MapCodec<MachineRecipe> codec(MachineRecipeType type) {
        return RecordCodecBuilder.mapCodec(
                g -> g
                        .group(
                                ExtraCodecs.POSITIVE_INT.fieldOf("eu").forGetter(recipe -> recipe.eu),
                                ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(recipe -> recipe.duration),
                                MIExtraCodecs.maybeList(ItemInput.CODEC, "item_inputs").forGetter(recipe -> recipe.itemInputs),
                                MIExtraCodecs.maybeList(FluidInput.CODEC, "fluid_inputs").forGetter(recipe -> recipe.fluidInputs),
                                MIExtraCodecs.maybeList(ItemOutput.CODEC, "item_outputs").forGetter(recipe -> recipe.itemOutputs),
                                MIExtraCodecs.maybeList(FluidOutput.CODEC, "fluid_outputs").forGetter(recipe -> recipe.fluidOutputs),
                                MIExtraCodecs.maybeList(MachineProcessCondition.CODEC, "conditions").forGetter(recipe -> recipe.conditions))
                        .apply(g, (eu, duration, itemInputs, fluidInputs, itemOutputs, fluidOutputs, conditions) -> {
                            var ret = new MachineRecipe(type);
                            ret.eu = eu;
                            ret.duration = duration;
                            ret.itemInputs = itemInputs;
                            ret.fluidInputs = fluidInputs;
                            ret.itemOutputs = itemOutputs;
                            ret.fluidOutputs = fluidOutputs;
                            ret.conditions = conditions;
                            return ret;
                        }));

    }

    final MachineRecipeType type;

    public int eu; // Also used for forge hammer damage
    public int duration;
    public List<ItemInput> itemInputs = new ArrayList<>();
    public List<FluidInput> fluidInputs = new ArrayList<>();
    public List<ItemOutput> itemOutputs = new ArrayList<>();
    public List<FluidOutput> fluidOutputs = new ArrayList<>();
    public List<MachineProcessCondition> conditions = new ArrayList<>();

    MachineRecipe(MachineRecipeType type) {
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
        private static final MapCodec<Ingredient> INGREDIENT_CODEC = MIExtraCodecs
                .xor(
                        MIExtraCodecs.xor(
                                ItemStack.SINGLE_ITEM_CODEC.fieldOf("item"),
                                TagKey.codec(Registries.ITEM).fieldOf("tag")),
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient"))
                .xmap(
                        des -> {
                            return des.map(x -> x.map(Ingredient::of, tagKey -> ingredientFromTagKey(tagKey)), x -> x);
                        },
                        ing -> {
                            if (ing.values.length == 1) {
                                if (ing.values[0] instanceof Ingredient.ItemValue itemValue) {
                                    return Either.left(Either.left(itemValue.item()));
                                } else if (ing.values[0] instanceof Ingredient.TagValue tagValue) {
                                    return Either.left(Either.right(tagValue.tag()));
                                }
                            }
                            return Either.right(ing);
                        });

        /**
         * Sadly, Ingredient.of(tagKey) resolves the ingredient to check if it's empty for some reason.
         */
        private static Ingredient ingredientFromTagKey(TagKey<Item> tagKey) {
            return new Ingredient(Stream.of(new Ingredient.TagValue(tagKey))) {
            };
        }

        private static final MapCodec<Integer> AMOUNT_CODEC = NeoForgeExtraCodecs
                .mapWithAlternative(
                        ExtraCodecs.strictOptionalField(ExtraCodecs.POSITIVE_INT, "amount"),
                        ExtraCodecs.strictOptionalField(ExtraCodecs.POSITIVE_INT, "count"))
                .xmap(
                        deserialized -> deserialized.orElse(1),
                        Optional::of);

        public static final Codec<ItemInput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        INGREDIENT_CODEC.forGetter(itemInput -> itemInput.ingredient),
                        AMOUNT_CODEC.forGetter(itemInput -> itemInput.amount),
                        ExtraCodecs.strictOptionalField(MIExtraCodecs.FLOAT_01, "probability", 1f).forGetter(itemInput -> itemInput.probability))
                        .apply(g, ItemInput::new));

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
        public static final Codec<FluidInput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(fluidInput -> fluidInput.fluid),
                        MIExtraCodecs.optionalFieldAlwaysWrite(MIExtraCodecs.POSITIVE_LONG, "amount", 1L).forGetter(fluidInput -> fluidInput.amount),
                        ExtraCodecs.strictOptionalField(MIExtraCodecs.FLOAT_01, "probability", 1f).forGetter(fluidInput -> fluidInput.probability))
                        .apply(g, FluidInput::new));

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
        public static final Codec<ItemOutput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(itemOutput -> itemOutput.item),
                        MIExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "amount", 1).forGetter(itemOutput -> itemOutput.amount),
                        ExtraCodecs.strictOptionalField(MIExtraCodecs.FLOAT_01, "probability", 1f).forGetter(itemOutput -> itemOutput.probability))
                        .apply(g, ItemOutput::new));

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
        public static final Codec<FluidOutput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(fluidOutput -> fluidOutput.fluid),
                        MIExtraCodecs.optionalFieldAlwaysWrite(MIExtraCodecs.POSITIVE_LONG, "amount", 1L)
                                .forGetter(fluidOutput -> fluidOutput.amount),
                        ExtraCodecs.strictOptionalField(MIExtraCodecs.FLOAT_01, "probability", 1f).forGetter(fluidOutput -> fluidOutput.probability))
                        .apply(g, FluidOutput::new));

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

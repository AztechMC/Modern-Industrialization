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
import aztech.modern_industrialization.util.MIExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public class MachineRecipe implements Recipe<RecipeInput> {
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
                                MIExtraCodecs.maybeList(MachineProcessCondition.CODEC, "process_conditions").forGetter(recipe -> recipe.conditions))
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

    public static StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec(MachineRecipeType type) {
        return StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                r -> r.eu,
                ByteBufCodecs.VAR_INT,
                r -> r.duration,
                ItemInput.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.itemInputs,
                FluidInput.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.fluidInputs,
                ItemOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.itemOutputs,
                FluidOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.fluidOutputs,
                MachineProcessCondition.STREAM_CODEC.apply(ByteBufCodecs.list()),
                r -> r.conditions,
                (eu, duration, itemInputs, fluidInputs, itemOutputs, fluidOutputs, conditions) -> {
                    var ret = new MachineRecipe(type);
                    ret.eu = eu;
                    ret.duration = duration;
                    ret.itemInputs = itemInputs;
                    ret.fluidInputs = fluidInputs;
                    ret.itemOutputs = itemOutputs;
                    ret.fluidOutputs = fluidOutputs;
                    ret.conditions = conditions;
                    return ret;
                });
    }

    final MachineRecipeType type;

    public int eu;
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
    public boolean matches(RecipeInput recipeInput, Level world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<MachineRecipe> getSerializer() {
        return type.serializer;
    }

    @Override
    public RecipeType<MachineRecipe> getType() {
        return type;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        throw new UnsupportedOperationException();
    }

    public boolean conditionsMatch(MachineProcessCondition.Context context) {
        for (var condition : conditions) {
            if (!condition.canProcessRecipe(context, this)) {
                return false;
            }
        }
        return true;
    }

    private static final MapCodec<Integer> AMOUNT_CODEC = NeoForgeExtraCodecs
            .mapWithAlternative(
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount"),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("count"))
            .xmap(
                    deserialized -> deserialized.orElse(1),
                    Optional::of);

    public record ItemInput(Ingredient ingredient, int amount, float probability) {
        public static final Codec<ItemInput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemInput::ingredient),
                        AMOUNT_CODEC.forGetter(ItemInput::amount),
                        MIExtraCodecs.FLOAT_01.optionalFieldOf("probability", 1f).forGetter(ItemInput::probability))
                        .apply(g, ItemInput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemInput> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                ItemInput::ingredient,
                ByteBufCodecs.VAR_INT,
                ItemInput::amount,
                ByteBufCodecs.FLOAT,
                ItemInput::probability,
                ItemInput::new);

        public boolean matches(ItemStack otherStack) {
            return ingredient.test(otherStack);
        }

        public List<Item> getInputItems() {
            return ingredient.items().map(Holder::value).distinct().collect(Collectors.toList());
        }
    }

    public record FluidInput(FluidIngredient fluid, int amount, float probability) {
        @Deprecated(forRemoval = true)
        public FluidInput(Fluid fluid, int amount, float probability) {
            this(FluidIngredient.of(fluid), amount, probability);
        }

        public static final Codec<FluidInput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        FluidIngredient.CODEC.fieldOf("ingredient").forGetter(FluidInput::fluid),
                        NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "amount", 1).forGetter(FluidInput::amount),
                        MIExtraCodecs.FLOAT_01.optionalFieldOf("probability", 1f).forGetter(FluidInput::probability))
                        .apply(g, FluidInput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidInput> STREAM_CODEC = StreamCodec.composite(
                FluidIngredient.STREAM_CODEC,
                FluidInput::fluid,
                ByteBufCodecs.VAR_INT,
                FluidInput::amount,
                ByteBufCodecs.FLOAT,
                FluidInput::probability,
                FluidInput::new);

        public List<Fluid> getInputFluids() {
            return fluid.fluids().stream().map(Holder::value).distinct().collect(Collectors.toList());
        }
    }

    public record ItemOutput(ItemStackTemplate template, float probability) {
        private static final Codec<Holder<Item>> ITEM_NON_AIR_CODEC = BuiltInRegistries.ITEM
                .holderByNameCodec()
                .validate(item -> item.is(Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> "Item must not be minecraft:air") : DataResult.success(item));
        public static final Codec<ItemOutput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        ITEM_NON_AIR_CODEC.fieldOf("id")
                                .forGetter(itemOutput -> itemOutput.template.item()),
                        AMOUNT_CODEC.forGetter(itemOutput -> itemOutput.template.count()),
                        DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                                .forGetter(itemOutput -> itemOutput.template.components()),
                        MIExtraCodecs.FLOAT_01.optionalFieldOf("probability", 1f)
                                .forGetter(itemOutput -> itemOutput.probability))
                        .apply(g, (item, count, components, probability) -> new ItemOutput(new ItemStackTemplate(item, count, components), probability)));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemOutput> STREAM_CODEC = StreamCodec.composite(
                ItemStackTemplate.STREAM_CODEC,
                ItemOutput::template,
                ByteBufCodecs.FLOAT,
                ItemOutput::probability,
                ItemOutput::new);

        public ItemStack getStack() {
            return template.create();
        }
    }

    public record FluidOutput(Fluid fluid, int amount, float probability) {
        public static final Codec<FluidOutput> CODEC = RecordCodecBuilder.create(
                g -> g.group(
                        BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(fluidOutput -> fluidOutput.fluid),
                        NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "amount", 1)
                                .forGetter(fluidOutput -> fluidOutput.amount),
                        MIExtraCodecs.FLOAT_01.optionalFieldOf("probability", 1f).forGetter(fluidOutput -> fluidOutput.probability))
                        .apply(g, FluidOutput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidOutput> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.FLUID),
                FluidOutput::fluid,
                ByteBufCodecs.VAR_INT,
                FluidOutput::amount,
                ByteBufCodecs.FLOAT,
                FluidOutput::probability,
                FluidOutput::new);
    }
}

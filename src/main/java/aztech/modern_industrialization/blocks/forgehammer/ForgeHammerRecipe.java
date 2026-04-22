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

package aztech.modern_industrialization.blocks.forgehammer;

import aztech.modern_industrialization.MIRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record ForgeHammerRecipe(
        Ingredient ingredient,
        int count,
        ItemStackTemplate result,
        int hammerDamage) implements Recipe<RecipeInput> {
    private static final MapCodec<ForgeHammerRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ForgeHammerRecipe::ingredient),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(ForgeHammerRecipe::count),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ForgeHammerRecipe::result),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("damage", 0).forGetter(ForgeHammerRecipe::hammerDamage))
                    .apply(instance, ForgeHammerRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ForgeHammerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            ForgeHammerRecipe::ingredient,
            ByteBufCodecs.VAR_INT,
            ForgeHammerRecipe::count,
            ItemStackTemplate.STREAM_CODEC,
            ForgeHammerRecipe::result,
            ByteBufCodecs.VAR_INT,
            ForgeHammerRecipe::hammerDamage,
            ForgeHammerRecipe::new);

    public static final RecipeSerializer<ForgeHammerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

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
    public RecipeSerializer<ForgeHammerRecipe> getSerializer() {
        return MIRegistries.FORGE_HAMMER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<ForgeHammerRecipe> getType() {
        return MIRegistries.FORGE_HAMMER_RECIPE_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        throw new UnsupportedOperationException();
    }
}

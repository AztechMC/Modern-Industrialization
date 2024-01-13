package aztech.modern_industrialization.blocks.forgehammer;

import aztech.modern_industrialization.MIRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public record ForgeHammerRecipe(
        Ingredient ingredient,
        int count,
        ItemStack result,
        int hammerDamage) implements Recipe<Container> {

    private static final Codec<ForgeHammerRecipe> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ForgeHammerRecipe::ingredient),
                    ExtraCodecs.strictOptionalField(ExtraCodecs.POSITIVE_INT, "count", 1).forGetter(ForgeHammerRecipe::count),
                    ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(ForgeHammerRecipe::result),
                    ExtraCodecs.strictOptionalField(ExtraCodecs.NON_NEGATIVE_INT, "damage", 0).forGetter(ForgeHammerRecipe::hammerDamage)
            ).apply(instance, ForgeHammerRecipe::new));

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
        return NonNullList.copyOf(List.of(ingredient));
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MIRegistries.FORGE_HAMMER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MIRegistries.FORGE_HAMMER_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ForgeHammerRecipe> {
        @Override
        public Codec<ForgeHammerRecipe> codec() {
            return CODEC;
        }

        @Override
        public ForgeHammerRecipe fromNetwork(FriendlyByteBuf buf) {
            return new ForgeHammerRecipe(
                    Ingredient.fromNetwork(buf),
                    buf.readVarInt(),
                    buf.readItem(),
                    buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ForgeHammerRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeVarInt(recipe.count);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.hammerDamage);
        }
    }
}

package aztech.modern_industrialization.recipe.json;

import aztech.modern_industrialization.MI;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;

public class ShapelessRecipeBuilder implements IMIRecipeBuilder {
    private final ItemStack resultStack;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();

    public ShapelessRecipeBuilder(ItemLike pResult, int pCount) {
        this(new ItemStack(pResult, pCount));
    }

    public ShapelessRecipeBuilder(ItemStack result) {
        this.resultStack = result;
    }

    /**
     * Creates a new builder for a shapeless recipe.
     */
    public static ShapelessRecipeBuilder shapeless(ItemLike pResult) {
        return new ShapelessRecipeBuilder(pResult, 1);
    }

    /**
     * Creates a new builder for a shapeless recipe.
     */
    public static ShapelessRecipeBuilder shapeless(ItemLike pResult, int pCount) {
        return new ShapelessRecipeBuilder(pResult, pCount);
    }

    public static ShapelessRecipeBuilder shapeless(ItemStack result) {
        return new ShapelessRecipeBuilder(result);
    }

    /**
     * Adds an ingredient that can be any item in the given tag.
     */
    public ShapelessRecipeBuilder requires(TagKey<Item> pTag) {
        return this.requires(Ingredient.of(pTag));
    }

    /**
     * Adds an ingredient of the given item.
     */
    public ShapelessRecipeBuilder requires(ItemLike pItem) {
        return this.requires(pItem, 1);
    }

    /**
     * Adds the given ingredient multiple times.
     */
    public ShapelessRecipeBuilder requires(ItemLike pItem, int pQuantity) {
        for(int i = 0; i < pQuantity; ++i) {
            this.requires(Ingredient.of(pItem));
        }

        return this;
    }

    /**
     * Adds an ingredient.
     */
    public ShapelessRecipeBuilder requires(Ingredient pIngredient) {
        return this.requires(pIngredient, 1);
    }

    /**
     * Adds an ingredient multiple times.
     */
    public ShapelessRecipeBuilder requires(Ingredient pIngredient, int pQuantity) {
        for(int i = 0; i < pQuantity; ++i) {
            this.ingredients.add(pIngredient);
        }

        return this;
    }

    public ShapelessRecipe buildRecipe() {
        return new ShapelessRecipe("", CraftingBookCategory.MISC, this.resultStack, ingredients);
    }

    @Override
    public void offerTo(RecipeOutput output, String path) {
        output.accept(MI.id(path), buildRecipe(), null);
    }
}

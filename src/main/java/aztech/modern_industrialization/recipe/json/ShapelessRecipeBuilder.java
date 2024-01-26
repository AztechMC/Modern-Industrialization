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
        for (int i = 0; i < pQuantity; ++i) {
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
        for (int i = 0; i < pQuantity; ++i) {
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

package aztech.modern_industrialization.materials.recipe;

import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.recipe.builder.SmeltingRecipeBuilder;

import static aztech.modern_industrialization.materials.part.MIParts.*;
import static aztech.modern_industrialization.materials.part.MIParts.INGOT;

/**
 * Standard smelting and blasting recipes for materials that can be smelted with regular (blast) furnaces.
 */
public final class SmeltingRecipes {
    public static void apply(MaterialBuilder.RecipeContext ctx) {
        for (boolean blasting : new boolean[] { true, false }) {
            new SmeltingRecipeBuilder(ctx, TINY_DUST, NUGGET, 0.08, blasting);
            new SmeltingRecipeBuilder(ctx, CRUSHED_DUST, INGOT, 0.7, blasting);
            new SmeltingRecipeBuilder(ctx, DUST, INGOT, 0.7, blasting);
            new SmeltingRecipeBuilder(ctx, ORE, INGOT, 0.7, blasting);
        }
    }

    private SmeltingRecipes() {
    }
}

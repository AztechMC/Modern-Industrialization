package aztech.modern_industrialization.materials.recipe;

import aztech.modern_industrialization.materials.MaterialBuilder;

import static aztech.modern_industrialization.materials.part.MIParts.*;

/**
 * Standard conversion recipes for all materials.
 */
public final class StandardRecipes {
    public static void apply(MaterialBuilder.RecipeContext ctx) {
        addMaceratorRecycling(ctx, DOUBLE_INGOT, 18);
        addMaceratorRecycling(ctx, PLATE, 9);
        addMaceratorRecycling(ctx, CURVED_PLATE, 9);
        addMaceratorRecycling(ctx, NUGGET, 1);
        addMaceratorRecycling(ctx, LARGE_PLATE, 36);
        addMaceratorRecycling(ctx, GEAR, 18);
        addMaceratorRecycling(ctx, RING, 4);
        addMaceratorRecycling(ctx, BOLT, 2);
        addMaceratorRecycling(ctx, ROD, 4);
        addMaceratorRecycling(ctx, ITEM_PIPE, 9);
        addMaceratorRecycling(ctx, FLUID_PIPE, 9);
        addMaceratorRecycling(ctx, ROTOR, 27);
        addMaceratorRecycling(ctx, INGOT, 9);
        addMaceratorRecycling(ctx, BLADE, 5);
        new MIRecipeBuilder(ctx, "macerator", "ore").addTaggedPartInput(ORE, 1).addPartOutput(CRUSHED_DUST, 2);
        new MIRecipeBuilder(ctx, "macerator", "crushed_dust").addTaggedPartInput(CRUSHED_DUST, 2).addPartOutput(DUST, 3);
    }

    /**
     * Add a recycling recipe in the macerator.
     */
    private static void addMaceratorRecycling(MaterialBuilder.RecipeContext ctx, String partInput, int tinyDustOutput) {
        MIRecipeBuilder builder = new MIRecipeBuilder(ctx, "macerator", partInput);
        builder.addPartInput(partInput, 1);
        if (tinyDustOutput % 9 == 0) {
            builder.addPartOutput(DUST, tinyDustOutput / 9);
        } else {
            builder.addPartOutput(TINY_DUST, tinyDustOutput);
        }
    }

    private StandardRecipes() {
    }
}

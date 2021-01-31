package aztech.modern_industrialization.materials.recipe;

import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;

import static aztech.modern_industrialization.materials.part.MIParts.*;

/**
 * Standard forge hammer recipes for early-game materials.
 */
public class ForgeHammerRecipes {
    public static void apply(MaterialBuilder.RecipeContext ctx) {
        addHammer(ctx, DOUBLE_INGOT, 1, PLATE, 1);
        addHammer(ctx, INGOT, 2, DOUBLE_INGOT, 1);
        addHammer(ctx, LARGE_PLATE, 1, CURVED_PLATE, 3);
        addHammer(ctx, NUGGET, 1, TINY_DUST, 1);
        addHammer(ctx, ORE, 1, CRUSHED_DUST, 2);
        addSaw(ctx, INGOT, ROD);
        addSaw(ctx, ITEM_PIPE, RING);
        addSaw(ctx, LARGE_PLATE, GEAR);
        addSaw(ctx, ROD, BOLT);
    }

    private static void addHammer(MaterialBuilder.RecipeContext ctx, String inputPart, int inputCount, String outputPart, int outputCount) {
        addRecipe("forge_hammer_hammer", ctx, inputPart, inputCount, outputPart, outputCount);
    }

    private static void addSaw(MaterialBuilder.RecipeContext ctx, String inputPart, String outputPart) {
        addRecipe("forge_hammer_saw", ctx, inputPart, 1, outputPart, 1);
    }

    private static void addRecipe(String type, MaterialBuilder.RecipeContext ctx, String inputPart, int inputCount, String outputPart, int outputCount) {
        new MIRecipeBuilder(ctx, type, outputPart).addTaggedPartInput(inputPart, inputCount).addPartOutput(outputPart, outputCount);
    }

    private ForgeHammerRecipes() {
    }
}

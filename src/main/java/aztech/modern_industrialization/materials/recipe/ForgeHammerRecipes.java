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
package aztech.modern_industrialization.materials.recipe;

import static aztech.modern_industrialization.materials.part.MIParts.*;

import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.part.Part;
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;

/**
 * Standard forge hammer recipes for early-game materials.
 */
public class ForgeHammerRecipes {
    public static void apply(MaterialBuilder.RecipeContext ctx) {

        addRecipe(ctx, INGOT, 1, DUST, 1);

        addRecipe(ctx, INGOT, 2, DOUBLE_INGOT, 1);

        addRecipe(ctx, INGOT, 1, PLATE, 1, 20);
        addRecipe(ctx, INGOT, 2, PLATE, 1);
        addRecipe(ctx, INGOT, 2, CURVED_PLATE, 1);
        addRecipe(ctx, INGOT, 1, CURVED_PLATE, 1, 40);
        addRecipe(ctx, INGOT, 1, ROD, 1);
        addRecipe(ctx, INGOT, 1, ROD, 2, 20);
        addRecipe(ctx, INGOT, 1, RING, 1);
        addRecipe(ctx, INGOT, 1, RING, 2, 60);
        addRecipe(ctx, INGOT, 1, BOLT, 2);
        addRecipe(ctx, INGOT, 1, BOLT, 4, 60);

        addRecipe(ctx, DOUBLE_INGOT, 1, PLATE, 2, 20);
        addRecipe(ctx, DOUBLE_INGOT, 1, PLATE, 1);
        addRecipe(ctx, DOUBLE_INGOT, 1, CURVED_PLATE, 1);
        addRecipe(ctx, DOUBLE_INGOT, 1, CURVED_PLATE, 2, 60);
        addRecipe(ctx, DOUBLE_INGOT, 1, ROD, 2);
        addRecipe(ctx, DOUBLE_INGOT, 1, ROD, 4, 20);
        addRecipe(ctx, DOUBLE_INGOT, 1, RING, 2);
        addRecipe(ctx, DOUBLE_INGOT, 1, RING, 4, 100);
        addRecipe(ctx, DOUBLE_INGOT, 1, BOLT, 4);
        addRecipe(ctx, DOUBLE_INGOT, 1, BOLT, 8, 100);

        addRecipe(ctx, PLATE, 1, CURVED_PLATE, 1, 20);

        addRecipe(ctx, ROD, 1, BOLT, 2, 20);
        addRecipe(ctx, ROD, 1, RING, 1, 20);

        addRecipe(ctx, ORE, 1, CRUSHED_DUST, 2);
        addRecipe(ctx, ORE, 1, CRUSHED_DUST, 3, 20);
        addRecipe(ctx, ORE, 1, RAW_METAL, 2);
        addRecipe(ctx, ORE, 1, RAW_METAL, 3, 20);

        addRecipe(ctx, ORE, 1, DUST, 4, 50);
    }

    private static void addRecipe(MaterialBuilder.RecipeContext ctx, Part inputPart, int inputCount, Part outputPart,
            int outputCount, int cost) {

        String recipeName = inputPart.key + "_to_" + outputPart.key + ((cost == 0) ? "" : "_with_tool");

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.FORGE_HAMMER, recipeName, (int) ((cost) * ctx.getHardness().timeFactor),
                0).addTaggedPartInput(inputPart, inputCount).addPartOutput(outputPart, outputCount);
    }

    private static void addRecipe(MaterialBuilder.RecipeContext ctx, Part inputPart, int inputCount, Part outputPart,
            int outputCount) {
        addRecipe(ctx, inputPart, inputCount, outputPart, outputCount, 0);
    }

}

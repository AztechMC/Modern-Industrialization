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

import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;

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
        addHammer(ctx, ORE, 1, RAW_METAL, 2);
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

    private static void addRecipe(String type, MaterialBuilder.RecipeContext ctx, String inputPart, int inputCount, String outputPart,
            int outputCount) {
        new MIRecipeBuilder(ctx, type, outputPart).addTaggedPartInput(inputPart, inputCount).addPartOutput(outputPart, outputCount);
    }

    private ForgeHammerRecipes() {
    }
}

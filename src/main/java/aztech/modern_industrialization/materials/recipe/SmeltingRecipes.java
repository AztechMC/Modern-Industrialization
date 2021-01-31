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
import static aztech.modern_industrialization.materials.part.MIParts.INGOT;

import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.recipe.builder.SmeltingRecipeBuilder;

/**
 * Standard smelting and blasting recipes for materials that can be smelted with
 * regular (blast) furnaces.
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

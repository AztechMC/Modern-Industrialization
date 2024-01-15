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
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;
import aztech.modern_industrialization.materials.recipe.builder.SmeltingRecipeBuilder;

/**
 * Standard smelting and blasting recipes for materials that can be smelted with
 * regular (blast) furnaces.
 */
public final class SmeltingRecipes {
    public static void apply(MaterialBuilder.RecipeContext ctx) {
        SmeltingRecipeBuilder.smeltAndBlast(ctx, TINY_DUST, NUGGET, 0.08f);
        SmeltingRecipeBuilder.smeltAndBlast(ctx, CRUSHED_DUST, INGOT, 0.7f);
        SmeltingRecipeBuilder.smeltAndBlast(ctx, DUST, INGOT, 0.7f);
        SmeltingRecipeBuilder.smeltAndBlast(ctx, ORE, INGOT, 0.7f);
        SmeltingRecipeBuilder.smeltAndBlast(ctx, ORE_DEEPSLATE, INGOT, 0.7f);
        SmeltingRecipeBuilder.smeltAndBlast(ctx, RAW_METAL, INGOT, 0.7f);
        SmeltingRecipeBuilder.smeltAndBlast(ctx, ORE, GEM, 0.7f);
        SmeltingRecipeBuilder.smeltAndBlast(ctx, ORE_DEEPSLATE, GEM, 0.7f);
    }

    public static void applyBlastFurnace(MaterialBuilder.RecipeContext ctx, boolean hotIngot, int eu, int duration) {
        if (hotIngot) {
            new MIRecipeBuilder(ctx, MIMachineRecipeTypes.BLAST_FURNACE, "dust", eu, duration).addTaggedPartInput(DUST, 1).addPartOutput(HOT_INGOT,
                    1);
        } else {
            new MIRecipeBuilder(ctx, MIMachineRecipeTypes.BLAST_FURNACE, "dust", eu, duration).addTaggedPartInput(DUST, 1).addPartOutput(INGOT, 1);
            new MIRecipeBuilder(ctx, MIMachineRecipeTypes.BLAST_FURNACE, "tiny_dust", eu, duration / 10).addTaggedPartInput(TINY_DUST, 1)
                    .addPartOutput(NUGGET, 1);
        }

    }

    public static void applyBlastFurnace(MaterialBuilder.RecipeContext ctx, boolean hotIngot, int eu) {
        applyBlastFurnace(ctx, hotIngot, eu, 200);
    }

    public static void applyBlastFurnace(MaterialBuilder.RecipeContext ctx, int eu) {
        applyBlastFurnace(ctx, false, eu);
    }

    public static void applyBlastFurnace(MaterialBuilder.RecipeContext ctx) {
        applyBlastFurnace(ctx, 32);
    }

    private SmeltingRecipes() {
    }
}

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

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.materials.MaterialBuilder;

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

        new ShapedRecipeBuilder(ctx, BLADE, 4).setPattern(new String[][] { { PLATE }, { PLATE }, { ROD } }).exportToAssembler();
        new ShapedRecipeBuilder(ctx, ROTOR, 1).setPattern(new String[][] { { BOLT, BLADE, BOLT }, { BLADE, RING, BLADE }, { BOLT, BLADE, BOLT } });

        new MIRecipeBuilder(ctx, "assembler", ROTOR).addPartOutput(ROTOR, 1).addPartInput(BLADE, 4).addPartInput(RING, 1);

        new ShapedRecipeBuilder(ctx, LARGE_PLATE, 1).setPattern(new String[][] { { PLATE, PLATE }, { PLATE, PLATE } }).exportToMachine("packer");

        new ShapedRecipeBuilder(ctx, ITEM_PIPE, 3)
                .setPattern(
                        new String[][] { { CURVED_PLATE, CURVED_PLATE, CURVED_PLATE }, { "", "", "" }, { CURVED_PLATE, CURVED_PLATE, CURVED_PLATE } })
                .exportToMachine("packer", 3);
        new ShapedRecipeBuilder(ctx, FLUID_PIPE, 3)
                .setPattern(new String[][] { { CURVED_PLATE, CURVED_PLATE, CURVED_PLATE },
                        { "minecraft:glass_pane", "minecraft:glass_pane", "minecraft:glass_pane" }, { CURVED_PLATE, CURVED_PLATE, CURVED_PLATE } })
                .exportToMachine("packer", 3);
        new ShapedRecipeBuilder(ctx, CABLE, 3)
                .setPattern(new Object[][] { { MIItem.ITEM_RUBBER_SHEET, MIItem.ITEM_RUBBER_SHEET, MIItem.ITEM_RUBBER_SHEET }, { WIRE, WIRE, WIRE },
                        { MIItem.ITEM_RUBBER_SHEET, MIItem.ITEM_RUBBER_SHEET, MIItem.ITEM_RUBBER_SHEET } })
                .exportToAssembler();

        /*
         * new ShapedRecipeBuilder(ctx, INGOT, 1).setPattern(new String[][] {{NUGGET,
         * NUGGET, NUGGET},{NUGGET, NUGGET, NUGGET}, {NUGGET, NUGGET,
         * NUGGET}}).exportToMachine("packer"); new ShapedRecipeBuilder(ctx, NUGGET,
         * 9).setPattern(new String[][] {{INGOT}}); new ShapedRecipeBuilder(ctx, BLOCK,
         * 1).setPattern(new String[][] {{INGOT, INGOT, INGOT},{INGOT, INGOT, INGOT},
         * {INGOT, INGOT, INGOT}}).exportToMachine("packer"); new
         * ShapedRecipeBuilder(ctx, INGOT, 9).setPattern(new String[][] {{BLOCK}});
         */
        new ShapedRecipeBuilder(ctx, DUST, 1).setPattern(
                new String[][] { { TINY_DUST, TINY_DUST, TINY_DUST }, { TINY_DUST, TINY_DUST, TINY_DUST }, { TINY_DUST, TINY_DUST, TINY_DUST } })
                .exportToMachine("packer");
        new ShapedRecipeBuilder(ctx, TINY_DUST, 9).setPattern(new String[][] { { DUST } });

        new ShapedRecipeBuilder(ctx, COIL, 1).setPattern(new String[][] { { WIRE, WIRE, WIRE }, { WIRE, "", WIRE }, { WIRE, WIRE, WIRE } })
                .exportToAssembler();

        for (boolean blasting : new boolean[] { true, false }) {
            new SmeltingRecipeBuilder(ctx, TINY_DUST, NUGGET, 0.08, blasting);
            new SmeltingRecipeBuilder(ctx, CRUSHED_DUST, INGOT, 0.7, blasting);
            new SmeltingRecipeBuilder(ctx, DUST, INGOT, 0.7, blasting);
            // new SmeltingRecipeBuilder(ctx, ORE, INGOT, 0.7, blasting);
        }

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

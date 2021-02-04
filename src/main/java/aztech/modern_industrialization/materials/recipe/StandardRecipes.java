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
import aztech.modern_industrialization.materials.recipe.builder.ShapedRecipeBuilder;

/**
 * Standard conversion recipes for all materials.
 */
public final class StandardRecipes {
    public static void apply(MaterialBuilder.RecipeContext ctx) {
        // CRAFTING
        add3By3Crafting(ctx, "tiny_dust", "dust");
        add3By3Crafting(ctx, "nugget", "ingot");
        add3By3Crafting(ctx, "ingot", "block");
        new ShapedRecipeBuilder(ctx, BLADE, 4, "blade", "P", "P", "I").addTaggedPart('P', PLATE).addTaggedPart('I', ROD).exportToAssembler();
        new ShapedRecipeBuilder(ctx, COIL, 1, "coil", "xxx", "x x", "xxx").addTaggedPart('x', WIRE).exportToAssembler();
        new ShapedRecipeBuilder(ctx, LARGE_PLATE, 1, "large_plate", "xx", "xx").addTaggedPart('x', PLATE).exportToMachine("packer");
        new ShapedRecipeBuilder(ctx, ROTOR, 1, "rotor", "bBb", "BRB", "bBb").addTaggedPart('b', BOLT).addTaggedPart('B', BLADE).addTaggedPart('R',
                RING);
        new ShapedRecipeBuilder(ctx, ITEM_PIPE, 6, "item_pipe", "ccc", "   ", "ccc").addTaggedPart('c', CURVED_PLATE).exportToMachine("packer");
        new ShapedRecipeBuilder(ctx, FLUID_PIPE, 6, "fluid_pipe", "ccc", "ggg", "ccc").addTaggedPart('c', CURVED_PLATE)
                .addPart('g', "minecraft:glass_pane").exportToMachine("packer", 3);
        new ShapedRecipeBuilder(ctx, CABLE, 3, "cable", "rrr", "www", "rrr").addInput('r', "modern_industrialization:rubber_sheet")
                .addTaggedPart('w', WIRE).exportToAssembler();
        // MACERATOR
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
        // COMPRESSOR
        new MIRecipeBuilder(ctx, "compressor", "main").addTaggedPartInput(INGOT, 1).addPartOutput(PLATE, 1);
        new MIRecipeBuilder(ctx, "compressor", "plate").addTaggedPartInput(PLATE, 1).addPartOutput(CURVED_PLATE, 1);
        new MIRecipeBuilder(ctx, "compressor", "double_ingot").addTaggedPartInput(DOUBLE_INGOT, 1).addPartOutput(PLATE, 2);
        // CUTTING MACHINE
        addCuttingMachine(ctx, "main", INGOT, ROD, 2);
        addCuttingMachine(ctx, "rod", ROD, BOLT, 2);
        addCuttingMachine(ctx, "large_plate", LARGE_PLATE, GEAR, 2);
        addCuttingMachine(ctx, "item_pipe", ITEM_PIPE, RING, 2);
        // PACKER
        new MIRecipeBuilder(ctx, "packer", "double_ingot").addTaggedPartInput(INGOT, 2).addPartOutput(DOUBLE_INGOT, 1);
        new MIRecipeBuilder(ctx, "packer", "fluid_pipe").addTaggedPartInput(ITEM_PIPE, 2).addItemInput("minecraft:glass_pane", 1)
                .addPartOutput(FLUID_PIPE, 2);
        new MIRecipeBuilder(ctx, "packer", "dust").addTaggedPartInput(TINY_DUST, 9).addPartOutput(DUST, 1);
        new MIRecipeBuilder(ctx, "packer", "ingot").addTaggedPartInput(NUGGET, 9).addPartOutput(INGOT, 1);
        // WIREMILL
        new MIRecipeBuilder(ctx, "wiremill", "wire").addTaggedPartInput(PLATE, 1).addPartOutput(WIRE, 2);
        new MIRecipeBuilder(ctx, "wiremill", "fine_wire").addTaggedPartInput(WIRE, 1).addPartOutput(FINE_WIRE, 4);
        // EXTRA ASSEMBLER
        new MIRecipeBuilder(ctx, "assembler", "rotor").addTaggedPartInput(BLADE, 4).addTaggedPartInput(RING, 1).addPartOutput(ROTOR, 1);
    }

    /**
     * Add a recycling recipe in the macerator.
     */
    private static void addMaceratorRecycling(MaterialBuilder.RecipeContext ctx, String partInput, int tinyDustOutput) {
        MIRecipeBuilder builder = new MIRecipeBuilder(ctx, "macerator", partInput);
        builder.addTaggedPartInput(partInput, 1);
        if (tinyDustOutput % 9 == 0) {
            builder.addPartOutput(DUST, tinyDustOutput / 9);
        } else {
            builder.addPartOutput(TINY_DUST, tinyDustOutput);
        }
    }

    /**
     * Add 3x3 -> 1 and 1 -> 9 crafting recipes.
     */
    private static void add3By3Crafting(MaterialBuilder.RecipeContext ctx, String smallPart, String bigPart) {
        new ShapedRecipeBuilder(ctx, bigPart, 1, bigPart + "_from_" + smallPart, "yxx", "xxx", "xxx").addPart('y', smallPart).addTaggedPart('x',
                smallPart);
        new ShapedRecipeBuilder(ctx, smallPart, 9, smallPart + "_from_" + bigPart, "x").addPart('x', bigPart);
    }

    private static void addCuttingMachine(MaterialBuilder.RecipeContext ctx, String name, String inputPart, String outputPart, int amount) {
        new MIRecipeBuilder(ctx, "cutting_machine", name).addTaggedPartInput(inputPart, 1).addPartOutput(outputPart, amount)
                .addFluidInput("minecraft:water", 1);
    }

    private StandardRecipes() {
    }
}

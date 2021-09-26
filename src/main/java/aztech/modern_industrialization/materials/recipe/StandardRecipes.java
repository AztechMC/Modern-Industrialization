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

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;
import aztech.modern_industrialization.materials.recipe.builder.ShapedRecipeBuilder;
import aztech.modern_industrialization.materials.recipe.builder.SmeltingRecipeBuilder;

/**
 * Standard conversion recipes for all materials.
 */
public final class StandardRecipes {

    public static void apply(MaterialBuilder.RecipeContext ctx) {

        // CRAFTING
        add3By3Crafting(ctx, "tiny_dust", "dust", true);
        add3By3Crafting(ctx, "nugget", "ingot", true);
        add3By3Crafting(ctx, ctx.getMainPart(), "block", false); // Not in packer due to conflicts with double ingots.
        add3By3Crafting(ctx, "raw_metal", "raw_metal_block", true);

        new ShapedRecipeBuilder(ctx, BLADE, 4, "blade", "P", "P", "I").addTaggedPart('P', CURVED_PLATE).addTaggedPart('I', ROD)
                .exportToMachine("packer");

        new ShapedRecipeBuilder(ctx, ORE, 1, "deeplsate_to_ore", "   ", " x ", "   ").addPart('x', ORE_DEEPLSATE);

        new ShapedRecipeBuilder(ctx, COIL, 1, "coil", "xxx", "x x", "xxx").addTaggedPart('x', WIRE).exportToAssembler();
        new ShapedRecipeBuilder(ctx, LARGE_PLATE, 1, "large_plate", "xx", "xx").addTaggedPart('x', PLATE).exportToMachine("packer");

        new ShapedRecipeBuilder(ctx, ROTOR, 1, "rotor", "bBb", "BRB", "bBb").addTaggedPart('b', BOLT).addTaggedPart('B', BLADE).addTaggedPart('R',
                RING);

        new ShapedRecipeBuilder(ctx, GEAR, 1, "gear", "PbP", "bRb", "PbP").addTaggedPart('b', BOLT).addTaggedPart('P', PLATE).addTaggedPart('R',
                RING);

        new ShapedRecipeBuilder(ctx, RING, 2, "ring", "bRb", "R R", "bRb").addTaggedPart('b', BOLT).addTaggedPart('R', ROD);

        new ShapedRecipeBuilder(ctx, CABLE, 3, "cable", "rrr", "www", "rrr").addInput('r', "modern_industrialization:rubber_sheet")
                .addTaggedPart('w', WIRE).exportToMachine("packer");

        if (MIConfig.getConfig().enableEasyMode) {
            new ShapedRecipeBuilder(ctx, TANK, 1, "tank", "###", "#G#", "###").addTaggedPart('#', PLATE).addInput('G', "minecraft:glass")
                    .exportToAssembler();
            new ShapedRecipeBuilder(ctx, BARREL, 1, "barrel", "###", "#b#", "###").addTaggedPart('#', PLATE).addInput('b', "minecraft:barrel")
                    .exportToAssembler();
        } else {
            new ShapedRecipeBuilder(ctx, TANK, 1, "tank", "###", "#G#", "###").addPart('#', LARGE_PLATE).addInput('G', "minecraft:glass")
                    .exportToAssembler();

            new ShapedRecipeBuilder(ctx, BARREL, 1, "barrel", "###", "#b#", "###").addPart('#', LARGE_PLATE).addInput('b', "minecraft:barrel")
                    .exportToAssembler();
        }

        new ShapedRecipeBuilder(ctx, DRILL_HEAD, 1, "drill_head", "bcp", "GRc", "bGb").addTaggedPart('G', GEAR).addPart('b', BOLT)
                .addPart('c', CURVED_PLATE).addPart('R', ROD).addTaggedPart('p', PLATE);

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
        addMaceratorRecycling(ctx, ROTOR, 27);
        if (!ctx.getMainPart().equals(DUST)) {
            addMaceratorRecycling(ctx, ctx.getMainPart(), 9);
        }
        addMaceratorRecycling(ctx, BLADE, 5);
        addMaceratorRecycling(ctx, DRILL_HEAD, 7 * 9 + 4);
        addMaceratorRecycling(ctx, WIRE, 4);

        new MIRecipeBuilder(ctx, "macerator", "ore_to_crushed").addTaggedPartInput(ORE, 1).addPartOutput(CRUSHED_DUST, 3);
        new MIRecipeBuilder(ctx, "macerator", "ore_to_raw").addTaggedPartInput(ORE, 1).addPartOutput(RAW_METAL, 3);

        new MIRecipeBuilder(ctx, "macerator", "crushed_dust", 2, (int) (100 * ctx.getHardness().timeFactor)).addTaggedPartInput(CRUSHED_DUST, 1)
                .addPartOutput(DUST, 1).addPartOutput(DUST, 1, 0.5);
        new MIRecipeBuilder(ctx, "macerator", "raw_metal", 2, (int) (100 * ctx.getHardness().timeFactor)).addTaggedPartInput(RAW_METAL, 1)
                .addPartOutput(DUST, 1).addPartOutput(DUST, 1, 0.5);
        // COMPRESSOR
        new MIRecipeBuilder(ctx, "compressor", "main").addTaggedPartInput(ctx.getMainPart(), 1).addPartOutput(PLATE, 1);
        new MIRecipeBuilder(ctx, "compressor", "plate").addTaggedPartInput(PLATE, 1).addPartOutput(CURVED_PLATE, 1);
        new MIRecipeBuilder(ctx, "compressor", "double_ingot").addTaggedPartInput(DOUBLE_INGOT, 1).addPartOutput(PLATE, 2);
        new MIRecipeBuilder(ctx, "compressor", "ring").addTaggedPartInput(ROD, 1).addPartOutput(RING, 1);
        // CUTTING MACHINE
        addCuttingMachine(ctx, "main", ctx.getMainPart(), ROD, 2);
        addCuttingMachine(ctx, "double_ingot", DOUBLE_INGOT, ROD, 4);
        addCuttingMachine(ctx, "rod", ROD, BOLT, 2);
        // PACKER
        new MIRecipeBuilder(ctx, "packer", "block").addTaggedPartInput(ctx.getMainPart(), 9)
                .addInput("modern_industrialization:packer_block_template", 1, 0.0).addPartOutput(BLOCK, 1);
        new MIRecipeBuilder(ctx, "packer", "double_ingot").addTaggedPartInput(INGOT, 2)
                .addInput("modern_industrialization:packer_double_ingot_template", 1, 0.0).addPartOutput(DOUBLE_INGOT, 1);

        new MIRecipeBuilder(ctx, "packer", "fuel_rod_double").addPartInput(FUEL_ROD, 2).addInput("#c:nuclear_alloy_plates", 1)
                .addPartOutput(FUEL_ROD_DOUBLE, 1);

        new MIRecipeBuilder(ctx, "packer", "fuel_rod_quad").addInput("#c:nuclear_alloy_plates", 2).addPartInput(FUEL_ROD_DOUBLE, 2)
                .addPartOutput(FUEL_ROD_QUAD, 1);

        // WIREMILL
        new MIRecipeBuilder(ctx, "wiremill", "wire").addTaggedPartInput(PLATE, 1).addPartOutput(WIRE, 2);
        new MIRecipeBuilder(ctx, "wiremill", "fine_wire").addTaggedPartInput(WIRE, 1).addPartOutput(FINE_WIRE, 4);
        // EXTRA ASSEMBLER
        new MIRecipeBuilder(ctx, "assembler", "rotor").addTaggedPartInput(BLADE, 4).addTaggedPartInput(RING, 1)
                .addFluidInput("modern_industrialization:soldering_alloy", 100).addPartOutput(ROTOR, 1);
        new MIRecipeBuilder(ctx, "assembler", "gear").addTaggedPartInput(PLATE, 4).addTaggedPartInput(RING, 1)
                .addFluidInput("modern_industrialization:soldering_alloy", 100).addPartOutput(GEAR, 2);
        new MIRecipeBuilder(ctx, "assembler", "drill_head").addTaggedPartInput(PLATE, 1).addTaggedPartInput(CURVED_PLATE, 2)
                .addTaggedPartInput(ROD, 1).addTaggedPartInput(GEAR, 2).addFluidInput("modern_industrialization:soldering_alloy", 75)
                .addPartOutput(DRILL_HEAD, 1);

        new MIRecipeBuilder(ctx, "assembler", "fuel_rod", 16, 200).addInput("modern_industrialization:blastproof_alloy_curved_plate", 2)
                .addInput("modern_industrialization:large_motor", 1).addInput("modern_industrialization:robot_arm", 2).addTaggedPartInput(ROD, 2)
                .addFluidInput("modern_industrialization:soldering_alloy", 500).addFluidInput("modern_industrialization:helium", 100)
                .addPartOutput(FUEL_ROD, 1);

        // HEAT EXCHANGER
        new MIRecipeBuilder(ctx, "heat_exchanger", "hot_ingot", 8, 10).addPartInput(HOT_INGOT, 1).addFluidInput(MIFluids.CRYOFLUID.id, 100)
                .addPartOutput(INGOT, 1);

        new MIRecipeBuilder(ctx, "polarizer", "rod_magnetic", 8, 200).addTaggedPartInput(ROD, 1).addPartOutput(ROD_MAGNETIC, 1);
        new MIRecipeBuilder(ctx, "polarizer", "wire_magnetic", 8, 200).addTaggedPartInput(WIRE, 1).addPartOutput(WIRE_MAGNETIC, 1);
        new SmeltingRecipeBuilder(ctx, ROD_MAGNETIC, ROD, 0.0, false);
        new SmeltingRecipeBuilder(ctx, WIRE_MAGNETIC, WIRE, 0.0, false);

        new MIRecipeBuilder(ctx, "vacuum_freezer", "hot_ingot", 32, 250).addTaggedPartInput(HOT_INGOT, 1).addPartOutput(INGOT, 1);

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
    private static void add3By3Crafting(MaterialBuilder.RecipeContext ctx, String smallPart, String bigPart, boolean packer) {
        new ShapedRecipeBuilder(ctx, bigPart, 1, bigPart + "_from_" + smallPart, "yxx", "xxx", "xxx").addPart('y', smallPart).addTaggedPart('x',
                smallPart);
        new ShapedRecipeBuilder(ctx, smallPart, 9, smallPart + "_from_" + bigPart, "x").addPart('x', bigPart);
        if (packer) {
            new MIRecipeBuilder(ctx, "packer", bigPart).addTaggedPartInput(smallPart, 9).addPartOutput(bigPart, 1);
        }
        new MIRecipeBuilder(ctx, "unpacker", smallPart).addTaggedPartInput(bigPart, 1).addPartOutput(smallPart, 9);

    }

    private static void addCuttingMachine(MaterialBuilder.RecipeContext ctx, String name, String inputPart, String outputPart, int amount) {
        new MIRecipeBuilder(ctx, "cutting_machine", name).addTaggedPartInput(inputPart, 1).addPartOutput(outputPart, amount)
                .addFluidInput("modern_industrialization:lubricant", 1);
    }

}

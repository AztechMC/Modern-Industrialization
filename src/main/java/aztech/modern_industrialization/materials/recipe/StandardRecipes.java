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
import static aztech.modern_industrialization.materials.property.MaterialProperty.HARDNESS;
import static aztech.modern_industrialization.materials.property.MaterialProperty.MAIN_PART;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.part.PartKeyProvider;
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;
import aztech.modern_industrialization.materials.recipe.builder.ShapedRecipeBuilder;
import aztech.modern_industrialization.materials.recipe.builder.SmeltingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

/**
 * Standard conversion recipes for all materials.
 */
public final class StandardRecipes {

    public static void apply(MaterialBuilder.RecipeContext ctx) {

        // CRAFTING
        add3By3Crafting(ctx, TINY_DUST, DUST, true);
        add3By3Crafting(ctx, NUGGET, INGOT, true);
        add3By3Crafting(ctx, ctx.get(MAIN_PART), BLOCK, false); // Not in packer due to conflicts with double ingots.
        add3By3Crafting(ctx, RAW_METAL, RAW_METAL_BLOCK, true);

        new ShapedRecipeBuilder(ctx, BLADE, 4, "blade", "P", "P", "I").addTaggedPart('P', CURVED_PLATE).addTaggedPart('I', ROD)
                .exportToMachine(MIMachineRecipeTypes.PACKER);

        new ShapedRecipeBuilder(ctx, ORE, 1, "deepslate_to_ore", "   ", " x ", "   ").addPart('x', ORE_DEEPSLATE);

        new ShapedRecipeBuilder(ctx, COIL, 1, "coil", "xxx", "x x", "xxx").addTaggedPart('x', CABLE).exportToAssembler();
        new ShapedRecipeBuilder(ctx, LARGE_PLATE, 1, "large_plate", "xx", "xx").addTaggedPart('x', PLATE)
                .exportToMachine(MIMachineRecipeTypes.PACKER);

        new ShapedRecipeBuilder(ctx, ROTOR, 1, "rotor", "bBb", "BRB", "bBb").addTaggedPart('b', BOLT).addTaggedPart('B', BLADE).addTaggedPart('R',
                RING);

        new ShapedRecipeBuilder(ctx, GEAR, 1, "gear", "PbP", "bRb", "PbP").addTaggedPart('b', BOLT).addTaggedPart('P', PLATE).addTaggedPart('R',
                RING);

        new ShapedRecipeBuilder(ctx, HAMMER, 1, "hammer", "ppp", "psp", " s ").addTaggedPart('p', LARGE_PLATE).addInput('s', Items.STICK);

        new ShapedRecipeBuilder(ctx, RING, 2, "ring", "bRb", "R R", "bRb").addTaggedPart('b', BOLT).addTaggedPart('R', ROD);

        new ShapedRecipeBuilder(ctx, CABLE, 3, "cable", "rrr", "www", "rrr").addInput('r', MIItem.RUBBER_SHEET)
                .addTaggedPart('w', WIRE).exportToMachine(MIMachineRecipeTypes.PACKER);

        new ShapedRecipeBuilder(ctx, TANK, 1, "tank", "###", "#G#", "###").addTaggedPart('#', PLATE).addInput('G', Tags.Items.GLASS)
                .exportToAssembler();
        new ShapedRecipeBuilder(ctx, BARREL, 1, "barrel", "###", "#b#", "###")
                .addTaggedPart('#', PLATE)
                .addInput('b', "#forge:barrels/wooden")
                .exportToAssembler();

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
        if (!ctx.get(MAIN_PART).equals(DUST)) {
            addMaceratorRecycling(ctx, ctx.get(MAIN_PART), 9);
        }
        addMaceratorRecycling(ctx, BLADE, 5);
        addMaceratorRecycling(ctx, DRILL_HEAD, 7 * 9 + 4);
        addMaceratorRecycling(ctx, WIRE, 4);

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.MACERATOR, "ore_to_crushed").addTaggedPartInput(ORE, 1).addPartOutput(CRUSHED_DUST, 3);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.MACERATOR, "ore_to_raw").addTaggedPartInput(ORE, 1).addPartOutput(RAW_METAL, 3);

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.MACERATOR, "crushed_dust", 2, (int) (100 * ctx.get(HARDNESS).timeFactor))
                .addTaggedPartInput(CRUSHED_DUST, 1)
                .addPartOutput(DUST, 1).addPartOutput(DUST, 1, 0.5f);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.MACERATOR, "raw_metal", 2, (int) (100 * ctx.get(HARDNESS).timeFactor))
                .addTaggedPartInput(RAW_METAL, 1)
                .addPartOutput(DUST, 1).addPartOutput(DUST, 1, 0.5f);
        // COMPRESSOR
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.COMPRESSOR, "main").addTaggedPartInput(ctx.get(MAIN_PART), 1).addPartOutput(PLATE, 1);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.COMPRESSOR, "plate").addTaggedPartInput(PLATE, 1).addPartOutput(CURVED_PLATE, 1);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.COMPRESSOR, "double_ingot").addTaggedPartInput(DOUBLE_INGOT, 1).addPartOutput(PLATE, 2);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.COMPRESSOR, "ring").addTaggedPartInput(ROD, 1).addPartOutput(RING, 1);
        // CUTTING MACHINE
        addCuttingMachine(ctx, "main", ctx.get(MAIN_PART), ROD, 2);
        addCuttingMachine(ctx, "double_ingot", DOUBLE_INGOT, ROD, 4);
        addCuttingMachine(ctx, "rod", ROD, BOLT, 2);
        // PACKER
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.PACKER, "block").addTaggedPartInput(ctx.get(MAIN_PART), 9)
                .addItemInput(MIItem.PACKER_BLOCK_TEMPLATE, 1, 0.0f).addPartOutput(BLOCK, 1);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.PACKER, "double_ingot").addTaggedPartInput(INGOT, 2)
                .addItemInput(MIItem.PACKER_DOUBLE_INGOT_TEMPLATE, 1, 0.0f).addPartOutput(DOUBLE_INGOT, 1);

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.PACKER, "fuel_rod_double").addPartInput(FUEL_ROD, 2)
                .addItemInput("#forge:plates/nuclear_alloy", 1)
                .addPartOutput(FUEL_ROD_DOUBLE, 1);

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.PACKER, "fuel_rod_quad").addItemInput("#forge:plates/nuclear_alloy", 2)
                .addPartInput(FUEL_ROD_DOUBLE, 2)
                .addPartOutput(FUEL_ROD_QUAD, 1);
        // UNPACKER
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.UNPACKER, "coil").addTaggedPartInput(COIL, 1).addPartOutput(CABLE, 8);

        // WIREMILL
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.WIREMILL, "wire").addTaggedPartInput(PLATE, 1).addPartOutput(WIRE, 2);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.WIREMILL, "fine_wire").addTaggedPartInput(WIRE, 1).addPartOutput(FINE_WIRE, 4);
        // EXTRA ASSEMBLER
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.ASSEMBLER, "rotor").addTaggedPartInput(BLADE, 4).addTaggedPartInput(RING, 1)
                .addFluidInput(MIFluids.SOLDERING_ALLOY, 100).addPartOutput(ROTOR, 1);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.ASSEMBLER, "gear").addTaggedPartInput(PLATE, 4).addTaggedPartInput(RING, 1)
                .addFluidInput(MIFluids.SOLDERING_ALLOY, 100).addPartOutput(GEAR, 2);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.ASSEMBLER, "drill_head").addTaggedPartInput(PLATE, 1).addTaggedPartInput(CURVED_PLATE, 2)
                .addTaggedPartInput(ROD, 1).addTaggedPartInput(GEAR, 2).addFluidInput(MIFluids.SOLDERING_ALLOY, 75)
                .addPartOutput(DRILL_HEAD, 1);

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.ASSEMBLER, "cable_synthetic_rubber")
                .addTaggedPartInput(WIRE, 3)
                .addFluidInput(MIFluids.SYNTHETIC_RUBBER, 30)
                .addPartOutput(CABLE, 3);

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.ASSEMBLER, "cable_styrene_rubber")
                .addTaggedPartInput(WIRE, 3)
                .addFluidInput(MIFluids.STYRENE_BUTADIENE_RUBBER, 6)
                .addPartOutput(CABLE, 3);

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.ASSEMBLER, "fuel_rod", 16, 200)
                .addItemInput("modern_industrialization:blastproof_alloy_curved_plate", 2)
                .addItemInput(MIItem.LARGE_MOTOR, 1).addItemInput(MIItem.ROBOT_ARM, 2).addTaggedPartInput(ROD, 18)
                .addFluidInput(MIFluids.SOLDERING_ALLOY, 500).addFluidInput(MIFluids.HELIUM, 100)
                .addPartOutput(FUEL_ROD, 1);

        // HEAT EXCHANGER
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.HEAT_EXCHANGER, "hot_ingot", 8, 10).addPartInput(HOT_INGOT, 1)
                .addFluidInput(MIFluids.CRYOFLUID, 100)
                .addPartOutput(INGOT, 1).addFluidOutput(MIFluids.ARGON, 65).addFluidOutput(MIFluids.HELIUM, 25);

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.POLARIZER, "rod_magnetic", 8, 200).addTaggedPartInput(ROD, 1).addPartOutput(ROD_MAGNETIC, 1);
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.POLARIZER, "wire_magnetic", 8, 200).addTaggedPartInput(WIRE, 1).addPartOutput(WIRE_MAGNETIC, 1);
        new SmeltingRecipeBuilder(ctx, ROD_MAGNETIC, ROD, 0.0f, false);
        new SmeltingRecipeBuilder(ctx, WIRE_MAGNETIC, WIRE, 0.0f, false);

        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.VACUUM_FREEZER, "hot_ingot", 32, 250).addTaggedPartInput(HOT_INGOT, 1).addPartOutput(INGOT, 1);

    }

    /**
     * Add a recycling recipe in the macerator.
     */
    private static void addMaceratorRecycling(MaterialBuilder.RecipeContext ctx, PartKeyProvider partInput, int tinyDustOutput) {
        MIRecipeBuilder builder = new MIRecipeBuilder(ctx, MIMachineRecipeTypes.MACERATOR, partInput);
        builder.addTaggedPartInput(partInput, 1);
        if (tinyDustOutput % 9 == 0) {
            builder.addPartOutput(DUST, tinyDustOutput / 9);
        } else {
            while (tinyDustOutput > 64) {
                builder.addPartOutput(TINY_DUST, 64);
                tinyDustOutput -= 64;
            }
            builder.addPartOutput(TINY_DUST, tinyDustOutput);
        }
    }

    /**
     * Add 3x3 -> 1 and 1 -> 9 crafting recipes.
     */
    private static void add3By3Crafting(MaterialBuilder.RecipeContext ctx, PartKeyProvider smallPart, PartKeyProvider bigPart, boolean packer) {
        if (ctx.hasInternalPart(smallPart) || ctx.hasInternalPart(bigPart)) {
            // Don't add recipe if it's all external (vanilla already has it)
            new ShapedRecipeBuilder(ctx, bigPart, 1, bigPart.key() + "_from_" + smallPart.key(), "yxx", "xxx", "xxx")
                    .addPart('y', smallPart)
                    .addTaggedPart('x', smallPart);
            new ShapedRecipeBuilder(ctx, smallPart, 9, smallPart.key() + "_from_" + bigPart.key(), "x")
                    .addPart('x', bigPart);
        }

        if (packer) {
            new MIRecipeBuilder(ctx, MIMachineRecipeTypes.PACKER, bigPart).addTaggedPartInput(smallPart, 9).addPartOutput(bigPart, 1);
        }
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.UNPACKER, smallPart).addTaggedPartInput(bigPart, 1).addPartOutput(smallPart, 9);

    }

    private static void addCuttingMachine(MaterialBuilder.RecipeContext ctx, String name, PartKeyProvider inputPart, PartKeyProvider outputPart,
            int amount) {
        new MIRecipeBuilder(ctx, MIMachineRecipeTypes.CUTTING_MACHINE, name).addTaggedPartInput(inputPart, 1).addPartOutput(outputPart, amount)
                .addFluidInput("modern_industrialization:lubricant", 1);
    }

}

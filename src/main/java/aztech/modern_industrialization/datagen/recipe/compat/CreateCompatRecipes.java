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

package aztech.modern_industrialization.datagen.recipe.compat;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import aztech.modern_industrialization.recipe.json.ShapelessRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.material.Fluids;

public class CreateCompatRecipes extends CompatRecipesProvider {
    public CreateCompatRecipes(PackOutput packOutput) {
        super(packOutput, "create");
    }

    @Override
    protected void generate() {
        addMiRecipe(MIMachineRecipeTypes.PACKER, "#c:nuggets/zinc", 9, "create:zinc_ingot", 1, 2, 200);
        addMiRecipe(MIMachineRecipeTypes.PACKER, "#c:ingots/zinc", 9, "create:zinc_block", 1, 2, 200);
        addMiRecipe(MIMachineRecipeTypes.PACKER, "#c:raw_materials/zinc", 9, "create:raw_zinc_block", 1, 2, 200);
        addMiRecipe(MIMachineRecipeTypes.PACKER, "create:andesite_alloy", 9, "create:andesite_alloy_block", 1, 2, 200);
        addMiRecipe(MIMachineRecipeTypes.UNPACKER, "#c:ingots/zinc", "create:zinc_nugget", 9, 2, 200);
        addMiRecipe(MIMachineRecipeTypes.UNPACKER, "#c:storage_blocks/zinc", "create:zinc_ingot", 9, 2, 200);
        addMiRecipe(MIMachineRecipeTypes.UNPACKER, "#c:storage_blocks/raw_zinc", "create:raw_zinc", 9, 2, 200);
        addMiRecipe(MIMachineRecipeTypes.UNPACKER, "#c:storage_blocks/andesite_alloy", "create:andesite_alloy", 9, 2, 200);

        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:ores/zinc", "create:raw_zinc", 3, 2, 200);
        addCompatRecipe("macerator/raw_zinc_to_crushed_raw_zinc", new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 2, 100)
                .addItemInput("#c:raw_materials/zinc", 1)
                .addItemOutput("create:crushed_raw_zinc", 1)
                .addItemOutput("create:crushed_raw_zinc", 1, 0.5f));

        addCompatRecipe("mixer/andesite_alloy/iron", new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100)
                .addItemInput("minecraft:andesite", 1)
                .addItemInput("#c:nuggets/iron", 1)
                .addItemOutput("create:andesite_alloy", 1));
        addCompatRecipe("mixer/andesite_alloy/zinc", new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100)
                .addItemInput("minecraft:andesite", 1)
                .addItemInput("#c:nuggets/zinc", 1)
                .addItemOutput("create:andesite_alloy", 1));

        addCompatRecipe("macerator/wheat_to_wheat_flour", new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 2, 100)
                .addItemInput("minecraft:wheat", 1)
                .addItemOutput("create:wheat_flour", 1)
                .addItemOutput("create:wheat_flour", 2, 0.25f)
                .addItemOutput("minecraft:wheat_seeds", 1, 0.25f));
        addCompatRecipe("mixer/dough", new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100)
                .addItemInput("create:wheat_flour", 1)
                .addFluidInput(Fluids.WATER, 1000)
                .addItemOutput("create:dough", 1));

        addCompatRecipe("macerator/netherrack_to_cinder_flour", new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 2, 100)
                .addItemInput("minecraft:netherrack", 1)
                .addItemOutput("create:cinder_flour", 1)
                .addItemOutput("create:cinder_flour", 1, 0.5f));

        addCompatRecipe("macerator/obsidian_to_powdered_obsidian", new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 2, 100)
                .addItemInput("minecraft:obsidian", 1)
                .addItemOutput("create:powdered_obsidian", 1)
                .addItemOutput("minecraft:obsidian", 1, 0.75f));

        addCompatRecipe("macerator/asurine", new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 8, 100)
                .addItemInput("#create:stone_types/asurine", 1)
                .addItemOutput("create:crushed_raw_zinc", 1, 0.3f)
                .addItemOutput("create:zinc_nugget", 1, 0.3f));
        addCompatRecipe("macerator/crimsite", new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 8, 100)
                .addItemInput("#create:stone_types/crimsite", 1)
                .addItemOutput("modern_industrialization:iron_dust", 1, 0.4f)
                .addItemOutput("modern_industrialization:iron_tiny_dust", 1, 0.4f));
        addCompatRecipe("macerator/ochrum", new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 8, 100)
                .addItemInput("#create:stone_types/ochrum", 1)
                .addItemOutput("modern_industrialization:gold_dust", 1, 0.2f)
                .addItemOutput("modern_industrialization:gold_tiny_dust", 1, 0.2f));
        addCompatRecipe("macerator/veridium", new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 8, 100)
                .addItemInput("#create:stone_types/veridium", 1)
                .addItemOutput("modern_industrialization:copper_dust", 1, 0.8f)
                .addItemOutput("modern_industrialization:copper_tiny_dust", 1, 0.8f));

        addCompatRecipe("mixer/pulp", new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100)
                .addItemInput("#create:pulpifiable", 4)
                .addFluidInput(Fluids.WATER, 250)
                .addItemOutput("create:pulp", 1));
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "create:pulp", "create:cardboard", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "create:cardboard", 4, "create:cardboard_block", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.UNPACKER, "create:cardboard_block", "create:cardboard", 4, 2, 200);

        casing("#c:stripped_logs", "create:andesite_alloy", "create:andesite_casing");
        casing("#c:stripped_woods", "create:andesite_alloy", "create:andesite_casing");
        casing("#c:stripped_logs", "#c:ingots/brass", "create:brass_casing");
        casing("#c:stripped_woods", "#c:ingots/brass", "create:brass_casing");
        casing("#c:stripped_logs", "#c:ingots/copper", "create:copper_casing");
        casing("#c:stripped_woods", "#c:ingots/copper", "create:copper_casing");
        casing("create:brass_casing", "create:sturdy_sheet", "create:railway_casing");

        addCompatRecipe("chemical_reactor/rose_quartz", new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 8, 100)
                .addItemInput("#c:gems/quartz", 1)
                .addFluidInput(MIFluids.MOLTEN_REDSTONE, 90 * 8)
                .addItemOutput("create:rose_quartz", 1));
        addCompatRecipe("chemical_reactor/polished_rose_quartz", new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 8, 100)
                .addItemInput("create:rose_quartz", 1)
                .addItemInput("modern_industrialization:wax", 1)
                .addItemOutput("create:polished_rose_quartz", 1));
        addCompatRecipe("craft/polished_rose_quartz", new ShapelessRecipeBuilder("create:polished_rose_quartz", 1)
                .requires("create:rose_quartz").requires(MIItem.WAX));
        addCompatRecipe("assembler/electron_tube", new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 100)
                .addItemInput("#c:plates/iron", 1)
                .addItemInput("create:polished_rose_quartz", 1)
                .addItemOutput("create:electron_tube", 1));

        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "minecraft:dried_kelp", 6, "create:belt_connector", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "modern_industrialization:rubber_sheet", 6, "create:belt_connector", 1, 2, 100);
        addCompatRecipe("craft/belt", new ShapedRecipeJson("create:belt_connector", 1, "rrr", "rrr")
                .addInput('r', "modern_industrialization:rubber_sheet"));

        addCuttingRecipe("create:andesite_alloy", "create:shaft", 6);

        addCompatRecipe("assembler/cogwheel", new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 100)
                .addItemInput("create:shaft", 1)
                .addItemInput("#minecraft:planks", 1)
                .addItemOutput("create:cogwheel", 1));
        addCompatRecipe("assembler/large_cogwheel", new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 100)
                .addItemInput("create:cogwheel", 1)
                .addItemInput("#minecraft:planks", 1)
                .addItemOutput("create:large_cogwheel", 1));

        addCompatRecipe("assembler/precision_mechanism", new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 500)
                .addItemInput("#c:plates/gold", 1)
                .addItemInput("create:cogwheel", 5)
                .addItemInput("create:large_cogwheel", 5)
                .addItemInput("modern_industrialization:iron_bolt", 5)
                .addItemOutput("create:precision_mechanism", 1, 0.8f));
    }

    private void casing(String block, String item, String output) {
        addCompatRecipe("assembler/casing/%s".formatted(formatRecipePath(block, output)), new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 100)
                .addItemInput(block, 1)
                .addItemInput(item, 1)
                .addItemOutput(output, 1));
    }
}

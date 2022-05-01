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
package aztech.modern_industrialization.datagen.recipe;

import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import aztech.modern_industrialization.recipe.json.RecipeJson;
import aztech.modern_industrialization.recipe.json.compat.IRCompressRecipeJson;
import aztech.modern_industrialization.recipe.json.compat.TRCompressorRecipeJson;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.material.Fluids;

public class CompatRecipesProvider extends MIRecipesProvider {
    private Consumer<FinishedRecipe> consumer;
    private String currentCompatModid;

    public CompatRecipesProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> consumer) {
        this.consumer = consumer;

        this.currentCompatModid = "techreborn";
        generateTrCompat();

        this.currentCompatModid = "ae2";
        generateAe2Compat();

        this.currentCompatModid = "indrev";
        generateIndrevCompat();
    }

    private void generateTrCompat() {
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "techreborn:advanced_alloy_ingot", "techreborn:advanced_alloy_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "#c:brass_ingots", "techreborn:brass_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "techreborn:carbon_mesh", "techreborn:carbon_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "techreborn:lazurite_dust", "techreborn:lazurite_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "minecraft:obsidian", "techreborn:obsidian_plate", 9);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "techreborn:obsidian_dust", "techreborn:obsidian_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "#c:peridot_dusts", "techreborn:peridot_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "techreborn:plantball", "techreborn:compressed_plantball", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "minecraft:prismarine_crystals", "minecraft:prismarine_shard", 1); // TODO
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "techreborn:red_garnet_dust", "techreborn:red_garnet_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "minecraft:redstone_block", "techreborn:redstone_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "#c:ruby_dusts", "techreborn:ruby_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "#c:sapphire_dusts", "techreborn:sapphire_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "techreborn:tungstensteel_ingot", "techreborn:tungstensteel_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "#minecraft:planks", "techreborn:wood_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "techreborn:yellow_garnet_dust", "techreborn:yellow_garnet_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "#c:zinc_ingots", "techreborn:zinc_plate", 1);
        addMiRecipe(MIMachineRecipeTypes.COMPRESSOR, "techreborn:refined_iron_ingot", "techreborn:refined_iron_plate", 1);

        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:andesite", "techreborn:andesite_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:basalt", "techreborn:basalt_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:brass_ingots", "techreborn:brass_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:charcoal", "techreborn:charcoal_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:cinnabar_ores", "techreborn:cinnabar_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:clay_ball", "techreborn:clay_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:diorite", "techreborn:diorite_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:ender_eye", "techreborn:ender_eye_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:ender_pearl", "techreborn:ender_pearl_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:end_stone", "techreborn:endstone_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:flint", "techreborn:flint_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:galena_ores", "techreborn:galena_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:granite", "techreborn:granite_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:netherrack", "techreborn:netherrack_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "techreborn:peridot_gem", "techreborn:peridot_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:peridot_ores", "techreborn:peridot_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:pyrite_ores", "techreborn:pyrite_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "techreborn:red_garnet_gem", "techreborn:red_garnet_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "techreborn:ruby_gem", "modern_industrialization:ruby_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:ruby_ores", "modern_industrialization:ruby_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "techreborn:sapphire_gem", "techreborn:sapphire_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:sapphire_ores", "techreborn:sapphire_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:sodalite_ores", "techreborn:sodalite_dust", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "techreborn:yellow_garnet_gem", "techreborn:yellow_garnet_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:zinc_ingots", "techreborn:zinc_dust", 1);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:silver_ores", "techreborn:raw_silver", 2);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:sphalerite_ores", "techreborn:sphalerite_dust", 2);

        for (Material material : MaterialRegistry.getMaterials().values()) {
            if (material.getParts().containsKey(MIParts.CURVED_PLATE.key)) {
                String plate = material.name;
                addCompatRecipe("%s_curved_plate".formatted(plate),
                        new TRCompressorRecipeJson("c:%s_plates".formatted(plate), "modern_industrialization:%s_curved_plate".formatted(plate)));
            }
        }
    }

    private void generateAe2Compat() {
        addMiRecipe(MIMachineRecipeTypes.ELECTROLYZER, "ae2:certus_quartz_crystal", "ae2:charged_certus_quartz_crystal", 1, 8, 60);
        addCompatRecipe("macerator/certus_ore", MIRecipeJson.create(MIMachineRecipeTypes.MACERATOR, 2, 200)
                .addItemInput("#c:certus_quartz_ores", 1)
                .addItemOutput("ae2:certus_quartz_dust", 5)
                .addItemOutput("ae2:certus_quartz_crystal", 1, 0.1));
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:certus_quartz", "ae2:certus_quartz_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "ae2:fluix_crystal", "ae2:fluix_dust", 1, 2, 100);
        addCompatRecipe("mixer/fluix", MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 8, 100)
                .addItemInput("minecraft:quartz", 1)
                .addItemInput("ae2:charged_certus_quartz_crystal", 1)
                .addItemInput("minecraft:redstone", 1)
                .addFluidInput(Fluids.WATER, 1000, 0)
                .addItemOutput("ae2:fluix_dust", 2));
        addCompatRecipe("quarry_ae2", MIRecipeJson.create(MIMachineRecipeTypes.QUARRY, 16, 600)
                .addItemInput("ae2:fluix_glass_cable", 1, 0.2)
                .addItemOutput("ae2:quartz_ore", 8, 0.02));
    }

    private void generateIndrevCompat() {
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "indrev:nikolite_ore", "indrev:nikolite_dust", 7);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "indrev:deepslate_nikolite_ore", "indrev:nikolite_dust", 7);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:silver_ores", "indrev:raw_silver", 2);

        // quarry recipe for nikolite
        addCompatRecipe("quarry_nikolite", MIRecipeJson.create(MIMachineRecipeTypes.QUARRY, 16, 600)
                .addItemInput("indrev:cable_mk1", 1, 0.6)
                .addItemOutput("indrev:nikolite_ore", 6, 0.03));

        for (Material material : MaterialRegistry.getMaterials().values()) {
            if (material.getParts().containsKey(MIParts.CURVED_PLATE.key) && !material.name.equals("tin")) {
                String plate = material.name;
                addCompatRecipe("%s_curved_plate".formatted(plate),
                        new IRCompressRecipeJson("c:%s_plates".formatted(plate), "modern_industrialization:%s_curved_plate".formatted(plate)));
            }
        }
    }

    private void addMiRecipe(MachineRecipeType machine, String input, String output, int outputAmount) {
        addMiRecipe(machine, input, output, outputAmount, 2, 200);
    }

    private void addMiRecipe(MachineRecipeType machine, String input, String output, int outputAmount, int eu, int duration) {
        String id = "%s/%s_to_%s".formatted(machine.getPath(), input.replace('#', '_').replace(':', '_'), output.replace(':', '_'));
        addCompatRecipe(id, MIRecipeJson.create(machine, eu, duration).addItemInput(input, 1).addItemOutput(output, outputAmount));
    }

    private void addCompatRecipe(String id, RecipeJson<?> recipeJson) {
        id = "compat/%s/%s".formatted(currentCompatModid, id);
        recipeJson.offerTo(withConditions(consumer, DefaultResourceConditions.allModsLoaded(currentCompatModid)), id);
    }
}

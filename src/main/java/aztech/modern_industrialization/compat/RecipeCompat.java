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
package aztech.modern_industrialization.compat;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import aztech.modern_industrialization.recipe.json.RecipeJson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.fluid.Fluids;

// TODO: can this cursed class be improved?
public class RecipeCompat {
    public static void loadCompatRecipes() {
        if (FabricLoader.getInstance().isModLoaded("techreborn")) {
            ModernIndustrialization.LOGGER.info("Tech Reborn is detected, loading compatibility recipes for Modern Industrialization!");

            addMiRecipe("compressor", "techreborn:advanced_alloy_ingot", "techreborn:advanced_alloy_plate", 1);
            addMiRecipe("compressor", "#c:brass_ingots", "techreborn:brass_plate", 1);
            addMiRecipe("compressor", "techreborn:carbon_mesh", "techreborn:carbon_plate", 1);
            addMiRecipe("compressor", "techreborn:lazurite_dust", "techreborn:lazurite_plate", 1);
            addMiRecipe("compressor", "minecraft:obsidian", "techreborn:obsidian_plate", 9);
            addMiRecipe("compressor", "techreborn:obsidian_dust", "techreborn:obsidian_plate", 1);
            addMiRecipe("compressor", "#c:peridot_dusts", "techreborn:peridot_plate", 1);
            addMiRecipe("compressor", "techreborn:plantball", "techreborn:compressed_plantball", 1);
            addMiRecipe("compressor", "minecraft:prismarine_crystals", "minecraft:prismarine_shard", 1); // TODO
            addMiRecipe("compressor", "techreborn:red_garnet_dust", "techreborn:red_garnet_plate", 1);
            addMiRecipe("compressor", "minecraft:redstone_block", "techreborn:redstone_plate", 1);
            addMiRecipe("compressor", "#c:ruby_dusts", "techreborn:ruby_plate", 1);
            addMiRecipe("compressor", "#c:sapphire_dusts", "techreborn:sapphire_plate", 1);
            addMiRecipe("compressor", "techreborn:tungstensteel_ingot", "techreborn:tungstensteel_plate", 1);
            addMiRecipe("compressor", "#minecraft:planks", "techreborn:wood_plate", 1);
            addMiRecipe("compressor", "techreborn:yellow_garnet_dust", "techreborn:yellow_garnet_plate", 1);
            addMiRecipe("compressor", "#c:zinc_ingots", "techreborn:zinc_plate", 1);
            addMiRecipe("compressor", "techreborn:refined_iron_ingot", "techreborn:refined_iron_plate", 1);

            addMiRecipe("macerator", "minecraft:andesite", "techreborn:andesite_dust", 2);
            addMiRecipe("macerator", "#c:basalt", "techreborn:basalt_dust", 2);
            addMiRecipe("macerator", "#c:brass_ingots", "techreborn:brass_dust", 1);
            addMiRecipe("macerator", "minecraft:charcoal", "techreborn:charcoal_dust", 1);
            addMiRecipe("macerator", "#c:cinnabar_ores", "techreborn:cinnabar_dust", 2);
            addMiRecipe("macerator", "minecraft:clay_ball", "techreborn:clay_dust", 1);
            addMiRecipe("macerator", "minecraft:diorite", "techreborn:diorite_dust", 2);
            addMiRecipe("macerator", "minecraft:ender_eye", "techreborn:ender_eye_dust", 2);
            addMiRecipe("macerator", "minecraft:ender_pearl", "techreborn:ender_pearl_dust", 1);
            addMiRecipe("macerator", "minecraft:end_stone", "techreborn:endstone_dust", 2);
            addMiRecipe("macerator", "minecraft:flint", "techreborn:flint_dust", 1);
            addMiRecipe("macerator", "#c:galena_ores", "techreborn:galena_dust", 2);
            addMiRecipe("macerator", "minecraft:granite", "techreborn:granite_dust", 2);
            addMiRecipe("macerator", "minecraft:netherrack", "techreborn:netherrack_dust", 1);
            addMiRecipe("macerator", "techreborn:peridot_gem", "techreborn:peridot_dust", 1);
            addMiRecipe("macerator", "#c:peridot_ores", "techreborn:peridot_dust", 2);
            addMiRecipe("macerator", "#c:pyrite_ores", "techreborn:pyrite_dust", 2);
            addMiRecipe("macerator", "techreborn:red_garnet_gem", "techreborn:red_garnet_dust", 1);
            addMiRecipe("macerator", "techreborn:ruby_gem", "modern_industrialization:ruby_dust", 1);
            addMiRecipe("macerator", "#c:ruby_ores", "modern_industrialization:ruby_dust", 2);
            addMiRecipe("macerator", "techreborn:sapphire_gem", "techreborn:sapphire_dust", 1);
            addMiRecipe("macerator", "#c:sapphire_ores", "techreborn:sapphire_dust", 2);
            addMiRecipe("macerator", "#c:sodalite_ores", "techreborn:sodalite_dust", 2);
            addMiRecipe("macerator", "techreborn:yellow_garnet_gem", "techreborn:yellow_garnet_dust", 1);
            addMiRecipe("macerator", "#c:zinc_ingots", "techreborn:zinc_dust", 1);
            addMiRecipe("macerator", "#c:silver_ores", "techreborn:raw_silver", 2);
            addMiRecipe("macerator", "#c:sphalerite_ores", "techreborn:sphalerite_dust", 2);

            for (Material material : MaterialRegistry.getMaterials().values()) {
                if (material.getParts().containsKey(MIParts.CURVED_PLATE.key)) {
                    String plate = material.name;
                    addRecipe("tr_compat/" + plate + "_curved_plates", String.format(
                            "{\"type\":\"techreborn:compressor\",\"power\":10,\"time\":300,\"ingredients\":[{\"tag\":\"c:%s_plates\"}],\"results\":[{\"item\":\"modern_industrialization:%s_curved_plate\"}]}",
                            plate, plate).getBytes());
                }
            }
        }

        if (FabricLoader.getInstance().isModLoaded("appliedenergistics2")) {
            ModernIndustrialization.LOGGER.info("Applied Energistics 2 is detected, loading compatibility recipes for Modern Industrialization!");

            addMiRecipe("electrolyzer", "appliedenergistics2:certus_quartz_crystal", "appliedenergistics2:charged_certus_quartz_crystal", 1, 8, 60);
            addRecipe("macerator/certus_ore", new MIRecipeJson("macerator", 2, 200)//
                    .addItemInput("#c:certus_quartz_ores", 1)//
                    .addOutput("appliedenergistics2:certus_quartz_dust", 5)//
                    .addOutput("appliedenergistics2:certus_quartz_crystal", 1, 0.1)//
            );
            addCrystalMaceration("certus", "#c:certus_quartz", "appliedenergistics2:certus_quartz_dust");
            addCrystalMaceration("fluix", "appliedenergistics2:fluix_crystal", "appliedenergistics2:fluix_dust");
            addRecipe("mixer/fluix", new MIRecipeJson("mixer", 8, 100)//
                    .addItemInput("minecraft:quartz", 1)//
                    .addItemInput("appliedenergistics2:charged_certus_quartz_crystal", 1)//
                    .addItemInput("minecraft:redstone", 1)//
                    .addFluidInput(Fluids.WATER, 1000, 0)//
                    .addOutput("appliedenergistics2:fluix_dust", 2)//
            );
            addRecipe("quarry_ae2", new MIRecipeJson("quarry", 16, 600)//
                    .addItemInput("appliedenergistics2:fluix_glass_cable", 1, 0.2)//
                    .addOutput("appliedenergistics2:quartz_ore", 8, 0.02)//
            );
        }

        if (FabricLoader.getInstance().isModLoaded("indrev")) {
            ModernIndustrialization.LOGGER.info("Industrial Revolution is detected, loading compatibility recipes for Modern Industrialization!");

            addMiRecipe("macerator", "indrev:nikolite_ore", "indrev:nikolite_dust", 7);
            addMiRecipe("macerator", "indrev:deepslate_nikolite_ore", "indrev:nikolite_dust", 7);
            addMiRecipe("macerator", "#c:silver_ores", "indrev:raw_silver", 2);

            // quarry recipe for nikolite
            addRecipe("quarry_nikolite",
                    "{\"type\":\"modern_industrialization:quarry\",\"eu\":16,\"duration\":600,\"item_inputs\":{\"item\":\"indrev:cable_mk1\",\"amount\":1,\"probability\":0.6},\"item_outputs\":{\"item\":\"indrev:nikolite_ore\",\"amount\":6,\"probability\":0.03}}"
                            .getBytes());

            for (Material material : MaterialRegistry.getMaterials().values()) {
                if (material.getParts().containsKey(MIParts.CURVED_PLATE.key) && !material.name.equals("tin")) {
                    String plate = material.name;
                    addRecipe("ir_compat/" + plate + "_curved_plates", String.format(
                            "{\"type\":\"indrev:compress\",\"ingredients\":{\"tag\":\"c:%s_plates\"},\"output\":{\"item\":\"modern_industrialization:%s_curved_plate\",\"count\":1},\"processTime\":300}",
                            plate, plate).getBytes());
                }
            }
        }
    }

    private static void addCrystalMaceration(String name, String crystal, String dust) {
        addRecipe("macerator/" + name, new MIRecipeJson("macerator", 2, 100).addItemInput(crystal, 2).addOutput(dust, 1));
    }

    private static void addMiRecipe(String machine, String input, String output, int outputAmount) {
        addMiRecipe(machine, input, output, outputAmount, 2, 200);
    }

    private static void addMiRecipe(String machine, String input, String output, int outputAmount, int eu, int duration) {
        String id = "compat_" + machine + "_" + input.replace(':', '_').replace('#', '_') + "_to_" + output.replace(':', '_');
        addRecipe(id, new MIRecipeJson(machine, eu, duration).addItemInput(input, 1).addOutput(output, outputAmount));
    }

    private static void addRecipe(String id, RecipeJson json) {
        addRecipe(id, json.toBytes());
    }

    private static void addRecipe(String id, byte[] data) {
        ModernIndustrialization.RESOURCE_PACK.addData(new MIIdentifier("recipes/" + id + ".json"), data);
    }
}

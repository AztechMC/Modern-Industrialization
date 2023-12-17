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

import static aztech.modern_industrialization.materials.property.MaterialProperty.HARDNESS;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.compat.ae2.AECompatCondition;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import aztech.modern_industrialization.recipe.json.RecipeJson;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import aztech.modern_industrialization.recipe.json.compat.IRCompressRecipeJson;
import aztech.modern_industrialization.recipe.json.compat.TRCompressorRecipeJson;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluids;

public class CompatRecipesProvider extends MIRecipesProvider {

    private Consumer<FinishedRecipe> consumer;
    private String currentCompatModid;
    private ConditionJsonProvider[] conditions = null;

    public CompatRecipesProvider(FabricDataOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {
        this.consumer = consumer;

        startCompat("techreborn");
        generateTrCompat();

        startCompat("ae2");
        generateAe2Compat();

        startCompat("indrev");
        generateIndrevCompat();
    }

    private void startCompat(String modid) {
        currentCompatModid = modid;
        conditions = new ConditionJsonProvider[] { DefaultResourceConditions.allModsLoaded(modid) };
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
            if (material.getParts().containsKey(MIParts.CURVED_PLATE.key())) {
                String plate = material.name;
                addCompatRecipe("%s_curved_plate".formatted(plate),
                        new TRCompressorRecipeJson("c:%s_plates".formatted(plate), "modern_industrialization:%s_curved_plate".formatted(plate)));
            }
        }

        var plateCompatMaterials = List.of(
                MIMaterials.ANNEALED_COPPER,
                MIMaterials.BATTERY_ALLOY,
                MIMaterials.BERYLLIUM,
                MIMaterials.BLASTPROOF_ALLOY,
                MIMaterials.CADMIUM,
                MIMaterials.CUPRONICKEL,
                MIMaterials.KANTHAL,
                MIMaterials.SILICON,
                MIMaterials.STAINLESS_STEEL,
                MIMaterials.SUPERCONDUCTOR);
        for (var material : plateCompatMaterials) {
            // Currently the TR compressor builder only accepts tags without the prefix, so we check that.
            var tag = material.getPart(MIParts.INGOT).getTaggedItemId();
            Preconditions.checkArgument(tag.startsWith("#"));
            tag = tag.substring(1);
            addCompatRecipe(material.name + "_plate",
                    new TRCompressorRecipeJson(
                            tag,
                            material.getPart(MIParts.PLATE).getItemId()).scaleTime(material.get(HARDNESS)));
        }
    }

    private void generateAe2Compat() {
        addMiRecipe(MIMachineRecipeTypes.ELECTROLYZER, "ae2:certus_quartz_crystal", "ae2:charged_certus_quartz_crystal", 1, 8, 60);

        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#c:certus_quartz", "ae2:certus_quartz_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:ender_pearl", "ae2:ender_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "ae2:fluix_crystal", "ae2:fluix_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "ae2:sky_stone_block", "ae2:sky_dust", 1, 2, 100);

        addCompatRecipe("mixer/fluix", MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 8, 100)
                .addItemInput("minecraft:quartz", 1)
                .addItemInput("ae2:charged_certus_quartz_crystal", 1)
                .addItemInput("minecraft:redstone", 1)
                .addFluidInput(Fluids.WATER, 1000, 0)
                .addItemOutput("ae2:fluix_crystal", 2));

        for (var entry : Map.of(
                "calculation", "ae2:certus_quartz_crystal",
                "engineering", "#c:diamonds",
                "logic", "#c:gold_ingots").entrySet()) {
            var type = entry.getKey();
            var ingredient = entry.getValue();

            addCompatRecipe("printed_" + type + "_processor", MIRecipeJson.create(MIMachineRecipeTypes.PACKER, 8, 200)
                    .addItemInput(ingredient, 1)
                    .addItemInput("ae2:" + type + "_processor_press", 1, 0)
                    .addItemOutput("ae2:printed_" + type + "_processor", 1));
            addCompatRecipe(type + "_processor", MIRecipeJson.create(MIMachineRecipeTypes.ASSEMBLER, 8, 200)
                    .addItemInput("ae2:printed_" + type + "_processor", 1)
                    .addItemInput("ae2:printed_silicon", 1)
                    .addFluidInput(MIFluids.MOLTEN_REDSTONE, 100)
                    .addItemOutput("ae2:" + type + "_processor", 1));
        }

        addCompatRecipe("printed_silicon", MIRecipeJson.create(MIMachineRecipeTypes.PACKER, 8, 200)
                .addItemInput("#c:silicon", 1)
                .addItemInput("ae2:silicon_press", 1, 0)
                .addItemOutput("ae2:printed_silicon", 1));

        addCompatRecipe("printed_silicon_from_ingot", MIRecipeJson.create(MIMachineRecipeTypes.PACKER, 8, 200)
                .addItemInput(MIMaterials.SILICON.getPart(MIParts.INGOT), 1)
                .addItemInput("ae2:silicon_press", 1, 0)
                .addItemOutput("ae2:printed_silicon", 1));

        // ME Wire stuff follows - only enable if AE2 is loaded AND if AE2 compat is enabled
        conditions = new ConditionJsonProvider[] { AECompatCondition.PROVIDER };

        for (DyeColor color : DyeColor.values()) {
            // 16 me wires with dye in the center
            var meWiresDirect = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_me_wire", 16, "qCq", "GdG", "qCq")
                    .addInput('C', "modern_industrialization:bronze_curved_plate")
                    .addInput('G', "#ae2:glass_cable")
                    .addInput('d', "#c:" + color.getName() + "_dyes")
                    .addInput('q', "ae2:quartz_fiber");
            addCompatRecipe("dyes/" + color.getName() + "/craft/me_wire_direct", meWiresDirect);
            addCompatRecipe("dyes/" + color.getName() + "/assembler/me_wire_direct", meWiresDirect.exportToAssembler());
            // 8 me wires
            var eightMeWires = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_me_wire", 8, "ppp", "pdp",
                    "ppp").addInput('d', "#c:" + color.getName() + "_dyes").addInput('p', "#modern_industrialization:me_wires");
            addCompatRecipe("dyes/" + color.getName() + "/craft/me_wire_8", eightMeWires);
            addCompatRecipe("dyes/" + color.getName() + "/mixer/me_wire_8",
                    eightMeWires.exportToMachine(MIMachineRecipeTypes.MIXER, 2, 100, 1));
            // 1 me wire
            addCompatRecipe("dyes/" + color.getName() + "/craft/me_wire_1",
                    new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_me_wire", 1, "pd")
                            .addInput('d', "#c:" + color.getName() + "_dyes").addInput('p', "#modern_industrialization:me_wires"));
        }

        // decolor 8 me wires
        addCompatRecipe("dyes/decolor/craft/me_wire_8", new ShapedRecipeJson("modern_industrialization:me_wire", 8, "ppp", "pbp",
                "ppp").addInput('b', "minecraft:water_bucket").addInput('p', "#modern_industrialization:me_wires"));
        // decolor 1 me wire
        addCompatRecipe("dyes/decolor/craft/me_wire_1", new ShapedRecipeJson("modern_industrialization:me_wire", 1, "pb")
                .addInput('b', "minecraft:water_bucket").addInput('p', "#modern_industrialization:me_wires"));
        // decolor 1 me wire with mixer
        addCompatRecipe("dyes/decolor/mixer/me_wire", MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 100)
                .addItemInput("#modern_industrialization:me_wires", 1)
                .addFluidInput(Fluids.WATER, 125)
                .addItemOutput("modern_industrialization:me_wire", 1));
        // 16 me wires direct
        var meWiresDirect = new ShapedRecipeJson("modern_industrialization:me_wire", 16, "qCq", "G G", "qCq")
                .addInput('C', "modern_industrialization:bronze_curved_plate")
                .addInput('G', "#ae2:glass_cable")
                .addInput('q', "ae2:quartz_fiber");
        addCompatRecipe("craft/me_wire_direct", meWiresDirect);
        addCompatRecipe("assembler/me_wire_direct", meWiresDirect.exportToAssembler());
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
            if (material.getParts().containsKey(MIParts.CURVED_PLATE.key()) && !material.name.equals("tin")) {
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

    private void addCompatRecipe(String id, RecipeJson recipeJson) {
        id = "compat/%s/%s".formatted(currentCompatModid, id);
        recipeJson.offerTo(withConditions(consumer, conditions), id);
    }
}

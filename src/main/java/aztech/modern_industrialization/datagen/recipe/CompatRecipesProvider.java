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

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.compat.ae2.AECompatCondition;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.recipe.json.IMIRecipeBuilder;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import java.util.Map;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public class CompatRecipesProvider extends MIRecipesProvider {

    private RecipeOutput consumer;
    private String currentCompatModid;
    private ICondition[] conditions = null;

    public CompatRecipesProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        this.consumer = consumer;

        startCompat("ae2");
        generateAe2Compat();
    }

    private void startCompat(String modid) {
        currentCompatModid = modid;
        conditions = new ICondition[] { new ModLoadedCondition(modid) };
    }

    private void generateAe2Compat() {
        addMiRecipe(MIMachineRecipeTypes.ELECTROLYZER, "ae2:certus_quartz_crystal", "ae2:charged_certus_quartz_crystal", 1, 8, 60);

        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "#forge:gems/certus_quartz", "ae2:certus_quartz_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "minecraft:ender_pearl", "ae2:ender_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "ae2:fluix_crystal", "ae2:fluix_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, "ae2:sky_stone_block", "ae2:sky_dust", 1, 2, 100);

        addCompatRecipe("mixer/fluix", new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 8, 100)
                .addItemInput("minecraft:quartz", 1)
                .addItemInput("ae2:charged_certus_quartz_crystal", 1)
                .addItemInput("minecraft:redstone", 1)
                .addFluidInput(Fluids.WATER, 1000, 0)
                .addItemOutput("ae2:fluix_crystal", 2));

        for (var entry : Map.of(
                "calculation", "ae2:certus_quartz_crystal",
                "engineering", "#forge:gems/diamond",
                "logic", "#forge:ingots/gold").entrySet()) {
            var type = entry.getKey();
            var ingredient = entry.getValue();

            addCompatRecipe("printed_" + type + "_processor", new MachineRecipeBuilder(MIMachineRecipeTypes.PACKER, 8, 200)
                    .addItemInput(ingredient, 1)
                    .addItemInput("ae2:" + type + "_processor_press", 1, 0)
                    .addItemOutput("ae2:printed_" + type + "_processor", 1));
            addCompatRecipe(type + "_processor", new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 200)
                    .addItemInput("ae2:printed_" + type + "_processor", 1)
                    .addItemInput("ae2:printed_silicon", 1)
                    .addFluidInput(MIFluids.MOLTEN_REDSTONE, 90)
                    .addItemOutput("ae2:" + type + "_processor", 1));
        }

        addCompatRecipe("printed_silicon", new MachineRecipeBuilder(MIMachineRecipeTypes.PACKER, 8, 200)
                .addItemInput("#forge:silicon", 1)
                .addItemInput("ae2:silicon_press", 1, 0)
                .addItemOutput("ae2:printed_silicon", 1));

        addCompatRecipe("printed_silicon_from_ingot", new MachineRecipeBuilder(MIMachineRecipeTypes.PACKER, 8, 200)
                .addItemInput(MIMaterials.SILICON.getPart(MIParts.INGOT), 1)
                .addItemInput("ae2:silicon_press", 1, 0)
                .addItemOutput("ae2:printed_silicon", 1));

        // ME Wire stuff follows - only enable if AE2 is loaded AND if AE2 compat is enabled
        conditions = new ICondition[] { AECompatCondition.INSTANCE };

        for (DyeColor color : DyeColor.values()) {
            // 16 me wires with dye in the center
            var meWiresDirect = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_me_wire", 16, "qCq", "GdG", "qCq")
                    .addInput('C', "modern_industrialization:bronze_curved_plate")
                    .addInput('G', "#ae2:glass_cable")
                    .addInput('d', "#forge:dyes/" + color.getName())
                    .addInput('q', "ae2:quartz_fiber");
            addCompatRecipe("dyes/" + color.getName() + "/craft/me_wire_direct", meWiresDirect);
            addCompatRecipe("dyes/" + color.getName() + "/assembler/me_wire_direct", meWiresDirect.exportToAssembler());
            // 8 me wires
            var eightMeWires = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_me_wire", 8, "ppp", "pdp",
                    "ppp").addInput('d', "#forge:dyes/" + color.getName()).addInput('p', "#modern_industrialization:me_wires");
            addCompatRecipe("dyes/" + color.getName() + "/craft/me_wire_8", eightMeWires);
            addCompatRecipe("dyes/" + color.getName() + "/mixer/me_wire_8",
                    eightMeWires.exportToMachine(MIMachineRecipeTypes.MIXER, 2, 100, 1));
            // 1 me wire
            addCompatRecipe("dyes/" + color.getName() + "/craft/me_wire_1",
                    new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_me_wire", 1, "pd")
                            .addInput('d', "#forge:dyes/" + color.getName()).addInput('p', "#modern_industrialization:me_wires"));
        }

        // decolor 8 me wires
        addCompatRecipe("dyes/decolor/craft/me_wire_8", new ShapedRecipeJson("modern_industrialization:me_wire", 8, "ppp", "pbp",
                "ppp").addInput('b', "minecraft:water_bucket").addInput('p', "#modern_industrialization:me_wires"));
        // decolor 1 me wire
        addCompatRecipe("dyes/decolor/craft/me_wire_1", new ShapedRecipeJson("modern_industrialization:me_wire", 1, "pb")
                .addInput('b', "minecraft:water_bucket").addInput('p', "#modern_industrialization:me_wires"));
        // decolor 1 me wire with mixer
        addCompatRecipe("dyes/decolor/mixer/me_wire", new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100)
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

    private void addMiRecipe(MachineRecipeType machine, String input, String output, int outputAmount) {
        addMiRecipe(machine, input, output, outputAmount, 2, 200);
    }

    private void addMiRecipe(MachineRecipeType machine, String input, String output, int outputAmount, int eu, int duration) {
        String id = "%s/%s_to_%s".formatted(machine.getPath(), input.replace('#', '_').replace(':', '_').replace('/', '_'), output.replace(':', '_'));
        addCompatRecipe(id, new MachineRecipeBuilder(machine, eu, duration).addItemInput(input, 1).addItemOutput(output, outputAmount));
    }

    private void addCompatRecipe(String id, IMIRecipeBuilder recipeJson) {
        id = "compat/%s/%s".formatted(currentCompatModid, id);
        recipeJson.offerTo(consumer.withConditions(conditions), id);
    }
}

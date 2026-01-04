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
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.compat.ae2.AECompatCondition;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.recipe.json.MIRecipeBuilder;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public class CompatRecipeProvider extends BaseRecipeProvider {
    private String currentCompatModid;
    private ICondition[] conditions = null;

    protected CompatRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        startCompat("ae2");
        generateAe2Compat();
    }

    private void startCompat(String modid) {
        currentCompatModid = modid;
        conditions = new ICondition[] { new ModLoadedCondition(modid) };
    }

    private void generateAe2Compat() {
        addMiRecipe(MIMachineRecipeTypes.ELECTROLYZER, item("ae2:certus_quartz_crystal"), "ae2:charged_certus_quartz_crystal", 1, 8, 60);

        addMiRecipe(MIMachineRecipeTypes.MACERATOR, conventionTag("gems/certus_quartz"), "ae2:certus_quartz_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, item("minecraft:ender_pearl"), "ae2:ender_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, item("ae2:fluix_crystal"), "ae2:fluix_dust", 1, 2, 100);
        addMiRecipe(MIMachineRecipeTypes.MACERATOR, item("ae2:sky_stone_block"), "ae2:sky_dust", 1, 2, 100);

        addCompatRecipe("mixer/fluix", machine(MIMachineRecipeTypes.MIXER, 8, 100)
                .itemIn("minecraft:quartz", 1)
                .itemIn("ae2:charged_certus_quartz_crystal", 1)
                .itemIn("minecraft:redstone", 1)
                .fluidIn(Fluids.WATER, 1000, 0)
                .itemOut("ae2:fluix_crystal", 2));

        for (var entry : Map.of(
                "calculation", item("ae2:certus_quartz_crystal"),
                "engineering", tag(Tags.Items.GEMS_DIAMOND),
                "logic", tag(Tags.Items.INGOTS_GOLD)).entrySet()) {
            var type = entry.getKey();
            var ingredient = entry.getValue();

            addCompatRecipe("printed_" + type + "_processor", machine(MIMachineRecipeTypes.PACKER, 8, 200)
                    .itemIn(ingredient, 1)
                    .itemIn("ae2:" + type + "_processor_press", 1, 0)
                    .itemOut("ae2:printed_" + type + "_processor", 1));
            addCompatRecipe(type + "_processor", machine(MIMachineRecipeTypes.ASSEMBLER, 8, 200)
                    .itemIn("ae2:printed_" + type + "_processor", 1)
                    .itemIn("ae2:printed_silicon", 1)
                    .fluidIn(MIFluids.MOLTEN_REDSTONE, 90)
                    .itemOut("ae2:" + type + "_processor", 1));
        }

        addCompatRecipe("printed_silicon", machine(MIMachineRecipeTypes.PACKER, 8, 200)
                .itemIn(conventionTag("silicon"), 1)
                .itemIn("ae2:silicon_press", 1, 0)
                .itemOut("ae2:printed_silicon", 1));

        addCompatRecipe("printed_silicon_from_ingot", machine(MIMachineRecipeTypes.PACKER, 8, 200)
                .itemIn(MIMaterials.SILICON.getPart(MIParts.INGOT), 1)
                .itemIn("ae2:silicon_press", 1, 0)
                .itemOut("ae2:printed_silicon", 1));

        // ME Wire stuff follows - only enable if AE2 is loaded AND if AE2 compat is enabled
        conditions = new ICondition[] { AECompatCondition.INSTANCE };

        for (DyeColor color : DyeColor.values()) {
            // 16 me wires with dye in the center
            var meWiresDirect = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_me_wire", 16, "qCq", "GdG", "qCq")
                    .addInput('C', "modern_industrialization:bronze_curved_plate")
                    .addInput('G', tag("ae2:glass_cable"))
                    .addInput('d', tag(color.getTag()))
                    .addInput('q', "ae2:quartz_fiber");
            addCompatRecipe("dyes/" + color.getName() + "/craft/me_wire_direct", meWiresDirect);
            addCompatRecipe("dyes/" + color.getName() + "/assembler/me_wire_direct", meWiresDirect.exportToAssembler());
            // 8 me wires
            var eightMeWires = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_me_wire", 8, "ppp", "pdp", "ppp")
                    .addInput('d', tag(color.getTag()))
                    .addInput('p', tag(MITags.ME_WIRES));
            addCompatRecipe("dyes/" + color.getName() + "/craft/me_wire_8", eightMeWires);
            addCompatRecipe("dyes/" + color.getName() + "/mixer/me_wire_8",
                    eightMeWires.exportToMachine(MIMachineRecipeTypes.MIXER, 2, 100, 1));
            // 1 me wire
            addCompatRecipe("dyes/" + color.getName() + "/craft/me_wire_1",
                    new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_me_wire", 1, "pd")
                            .addInput('d', tag(color.getTag()))
                            .addInput('p', tag(MITags.ME_WIRES)));
        }

        // decolor 8 me wires
        addCompatRecipe("dyes/decolor/craft/me_wire_8", new ShapedRecipeJson("modern_industrialization:me_wire", 8, "ppp", "pbp", "ppp")
                .addInput('b', "minecraft:water_bucket")
                .addInput('p', tag(MITags.ME_WIRES)));
        // decolor 1 me wire
        addCompatRecipe("dyes/decolor/craft/me_wire_1", new ShapedRecipeJson("modern_industrialization:me_wire", 1, "pb")
                .addInput('b', "minecraft:water_bucket")
                .addInput('p', tag(MITags.ME_WIRES)));
        // decolor 1 me wire with mixer
        addCompatRecipe("dyes/decolor/mixer/me_wire", machine(MIMachineRecipeTypes.MIXER, 2, 100)
                .itemIn(tag(MITags.ME_WIRES), 1)
                .fluidIn(Fluids.WATER, 125)
                .itemOut("modern_industrialization:me_wire", 1));
        // 16 me wires direct
        var meWiresDirect = new ShapedRecipeJson("modern_industrialization:me_wire", 16, "qCq", "G G", "qCq")
                .addInput('C', "modern_industrialization:bronze_curved_plate")
                .addInput('G', tag("ae2:glass_cable"))
                .addInput('q', "ae2:quartz_fiber");
        addCompatRecipe("craft/me_wire_direct", meWiresDirect);
        addCompatRecipe("assembler/me_wire_direct", meWiresDirect.exportToAssembler());
    }

    private Ingredient item(String id) {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ResourceKey.create(Registries.ITEM, Identifier.parse(id))).value());
    }

    private void addMiRecipe(MachineRecipeType machine, Ingredient input, String output, int outputAmount, int eu, int duration) {
        var inputName = input.getValues().unwrap().map(
                TagKey::location,
                items -> items.getFirst().unwrapKey().orElseThrow().identifier())
                .toString().replace(':', '_').replace('/', '_');
        String id = "%s/%s_to_%s".formatted(machine.getPath(), inputName, output.replace(':', '_'));
        addCompatRecipe(id, machine(machine, eu, duration)
                .itemIn(input, 1)
                .itemOut(output, outputAmount));
    }

    private void addCompatRecipe(String id, MIRecipeBuilder recipeJson) {
        id = "compat/%s/%s".formatted(currentCompatModid, id);
        recipeJson.offerTo(output.withConditions(conditions), id);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new CompatRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Compat Recipes";
        }
    }
}

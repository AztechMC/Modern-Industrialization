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
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class DyeRecipeProvider extends BaseRecipeProvider {
    protected DyeRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    public void buildRecipes() {
        for (DyeColor color : DyeColor.values()) {
            String pathPrefix = "dyes/" + color.getName() + "/";
            var dyeTag = tag(color.getTag());
            // 16 item pipes with dye in the center
            var itemPipesDirect = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_item_pipe", 16, "CCC", "GdG", "CCC")
                    .addInput('C', "modern_industrialization:bronze_curved_plate")
                    .addInput('G', partIngredient(MIMaterials.STEEL, MIParts.GEAR))
                    .addInput('d', dyeTag);
            itemPipesDirect.offerTo(output, pathPrefix + "craft/item_pipe_direct");
            itemPipesDirect.exportToAssembler().offerTo(output, pathPrefix + "assembler/item_pipe_direct");
            // 8 item pipes
            var eightItemPipes = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_item_pipe", 8, "ppp", "pdp", "ppp")
                    .addInput('d', dyeTag)
                    .addInput('p', tag(MITags.ITEM_PIPES));
            eightItemPipes.offerTo(output, pathPrefix + "craft/item_pipe_8");
            eightItemPipes.exportToMachine(MIMachineRecipeTypes.MIXER, 2, 100, 1).offerTo(output, pathPrefix + "mixer/item_pipe_8");
            // 1 item pipe
            new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_item_pipe", 1, "pd")
                    .addInput('d', dyeTag)
                    .addInput('p', tag(MITags.ITEM_PIPES))
                    .offerTo(output, pathPrefix + "craft/item_pipe_1");
            // 16 fluid pipes with stained glass
            var fluidPipesStainedGlass = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_fluid_pipe", 16, "CCC", "rPr", "CCC")
                    .addInput('C', "modern_industrialization:bronze_curved_plate")
                    .addInput('r', "modern_industrialization:copper_rotor")
                    .addInput('P', color.getName() + "_stained_glass_pane");
            fluidPipesStainedGlass.offerTo(output, pathPrefix + "craft/fluid_pipe_stained_glass");
            fluidPipesStainedGlass.exportToAssembler().offerTo(output, pathPrefix + "assembler/fluid_pipe_stained_glass");
            // 8 fluid pipes
            var eightFluidPipes = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_fluid_pipe", 8, "ppp", "pdp", "ppp")
                    .addInput('d', dyeTag)
                    .addInput('p', tag(MITags.FLUID_PIPES));
            eightFluidPipes.offerTo(output, pathPrefix + "craft/fluid_pipe_8");
            eightFluidPipes.exportToMachine(MIMachineRecipeTypes.MIXER, 2, 100, 1).offerTo(output, pathPrefix + "mixer/fluid_pipe_8");
            // 1 fluid pipe
            new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_fluid_pipe", 1, "pd")
                    .addInput('d', dyeTag)
                    .addInput('p', tag(MITags.FLUID_PIPES))
                    .offerTo(output, pathPrefix + "craft/fluid_pipe_1");
            // generate dyes with synthetic oil
            machine(MIMachineRecipeTypes.MIXER, 2, 200)
                    .fluidIn(MIFluids.SYNTHETIC_OIL, 100)
                    .itemIn(dyeTag, 1, 0)
                    .itemOut("minecraft:" + color.getName() + "_dye", 1)
                    .offerTo(output, pathPrefix + "mixer/synthetic_oil");
            // generate dyes with benzene
            machine(MIMachineRecipeTypes.MIXER, 2, 200)
                    .fluidIn(MIFluids.BENZENE, 25)
                    .itemIn(dyeTag, 1, 0)
                    .itemOut("minecraft:" + color.getName() + "_dye", 1)
                    .offerTo(output, pathPrefix + "mixer/benzene");

            // wool
            machine(MIMachineRecipeTypes.MIXER, 2, 100)
                    .itemIn(dyeTag, 1)
                    .itemIn(tag(ItemTags.WOOL), 8)
                    .itemOut("minecraft:" + color.getName() + "_wool", 8)
                    .offerTo(output, pathPrefix + "mixer/wool");

            // glass
            machine(MIMachineRecipeTypes.MIXER, 2, 100)
                    .itemIn(dyeTag, 1)
                    .itemIn(tag(Tags.Items.GLASS_BLOCKS), 8)
                    .itemOut("minecraft:" + color.getName() + "_stained_glass", 8)
                    .offerTo(output, pathPrefix + "mixer/glass");

            // glassPane
            machine(MIMachineRecipeTypes.MIXER, 2, 100)
                    .itemIn(dyeTag, 1)
                    .itemIn(tag(Tags.Items.GLASS_PANES), 8)
                    .itemOut("minecraft:" + color.getName() + "_stained_glass_pane", 8)
                    .offerTo(output, pathPrefix + "mixer/glass_pane");

            // shulker Box
            machine(MIMachineRecipeTypes.MIXER, 2, 100)
                    .itemIn(dyeTag, 1)
                    .itemIn(tag(Tags.Items.SHULKER_BOXES), 1)
                    .itemOut("minecraft:" + color.getName() + "_shulker_box", 1)
                    .offerTo(output, pathPrefix + "mixer/shulker_box");

            // bed
            machine(MIMachineRecipeTypes.MIXER, 2, 100)
                    .itemIn(dyeTag, 1)
                    .itemIn(tag(ItemTags.BEDS), 1)
                    .itemOut("minecraft:" + color.getName() + "_bed", 1)
                    .offerTo(output, pathPrefix + "mixer/bed");

            // candle
            machine(MIMachineRecipeTypes.MIXER, 2, 100)
                    .itemIn(dyeTag, 1)
                    .itemIn(tag(ItemTags.CANDLES), 1)
                    .itemOut("minecraft:" + color.getName() + "_candle", 1)
                    .offerTo(output, pathPrefix + "mixer/candle");

            // carpet
            machine(MIMachineRecipeTypes.MIXER, 2, 100)
                    .itemIn(dyeTag, 1)
                    .itemIn(tag(ItemTags.WOOL_CARPETS), 8)
                    .itemOut("minecraft:" + color.getName() + "_carpet", 8)
                    .offerTo(output, pathPrefix + "mixer/carpet");

            // terracotta
            machine(MIMachineRecipeTypes.MIXER, 2, 100)
                    .itemIn(dyeTag, 1)
                    .itemIn(tag(ItemTags.TERRACOTTA), 8)
                    .itemOut("minecraft:" + color.getName() + "_terracotta", 8)
                    .offerTo(output, pathPrefix + "mixer/terracotta");

            // glass pane cutting
            machine(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100)
                    .fluidIn(MIFluids.LUBRICANT, 1)
                    .itemIn("minecraft:" + color.getName() + "_stained_glass", 6)
                    .itemOut("minecraft:" + color.getName() + "_stained_glass_pane", 16)
                    .offerTo(output, pathPrefix + "cutting_machine/glass_pane");

            // carpet cutting
            machine(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100)
                    .fluidIn(MIFluids.LUBRICANT, 1)
                    .itemIn("minecraft:" + color.getName() + "_wool", 1)
                    .itemOut("minecraft:" + color.getName() + "_carpet", 4)
                    .offerTo(output, pathPrefix + "cutting_machine/carpet");
        }
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new DyeRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Dye Recipes";
        }
    }
}

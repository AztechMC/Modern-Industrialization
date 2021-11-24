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
package aztech.modern_industrialization.recipe;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import aztech.modern_industrialization.util.ResourceUtil;
import com.google.gson.Gson;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class DyeRecipes {
    private static final Gson GSON = new Gson();

    public static void initTags() {

        Identifier terracottas = new Identifier("c", "terracottas");
        Identifier glass = new Identifier("c", "glass");
        Identifier glassPane = new Identifier("c", "glass_pane");
        Identifier shulkerBox = new Identifier("c", "shulker_box");

        ResourceUtil.appendToItemTag(terracottas, new Identifier("minecraft:terracotta"));
        ResourceUtil.appendToItemTag(glass, new Identifier("minecraft:glass"));
        ResourceUtil.appendToItemTag(glassPane, new Identifier("minecraft:glass_pane"));
        ResourceUtil.appendToItemTag(shulkerBox, new Identifier("minecraft:shulker_box"));

        for (DyeColor color : DyeColor.values()) {
            Identifier tagId = new Identifier("c", color.getName() + "_dyes");
            TagRegistry.item(tagId);
            ResourceUtil.appendToItemTag(tagId, new Identifier("minecraft:" + color.getName() + "_dye"));
            ResourceUtil.appendToItemTag(terracottas, new Identifier("minecraft:" + color.getName() + "_terracotta"));
            ResourceUtil.appendToItemTag(terracottas, new Identifier("minecraft:" + color.getName() + "_glazed_terracotta"));

            ResourceUtil.appendToItemTag(glass, new Identifier("minecraft:" + color.getName() + "_stained_glass"));
            ResourceUtil.appendToItemTag(glassPane, new Identifier("minecraft:" + color.getName() + "_stained_glass_pane"));
            ResourceUtil.appendToItemTag(shulkerBox, new Identifier("minecraft:" + color.getName() + "_shulker_box"));
        }
    }

    public static void addRecipes(RuntimeResourcePack pack) {
        for (DyeColor color : DyeColor.values()) {
            String pathPrefix = "recipes/generated/dyes/" + color.getName() + "/";
            // 16 item pipes with dye in the center
            ShapedRecipeJson itemPipesDirect = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_item_pipe", 16, "CCC", "GdG",
                    "CCC").addInput('C', "modern_industrialization:bronze_curved_plate").addInput('G', "#c:steel_gears").addInput('d',
                            "#c:" + color.getName() + "_dyes");
            pack.addData(new MIIdentifier(pathPrefix + "craft/item_pipe_direct_asbl.json"), GSON.toJson(itemPipesDirect).getBytes());
            // 8 item pipes
            ShapedRecipeJson eightItemPipes = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_item_pipe", 8, "ppp", "pdp",
                    "ppp").addInput('d', "#c:" + color.getName() + "_dyes").addInput('p', "#modern_industrialization:item_pipes");
            pack.addData(new MIIdentifier(pathPrefix + "craft/item_pipe_8.json"), GSON.toJson(eightItemPipes).getBytes());
            pack.addData(new MIIdentifier(pathPrefix + "mixer/item_pipe_8.json"),
                    GSON.toJson(eightItemPipes.exportToMachine(MIMachineRecipeTypes.MIXER, 2, 100, 1)).getBytes());
            // 1 item pipe
            ShapedRecipeJson oneItemPipe = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_item_pipe", 1, "pd")
                    .addInput('d', "#c:" + color.getName() + "_dyes").addInput('p', "#modern_industrialization:item_pipes");
            pack.addData(new MIIdentifier(pathPrefix + "craft/item_pipe_1.json"), GSON.toJson(oneItemPipe).getBytes());
            // 16 fluid pipes with stained glass
            ShapedRecipeJson fluidPipesStainedGlass = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_fluid_pipe", 16, "CCC",
                    "rPr", "CCC").addInput('C', "modern_industrialization:bronze_curved_plate").addInput('r', "modern_industrialization:copper_rotor")
                            .addInput('P', color.getName() + "_stained_glass_pane");
            pack.addData(new MIIdentifier(pathPrefix + "craft/fluid_pipe_stained_glass_asbl.json"), GSON.toJson(fluidPipesStainedGlass).getBytes());
            // 8 fluid pipes
            ShapedRecipeJson eightFluidPipes = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_fluid_pipe", 8, "ppp", "pdp",
                    "ppp").addInput('d', "#c:" + color.getName() + "_dyes").addInput('p', "#modern_industrialization:fluid_pipes");
            pack.addData(new MIIdentifier(pathPrefix + "craft/fluid_pipe_8.json"), GSON.toJson(eightFluidPipes).getBytes());
            pack.addData(new MIIdentifier(pathPrefix + "mixer/fluid_pipe_8.json"),
                    GSON.toJson(eightFluidPipes.exportToMachine(MIMachineRecipeTypes.MIXER, 2, 100, 1)).getBytes());
            // 1 fluid pipe
            ShapedRecipeJson oneFluidPipe = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_fluid_pipe", 1, "pd")
                    .addInput('d', "#c:" + color.getName() + "_dyes").addInput('p', "#modern_industrialization:fluid_pipes");
            pack.addData(new MIIdentifier(pathPrefix + "craft/fluid_pipe_1.json"), GSON.toJson(oneFluidPipe).getBytes());
            // generate dyes with synthetic oil
            MIRecipeJson syntheticOilDye = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 200).addFluidInput(MIFluids.SYNTHETIC_OIL, 100)
                    .addItemInput("#c:" + color.getName() + "_dyes", 1, 0).addItemOutput("minecraft:" + color.getName() + "_dye", 1);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/synthetic_oil.json"), GSON.toJson(syntheticOilDye).getBytes());
            // generate dyes with benzene
            MIRecipeJson benzeneDye = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 200).addFluidInput(MIFluids.BENZENE, 25)
                    .addItemInput("#c:" + color.getName() + "_dyes", 1, 0).addItemOutput("minecraft:" + color.getName() + "_dye", 1);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/benzene.json"), GSON.toJson(benzeneDye).getBytes());

            // wool
            MIRecipeJson wool = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 100).addItemInput("#c:" + color.getName() + "_dyes", 1)
                    .addItemInput("#minecraft:wool", 8).addItemOutput("minecraft:" + color.getName() + "_wool", 8);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/wool.json"), GSON.toJson(wool).getBytes());

            // glass
            MIRecipeJson glass = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 100).addItemInput("#c:" + color.getName() + "_dyes", 1)
                    .addItemInput("#c:glass", 8)
                    .addItemOutput("minecraft:" + color.getName() + "_stained_glass", 8);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/glass.json"), GSON.toJson(glass).getBytes());

            // glassPane
            MIRecipeJson glassPane = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 100).addItemInput("#c:" + color.getName() + "_dyes", 1)
                    .addItemInput("#c:glass_pane", 8).addItemOutput("minecraft:" + color.getName() + "_stained_glass_pane", 8);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/glass_pane.json"), GSON.toJson(glassPane).getBytes());

            // shulker Box
            MIRecipeJson shulkerBox = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 100).addItemInput("#c:" + color.getName() + "_dyes", 1)
                    .addItemInput("#c:shulker_box", 1).addItemOutput("minecraft:" + color.getName() + "_shulker_box", 1);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/shulker_box.json"), GSON.toJson(shulkerBox).getBytes());

            // bed
            MIRecipeJson bed = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 100).addItemInput("#c:" + color.getName() + "_dyes", 1)
                    .addItemInput("#minecraft:beds", 1)
                    .addItemOutput("minecraft:" + color.getName() + "_bed", 1);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/bed.json"), GSON.toJson(bed).getBytes());

            // candle
            MIRecipeJson candle = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 100).addItemInput("#c:" + color.getName() + "_dyes", 1)
                    .addItemInput("#minecraft:candles", 1).addItemOutput("minecraft:" + color.getName() + "_candle", 1);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/candle.json"), GSON.toJson(candle).getBytes());

            // carpet
            MIRecipeJson carpet = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 100).addItemInput("#c:" + color.getName() + "_dyes", 1)
                    .addItemInput("#minecraft:carpets", 8).addItemOutput("minecraft:" + color.getName() + "_carpet", 8);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/carpet.json"), GSON.toJson(carpet).getBytes());

            // terracotta
            MIRecipeJson terracotta = MIRecipeJson.create(MIMachineRecipeTypes.MIXER, 2, 100).addItemInput("#c:" + color.getName() + "_dyes", 1)
                    .addItemInput("#c:terracottas", 8).addItemOutput("minecraft:" + color.getName() + "_terracotta", 8);
            pack.addData(new MIIdentifier(pathPrefix + "mixer/terracotta.json"), GSON.toJson(terracotta).getBytes());

            // glass pane cutting
            MIRecipeJson glassPaneCutting = MIRecipeJson.create(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100).addFluidInput(MIFluids.LUBRICANT, 1)
                    .addItemInput("minecraft:" + color.getName() + "_stained_glass", 6)
                    .addItemOutput("minecraft:" + color.getName() + "_stained_glass_pane", 16);

            pack.addData(new MIIdentifier(pathPrefix + "cutting_machine/glass_pane.json"), GSON.toJson(glassPaneCutting).getBytes());

            // carpet cutting
            MIRecipeJson carpetCutting = MIRecipeJson.create(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100).addFluidInput(MIFluids.LUBRICANT, 1)
                    .addItemInput("minecraft:" + color.getName() + "_wool", 1).addItemOutput("minecraft:" + color.getName() + "_carpet", 4);

            pack.addData(new MIIdentifier(pathPrefix + "cutting_machine/carpet.json"), GSON.toJson(carpetCutting).getBytes());

        }
    }
}

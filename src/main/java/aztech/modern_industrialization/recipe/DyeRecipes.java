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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import aztech.modern_industrialization.util.ResourceUtil;
import com.google.gson.Gson;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class DyeRecipes {
    private static final Gson GSON = new Gson();

    static {
        initTags();
    }

    private static void initTags() {
        for (DyeColor color : DyeColor.values()) {
            Identifier tagId = new Identifier("c", color.getName() + "_dyes");
            TagRegistry.item(tagId);
            ResourceUtil.appendToItemTag(tagId, new Identifier("minecraft:" + color.getName() + "_dye"));
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
                    GSON.toJson(eightItemPipes.exportToMachine("mixer", 2, 200, 1)).getBytes());
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
                    GSON.toJson(eightFluidPipes.exportToMachine("mixer", 2, 200, 1)).getBytes());
            // 1 fluid pipe
            ShapedRecipeJson oneFluidPipe = new ShapedRecipeJson("modern_industrialization:" + color.getName() + "_fluid_pipe", 1, "pd")
                    .addInput('d', "#c:" + color.getName() + "_dyes").addInput('p', "#modern_industrialization:fluid_pipes");
            pack.addData(new MIIdentifier(pathPrefix + "craft/fluid_pipe_1.json"), GSON.toJson(oneFluidPipe).getBytes());
        }
    }
}

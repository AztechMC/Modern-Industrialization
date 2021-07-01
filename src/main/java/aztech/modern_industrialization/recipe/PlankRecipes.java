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
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import com.google.gson.Gson;
import net.devtech.arrp.api.RuntimeResourcePack;

public final class PlankRecipes {
    private static final Gson GSON = new Gson();

    public static void yes(RuntimeResourcePack pack) {
        genPlanks(pack, "oak", "logs");
        genPlanks(pack, "spruce", "logs");
        genPlanks(pack, "birch", "logs");
        genPlanks(pack, "jungle", "logs");
        genPlanks(pack, "acacia", "logs");
        genPlanks(pack, "dark_oak", "logs");
        genPlanks(pack, "crimson", "stems");
        genPlanks(pack, "warped", "stems");
    }

    private static void genPlanks(RuntimeResourcePack pack, String prefix, String suffix) {
        MIRecipeJson json = new MIRecipeJson("cutting_machine", 2, 200).addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("#minecraft:" + prefix + "_" + suffix, 1).addOutput("minecraft:" + prefix + "_planks", 4);
        pack.addData(new MIIdentifier("recipes/generated/planks/" + prefix + ".json"), GSON.toJson(json).getBytes());
    }

    private PlankRecipes() {
    }
}

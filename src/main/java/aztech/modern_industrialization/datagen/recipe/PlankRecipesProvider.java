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
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;

public final class PlankRecipesProvider extends MIRecipesProvider {

    public PlankRecipesProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> consumer) {
        genPlanks(consumer, "oak", "logs");
        genPlanks(consumer, "spruce", "logs");
        genPlanks(consumer, "birch", "logs");
        genPlanks(consumer, "jungle", "logs");
        genPlanks(consumer, "acacia", "logs");
        genPlanks(consumer, "dark_oak", "logs");
        genPlanks(consumer, "crimson", "stems");
        genPlanks(consumer, "warped", "stems");
    }

    private static void genPlanks(Consumer<FinishedRecipe> consumer, String prefix, String suffix) {
        MIRecipeJson json = MIRecipeJson.create(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100).addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("#minecraft:" + prefix + "_" + suffix, 1).addItemOutput("minecraft:" + prefix + "_planks", 4);

        json.offerTo(consumer, "cutting_machine/planks/" + prefix);

        MIRecipeJson slab = MIRecipeJson.create(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100).addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("minecraft:" + prefix + "_planks", 1).addItemOutput("minecraft:" + prefix + "_slab", 2);

        slab.offerTo(consumer, "cutting_machine/slabs/" + prefix);
    }

}

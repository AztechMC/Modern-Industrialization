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
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;

public final class PlankRecipesProvider extends MIRecipesProvider {

    public PlankRecipesProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        genPlanks(consumer, "oak", true);
        genPlanks(consumer, "spruce", true);
        genPlanks(consumer, "birch", true);
        genPlanks(consumer, "jungle", true);
        genPlanks(consumer, "acacia", true);
        genPlanks(consumer, "dark_oak", true);
        genPlanks(consumer, "mangrove", true);
        genPlanks(consumer, "crimson", false);
        genPlanks(consumer, "warped", false);
    }

    private static void genPlanks(RecipeOutput consumer, String prefix, boolean log) {

        String suffixTag = log ? "logs" : "stems";
        String suffix = log ? "log" : "stem";

        String packedSuffix = log ? "wood" : "hyphae";

        var planks = new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100).addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("#minecraft:" + prefix + "_" + suffixTag, 1).addItemOutput("minecraft:" + prefix + "_planks", 6);

        planks.offerTo(consumer, "cutting_machine/planks/" + prefix);

        var stripped = new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100).addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("minecraft:" + prefix + "_" + suffix, 1).addItemOutput("minecraft:stripped_" + prefix + "_" + suffix, 1);

        stripped.offerTo(consumer, "cutting_machine/stripped/" + prefix);

        var strippedWood = new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100).addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("minecraft:" + prefix + "_" + packedSuffix, 1).addItemOutput("minecraft:stripped_" + prefix + "_" + packedSuffix, 1);

        strippedWood.offerTo(consumer, "cutting_machine/stripped_wood/" + prefix);

        var slab = new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100).addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("minecraft:" + prefix + "_planks", 1).addItemOutput("minecraft:" + prefix + "_slab", 2);

        slab.offerTo(consumer, "cutting_machine/slabs/" + prefix);

        // packer

        var packedWood = new MachineRecipeBuilder(MIMachineRecipeTypes.PACKER, 2, 100).addItemInput("minecraft:" + prefix + "_" + suffix, 4)
                .addItemOutput("minecraft:" + prefix + "_" + packedSuffix, 3);
        packedWood.offerTo(consumer, "packer/wood/" + prefix);

        var packedStrippedWood = new MachineRecipeBuilder(MIMachineRecipeTypes.PACKER, 2, 100)
                .addItemInput("minecraft:stripped_" + prefix + "_" + suffix, 4)
                .addItemOutput("minecraft:stripped_" + prefix + "_" + packedSuffix, 3);
        packedStrippedWood.offerTo(consumer, "packer/stripped_wood/" + prefix);
    }

}

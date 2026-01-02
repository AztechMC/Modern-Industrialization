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
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public final class PlankRecipeProvider extends MIRecipeProvider {
    PlankRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    public void buildRecipes() {
        genPlanks("oak", true);
        genPlanks("spruce", true);
        genPlanks("birch", true);
        genPlanks("jungle", true);
        genPlanks("acacia", true);
        genPlanks("dark_oak", true);
        genPlanks("mangrove", true);
        genPlanks("cherry", true);
        genPlanks("crimson", false);
        genPlanks("warped", false);
    }

    private void genPlanks(String prefix, boolean log) {
        String suffixTag = log ? "logs" : "stems";
        String suffix = log ? "log" : "stem";

        String packedSuffix = log ? "wood" : "hyphae";

        new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100)
                .addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput(tag("minecraft:" + prefix + "_" + suffixTag), 1)
                .addItemOutput("minecraft:" + prefix + "_planks", 6)
                .offerTo(output, "cutting_machine/planks/" + prefix);

        new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100)
                .addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("minecraft:" + prefix + "_" + suffix, 1)
                .addItemOutput("minecraft:stripped_" + prefix + "_" + suffix, 1)
                .offerTo(output, "cutting_machine/stripped/" + prefix);

        new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100)
                .addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("minecraft:" + prefix + "_" + packedSuffix, 1)
                .addItemOutput("minecraft:stripped_" + prefix + "_" + packedSuffix, 1)
                .offerTo(output, "cutting_machine/stripped_wood/" + prefix);

        new MachineRecipeBuilder(MIMachineRecipeTypes.CUTTING_MACHINE, 2, 100)
                .addFluidInput(MIFluids.LUBRICANT, 1)
                .addItemInput("minecraft:" + prefix + "_planks", 1)
                .addItemOutput("minecraft:" + prefix + "_slab", 2)
                .offerTo(output, "cutting_machine/slabs/" + prefix);

        // packer

        new MachineRecipeBuilder(MIMachineRecipeTypes.PACKER, 2, 100)
                .addItemInput("minecraft:" + prefix + "_" + suffix, 4)
                .addItemOutput("minecraft:" + prefix + "_" + packedSuffix, 3)
                .offerTo(output, "packer/wood/" + prefix);

        new MachineRecipeBuilder(MIMachineRecipeTypes.PACKER, 2, 100)
                .addItemInput("minecraft:stripped_" + prefix + "_" + suffix, 4)
                .addItemOutput("minecraft:stripped_" + prefix + "_" + packedSuffix, 3)
                .offerTo(output, "packer/stripped_wood/" + prefix);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new PlankRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Plank Recipes";
        }
    }
}

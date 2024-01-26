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
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.definition.FluidLike;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluids;

public class HeatExchangerRecipesProvider extends MIRecipesProvider {

    public HeatExchangerRecipesProvider(PackOutput packOutput) {
        super(packOutput);
    }

    private static String fluidToString(FluidLike f, boolean id) {
        if (f.asFluid() == Fluids.WATER) {
            return (id ? "minecraft:" : "") + "water";
        } else if (f.asFluid() == Fluids.LAVA) {
            return (id ? "minecraft:" : "") + "lava";
        } else {
            return ((FluidDefinition) f).getResourceAsString(id);
        }
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        FluidLike[] hots = { MIFluids.STEAM, MIFluids.HEAVY_WATER_STEAM, MIFluids.HIGH_PRESSURE_HEAVY_WATER_STEAM, MIFluids.HIGH_PRESSURE_STEAM };
        FluidLike[] cold = { FluidLike.of(Fluids.WATER), MIFluids.HEAVY_WATER, MIFluids.HIGH_PRESSURE_HEAVY_WATER, MIFluids.HIGH_PRESSURE_WATER };
        int[] amount = { 1, 1, 8, 8 };

        int amountBaseHot = 16000;
        int amountBaseCold = 1000;

        for (int i = 0; i < hots.length; i++) {
            for (int j = 0; j < cold.length; j++) {
                if (i != j) {

                    var recipe = new MachineRecipeBuilder(MIMachineRecipeTypes.HEAT_EXCHANGER, 2, 300);

                    String path = "heat_exchanger/" + fluidToString(hots[i], false) + "_with_"
                            + fluidToString(cold[j], false);

                    recipe.addFluidInput(fluidToString(hots[i], true), amountBaseHot / amount[i]);
                    recipe.addFluidInput(fluidToString(cold[j], true), amountBaseCold / amount[j]);

                    recipe.addFluidOutput(fluidToString(cold[i], true), amountBaseCold / amount[i]);
                    recipe.addFluidOutput(fluidToString(hots[j], true), amountBaseHot / amount[j]);

                    recipe.offerTo(consumer, path);
                }

            }
        }

    }
}

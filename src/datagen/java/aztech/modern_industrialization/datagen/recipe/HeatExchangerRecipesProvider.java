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
import aztech.modern_industrialization.fluid.CraftingFluid;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class HeatExchangerRecipesProvider extends MIRecipesProvider {

    public HeatExchangerRecipesProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    private static String fluidToString(Fluid f, boolean id) {
        if (f == Fluids.WATER) {
            return (id ? "minecraft:" : "") + "water";
        } else if (f == Fluids.LAVA) {
            return (id ? "minecraft:" : "") + "lava";
        } else {
            CraftingFluid fluid = (CraftingFluid) f;
            return (id ? "modern_industrialization:" : "") + fluid.name;
        }
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> consumer) {
        Fluid[] hots = { MIFluids.STEAM, MIFluids.HEAVY_WATER_STEAM, MIFluids.HIGH_PRESSURE_HEAVY_WATER_STEAM, MIFluids.HIGH_PRESSURE_STEAM };
        Fluid[] cold = { Fluids.WATER, MIFluids.HEAVY_WATER, MIFluids.HIGH_PRESSURE_HEAVY_WATER, MIFluids.HIGH_PRESSURE_WATER };
        int[] amount = { 1, 1, 8, 8 };

        int amountBaseHot = 16000;
        int amountBaseCold = 1000;

        for (int i = 0; i < hots.length; i++) {
            for (int j = 0; j < cold.length; j++) {
                if (i != j) {

                    MIRecipeJson recipe = MIRecipeJson.create(MIMachineRecipeTypes.HEAT_EXCHANGER, 2, 300);

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

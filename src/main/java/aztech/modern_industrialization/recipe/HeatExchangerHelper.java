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
import aztech.modern_industrialization.MIRuntimeResourcePack;
import aztech.modern_industrialization.fluid.CraftingFluid;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public class HeatExchangerHelper {

    private static final Gson GSON = new Gson();

    public static void init(MIRuntimeResourcePack pack) {
        Fluid[] hots = { MIFluids.STEAM, MIFluids.HEAVY_WATER_STEAM, MIFluids.HIGH_PRESSURE_HEAVY_WATER_STEAM, MIFluids.HIGH_PRESSURE_STEAM };
        Fluid[] cold = { Fluids.WATER, MIFluids.HEAVY_WATER, MIFluids.HIGH_PRESSURE_HEAVY_WATER, MIFluids.HIGH_PRESSURE_WATER };
        int[] amount = { 1, 1, 8, 8 };

        int amountBaseHot = 6400;
        int amountBaseCold = 400;

        for (int i = 0; i < hots.length; i++) {
            for (int j = 0; j < cold.length; j++) {
                if (i != j) {
                    String path = "modern_industrialization/recipes/generated/heat_exchanger/" + fluidToString(hots[i], false) + "_with_"
                            + fluidToString(cold[j], false) + ".json";

                    ArrayList<HeatExchangerFluidRecipe.FluidIO> inputs = new ArrayList<>();
                    ArrayList<HeatExchangerFluidRecipe.FluidIO> outputs = new ArrayList<>();

                    inputs.add(new HeatExchangerFluidRecipe.FluidIO(fluidToString(hots[i], true), amountBaseHot / amount[i]));
                    inputs.add(new HeatExchangerFluidRecipe.FluidIO(fluidToString(cold[j], true), amountBaseCold / amount[j]));

                    outputs.add(new HeatExchangerFluidRecipe.FluidIO(fluidToString(cold[i], true), amountBaseCold / amount[i]));
                    outputs.add(new HeatExchangerFluidRecipe.FluidIO(fluidToString(hots[j], true), amountBaseHot / amount[j]));

                    HeatExchangerFluidRecipe recipe = new HeatExchangerFluidRecipe(inputs, outputs);

                    pack.addData(path, GSON.toJson(recipe).getBytes());
                }

            }
        }

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

    private static class HeatExchangerFluidRecipe {

        private final List<FluidIO> fluid_inputs;
        private final List<FluidIO> fluid_outputs;
        private final long eu = 2;
        private final long duration = 300;
        private final String type = "modern_industrialization:heat_exchanger";

        private HeatExchangerFluidRecipe(List<FluidIO> fluid_inputs, List<FluidIO> fluid_outputs) {
            this.fluid_inputs = fluid_inputs;
            this.fluid_outputs = fluid_outputs;
        }

        public static class FluidIO {
            final String fluid;
            final int amount;

            private FluidIO(String fluid, int amount) {
                this.fluid = fluid;
                this.amount = amount;
            }
        }

    }
}

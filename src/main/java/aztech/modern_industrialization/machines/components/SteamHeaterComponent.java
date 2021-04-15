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
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import java.util.List;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public class SteamHeaterComponent extends TemperatureComponent {

    public final long maxEuProduction;
    public final long euPerDegree;

    public final boolean acceptHighPressure;
    public final boolean acceptLowPressure;

    public SteamHeaterComponent(double temperatureMax, long maxEuProduction, long euPerDegree) {
        super(temperatureMax);
        this.maxEuProduction = maxEuProduction;
        this.euPerDegree = euPerDegree;
        this.acceptLowPressure = true;
        this.acceptHighPressure = false;
    }

    public SteamHeaterComponent(double temperatureMax, long maxEuProduction, long euPerDegree, boolean acceptLowPressure,
            boolean acceptHighPressure) {
        super(temperatureMax);
        this.maxEuProduction = maxEuProduction;
        this.euPerDegree = euPerDegree;
        this.acceptLowPressure = acceptLowPressure;
        this.acceptHighPressure = acceptHighPressure;
    }

    public void tick(List<ConfigurableFluidStack> fluidInputs, List<ConfigurableFluidStack> fluidOutputs) {

        Fluid[] inputs = new Fluid[] { Fluids.WATER, MIFluids.HEAVY_WATER, MIFluids.HIGH_PRESSURE_WATER, MIFluids.HIGH_PRESSURE_HEAVY_WATER };

        Fluid[] outputs = new Fluid[] { MIFluids.STEAM, MIFluids.HEAVY_WATER_STEAM, MIFluids.HIGH_PRESSURE_STEAM,
                MIFluids.HIGH_PRESSURE_HEAVY_WATER_STEAM };

        int[] factors = new int[] { 1, 1, 8, 8 };

        for (int i = 0; i < 4; i++) {
            if ((i < 2 && acceptLowPressure) || (i >= 2 && acceptHighPressure)) {
                if (getTemperature() > 100d) {
                    long steamProduction = (long) (81 * (getTemperature() - 100d) / (temperatureMax - 100d) * maxEuProduction / factors[i]);
                    long maxFluidExtract = 0;

                    for (ConfigurableFluidStack fluidStack : fluidInputs) {
                        if (fluidStack.getFluid() == inputs[i]) {
                            maxFluidExtract += fluidStack.getAmount();
                        }
                    }

                    long maxInsertSteam = 0;

                    for (ConfigurableFluidStack fluidStack : fluidOutputs) {
                        if (fluidStack.isValid(outputs[i])) {
                            maxInsertSteam += fluidStack.getRemainingSpace();
                        }
                    }

                    long effSteamProduced = Math.min(Math.min(steamProduction, maxFluidExtract * 16), maxInsertSteam);
                    decreaseTemperature((double) effSteamProduced * factors[i] / (81 * euPerDegree));

                    long fluidExtract = (long) Math.ceil(effSteamProduced / 16f);

                    for (ConfigurableFluidStack fluidStack : fluidInputs) {
                        if (fluidStack.getFluid() == inputs[i]) {
                            long decrement = Math.min(fluidStack.getAmount(), fluidExtract);
                            fluidExtract -= decrement;
                            fluidStack.decrement(decrement);
                        }
                    }

                    long steamInsert = effSteamProduced;

                    for (ConfigurableFluidStack fluidStack : fluidOutputs) {
                        if (fluidStack.isValid(outputs[i])) {
                            long increment = Math.min(fluidStack.getRemainingSpace(), steamInsert);
                            steamInsert -= increment;
                            fluidStack.setFluid(outputs[i]);
                            fluidStack.increment(increment);
                        }
                    }
                }
            }
        }

    }
}

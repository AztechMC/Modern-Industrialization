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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public class SteamHeaterComponent extends TemperatureComponent {
    private static final int STEAM_TO_WATER = 16;

    /**
     * mb/t of steam produced at max heat, assuming enough water
     */
    public final long maxEuProduction;
    /**
     * How many eu in one degree of heat.
     */
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
        if (acceptLowPressure) {
            if (!tryMakeSteam(fluidInputs, fluidOutputs, Fluids.WATER, MIFluids.STEAM, 1)) {
                tryMakeSteam(fluidInputs, fluidOutputs, MIFluids.HEAVY_WATER, MIFluids.HEAVY_WATER_STEAM, 1);
            }
        }
        if (acceptHighPressure) {
            if (!tryMakeSteam(fluidInputs, fluidOutputs, MIFluids.HIGH_PRESSURE_WATER, MIFluids.HIGH_PRESSURE_STEAM, 8)) {
                tryMakeSteam(fluidInputs, fluidOutputs, MIFluids.HIGH_PRESSURE_HEAVY_WATER, MIFluids.HIGH_PRESSURE_HEAVY_WATER_STEAM, 8);
            }
        }
    }

    // Return true if any steam was made.
    private boolean tryMakeSteam(List<ConfigurableFluidStack> fluidInputs, List<ConfigurableFluidStack> fluidOutputs, Fluid water, Fluid steam,
            int euPerSteamMb) {
        FluidVariant waterKey = FluidVariant.of(water);
        FluidVariant steamKey = FluidVariant.of(steam);

        if (getTemperature() > 100d) {
            long steamProduction = (long) (81 * (getTemperature() - 100d) / (temperatureMax - 100d) * maxEuProduction / euPerSteamMb);

            // Check how much water and steam are available
            long availableWater = 0;
            for (ConfigurableFluidStack fluidStack : fluidInputs) {
                if (fluidStack.getFluid().equals(waterKey)) {
                    availableWater += fluidStack.getAmount();
                }
            }

            long remainingSpaceForSteam = 0;
            for (ConfigurableFluidStack fluidStack : fluidOutputs) {
                if (fluidStack.isValid(steamKey)) {
                    remainingSpaceForSteam += fluidStack.getRemainingSpace();
                }
            }

            // Compute steam production
            long effSteamProduced = Math.min(Math.min(steamProduction, availableWater * STEAM_TO_WATER), remainingSpaceForSteam);
            if (effSteamProduced == 0) {
                return false;
            }
            // Lose temperature accordingly
            double euProduced = effSteamProduced * euPerSteamMb / 81d;
            decreaseTemperature(euProduced / euPerDegree);

            // Consume water and produce steam
            // (always consume at least 1 mb = 81 dp)
            long remainingWaterToConsume = Math.max((long) Math.ceil((double) effSteamProduced / STEAM_TO_WATER), 81);
            for (ConfigurableFluidStack fluidStack : fluidInputs) {
                if (fluidStack.getFluid().equals(waterKey)) {
                    long decrement = Math.min(fluidStack.getAmount(), remainingWaterToConsume);
                    remainingWaterToConsume -= decrement;
                    fluidStack.decrement(decrement);
                }
            }

            long remainingSteamToProduce = effSteamProduced;
            for (ConfigurableFluidStack fluidStack : fluidOutputs) {
                if (fluidStack.isValid(steamKey)) {
                    long increment = Math.min(fluidStack.getRemainingSpace(), remainingSteamToProduce);
                    remainingSteamToProduce -= increment;
                    fluidStack.setFluid(steamKey);
                    fluidStack.increment(increment);
                }
            }

            return true;
        } else {
            return false;
        }
    }
}

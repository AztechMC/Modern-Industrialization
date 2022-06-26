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
import aztech.modern_industrialization.inventory.MIFluidStorage;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

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

    public final boolean requiresContinuousOperation;
    public static final double INPUT_ENERGY_RATIO_FOR_STARTUP = 0.8; // only if requires continuous operation

    public SteamHeaterComponent(double temperatureMax, long maxEuProduction, long euPerDegree) {
        this(maxEuProduction, maxEuProduction, euPerDegree, true, false, false);
    }

    public SteamHeaterComponent(double temperatureMax, long maxEuProduction, long euPerDegree, boolean acceptLowPressure,
            boolean acceptHighPressure, boolean requiresContinuousOperation) {
        super(temperatureMax);
        this.maxEuProduction = maxEuProduction;
        this.euPerDegree = euPerDegree;
        this.acceptLowPressure = acceptLowPressure;
        this.acceptHighPressure = acceptHighPressure;
        this.requiresContinuousOperation = requiresContinuousOperation;
    }

    // return eu produced
    public double tick(List<ConfigurableFluidStack> fluidInputs, List<ConfigurableFluidStack> fluidOutputs) {

        double euProducedLowPressure = 0;
        if (acceptLowPressure) {
            euProducedLowPressure = tryMakeSteam(fluidInputs, fluidOutputs, Fluids.WATER, MIFluids.STEAM.asFluid(), 1);
            if (euProducedLowPressure == 0) {
                euProducedLowPressure = tryMakeSteam(fluidInputs, fluidOutputs, MIFluids.HEAVY_WATER.asFluid(), MIFluids.HEAVY_WATER_STEAM.asFluid(),
                        1);
            }
        }

        double euProducedHighPressure = 0;
        if (acceptHighPressure) {
            euProducedHighPressure = tryMakeSteam(fluidInputs, fluidOutputs, MIFluids.HIGH_PRESSURE_WATER.asFluid(),
                    MIFluids.HIGH_PRESSURE_STEAM.asFluid(), 8);
            if (euProducedHighPressure == 0) {
                euProducedHighPressure = tryMakeSteam(fluidInputs, fluidOutputs, MIFluids.HIGH_PRESSURE_HEAVY_WATER.asFluid(),
                        MIFluids.HIGH_PRESSURE_HEAVY_WATER_STEAM.asFluid(), 8);
            }
        }

        double totalEuProduced = euProducedLowPressure + euProducedHighPressure;

        if (this.requiresContinuousOperation) {
            this.decreaseTemperature(INPUT_ENERGY_RATIO_FOR_STARTUP * (this.maxEuProduction - totalEuProduced) / this.euPerDegree);
        }

        return totalEuProduced;
    }

    private double tryMakeSteam(List<ConfigurableFluidStack> input, List<ConfigurableFluidStack> output, Fluid water, Fluid steam, int euPerSteamMb) {
        return tryMakeSteam(new MIFluidStorage(input), new MIFluidStorage(output), water, steam, euPerSteamMb);

    }

    // Return true if any steam was made.
    private double tryMakeSteam(MIFluidStorage input, MIFluidStorage output, Fluid water, Fluid steam, int euPerSteamMb) {

        FluidVariant waterKey = FluidVariant.of(water);
        FluidVariant steamKey = FluidVariant.of(steam);

        if (getTemperature() > 100d) {
            long steamProduction = (long) (81 * (getTemperature() - 100d) / (temperatureMax - 100d) * maxEuProduction / euPerSteamMb);

            try (Transaction tx = Transaction.openOuter()) {
                long inserted;
                try (Transaction simul = Transaction.openNested(tx)) { // insertion Simulation
                    inserted = output.insertAllSlot(steamKey, steamProduction, simul);
                }
                if (inserted > 0) {
                    long extracted = input.extractAllSlot(waterKey, inserted / STEAM_TO_WATER, tx);
                    if (extracted > 0) {
                        if (output.insertAllSlot(steamKey, extracted * STEAM_TO_WATER, tx) == extracted * STEAM_TO_WATER) {
                            double euProduced = extracted * STEAM_TO_WATER * euPerSteamMb / 81d;
                            decreaseTemperature(euProduced / euPerDegree);
                            tx.commit();
                            return euProduced;
                        } else {
                            throw new IllegalStateException("Steam Component : Logic bug: failed to insert");
                        }
                    }
                }
            }
        }
        return 0;
    }
}

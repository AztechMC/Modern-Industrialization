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
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    /**
     * Amount of steam for which we already consumed the water.
     */
    private final Reference2IntMap<Fluid> steamBuffer = new Reference2IntOpenHashMap<>();

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

    public void tickDisabled() {
        this.decreaseTemperature(INPUT_ENERGY_RATIO_FOR_STARTUP * this.maxEuProduction / this.euPerDegree);
    }

    private double tryMakeSteam(List<ConfigurableFluidStack> input, List<ConfigurableFluidStack> output, Fluid water, Fluid steam, int euPerSteamMb) {
        return tryMakeSteam(new MIFluidStorage(input), new MIFluidStorage(output), water, steam, euPerSteamMb);
    }

    // Return true if any steam was made.
    private double tryMakeSteam(MIFluidStorage input, MIFluidStorage output, Fluid water, Fluid steam, int euPerSteamMb) {
        FluidResource waterKey = FluidResource.of(water);
        FluidResource steamKey = FluidResource.of(steam);

        if (getTemperature() > 100d) {
            int steamProduction = (int) ((getTemperature() - 100d) / (temperatureMax - 100d) * maxEuProduction / euPerSteamMb);

            try (Transaction tx = Transaction.openRoot()) {
                int inserted;
                try (Transaction simul = Transaction.open(tx)) { // insertion Simulation
                    inserted = output.insertAllSlot(steamKey, steamProduction, simul);
                }
                if (inserted > 0) {
                    // Round water consumption up
                    int waterToUse = (inserted - steamBuffer.getInt(steam) + STEAM_TO_WATER - 1) / STEAM_TO_WATER;
                    // Extract water
                    int extracted = input.extractAllSlot(waterKey, waterToUse, tx);
                    // Add to steam buffer
                    steamBuffer.mergeInt(steam, extracted * STEAM_TO_WATER, Integer::sum);

                    // Produce steam
                    int producedSteam = output.insertAllSlot(steamKey, Math.min(steamProduction, steamBuffer.getInt(steam)), tx);
                    steamBuffer.mergeInt(steam, -producedSteam, Integer::sum);

                    double euProduced = producedSteam * euPerSteamMb;
                    decreaseTemperature(euProduced / euPerDegree);
                    tx.commit();
                    return euProduced;
                }
            }
        }
        return 0;
    }

    @Override
    public void writeNbt(ValueOutput output) {
        super.writeNbt(output);

        var buffer = new CompoundTag();
        for (var entry : steamBuffer.reference2IntEntrySet()) {
            if (entry.getIntValue() != 0) {
                buffer.putInt(entry.getKey().toString(), entry.getIntValue());
            }
        }
        output.store("steamBuffer", CompoundTag.CODEC, buffer);
    }

    @Override
    public void readNbt(ValueInput input, boolean isUpgradingMachine) {
        super.readNbt(input, isUpgradingMachine);

        var steamBuffer = input.read("steamBuffer", CompoundTag.CODEC).orElseThrow();
        for (var key : steamBuffer.keySet()) {
            var fluid = BuiltInRegistries.FLUID.getValue(Identifier.tryParse(key));
            if (fluid != Fluids.EMPTY) {
                this.steamBuffer.put(fluid, steamBuffer.getIntOr(key, 0));
            }
        }
    }
}

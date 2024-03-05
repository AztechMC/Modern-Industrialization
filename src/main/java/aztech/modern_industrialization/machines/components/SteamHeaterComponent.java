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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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

    /**
     * Amount of steam for which we already consumed the water.
     */
    private final Reference2LongMap<Fluid> steamBuffer = new Reference2LongOpenHashMap<>();

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
            long steamProduction = (long) ((getTemperature() - 100d) / (temperatureMax - 100d) * maxEuProduction / euPerSteamMb);

            try (Transaction tx = Transaction.openOuter()) {
                long inserted;
                try (Transaction simul = Transaction.openNested(tx)) { // insertion Simulation
                    inserted = output.insertAllSlot(steamKey, steamProduction, simul);
                }
                if (inserted > 0) {
                    // Round water consumption up
                    long waterToUse = (inserted - steamBuffer.getLong(steam) + STEAM_TO_WATER - 1) / STEAM_TO_WATER;
                    // Extract water
                    long extracted = input.extractAllSlot(waterKey, waterToUse, tx);
                    // Add to steam buffer
                    steamBuffer.mergeLong(steam, extracted * STEAM_TO_WATER, Long::sum);

                    // Produce steam
                    long producedSteam = output.insertAllSlot(steamKey, Math.min(steamProduction, steamBuffer.getLong(steam)), tx);
                    steamBuffer.mergeLong(steam, -producedSteam, Long::sum);

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
    public void writeNbt(CompoundTag tag) {
        super.writeNbt(tag);

        var buffer = new CompoundTag();
        for (var entry : steamBuffer.reference2LongEntrySet()) {
            if (entry.getLongValue() != 0) {
                buffer.putLong(entry.getKey().toString(), entry.getLongValue());
            }
        }
        tag.put("steamBuffer", buffer);
    }

    @Override
    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        super.readNbt(tag, isUpgradingMachine);

        var steamBuffer = tag.getCompound("steamBuffer");
        for (var key : steamBuffer.getAllKeys()) {
            var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(key));
            if (fluid != Fluids.EMPTY) {
                this.steamBuffer.put(fluid, steamBuffer.getLong(key));
            }
        }
    }
}

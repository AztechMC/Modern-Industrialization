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
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.api.datamaps.FluidFuel;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.util.ItemStackHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.CommonHooks;

public class FuelBurningComponent implements IComponent {
    /**
     * How many EUs worth of heat can be produced every tick at most.
     */
    public final long maxEuProduction;
    /**
     * How many EUs one degree of heat is worth.
     */
    public final long euPerDegree;
    public final TemperatureComponent temperature;
    /**
     * Multiplier on the efficiency of the boiler, i.e. the number of EUs produced
     * per burn tick.
     */
    private final long burningEuMultiplier;

    /**
     * How many EUs one furnace burn tick is worth. Remembering that 1 furnace
     * recipe = 2 EU/t for 10 seconds, EU_PER_BURN_TICK/2 is the efficiency
     * multiplier.
     */
    public static final long EU_PER_BURN_TICK = 20;

    /**
     * Buffer of EU that was burnt already, and is awaiting to be turned into heat.
     */
    private long burningEuBuffer;

    public FuelBurningComponent(TemperatureComponent temperature, long maxEuProduction, long euPerDegree, long burningEuMultiplier) {
        this.temperature = temperature;
        this.maxEuProduction = maxEuProduction;
        this.euPerDegree = euPerDegree;
        this.burningEuMultiplier = burningEuMultiplier;
    }

    public FuelBurningComponent(TemperatureComponent temperature, long maxEuProduction, long euPerDegree) {
        this(temperature, maxEuProduction, euPerDegree, 1);
    }

    public FuelBurningComponent(SteamHeaterComponent steamHeater, long burningEuMultiplier) {
        this(steamHeater, steamHeater.maxEuProduction, steamHeater.euPerDegree, burningEuMultiplier);
    }

    public FuelBurningComponent(SteamHeaterComponent steamHeater) {
        this(steamHeater, steamHeater.maxEuProduction, steamHeater.euPerDegree, 1);
    }

    public boolean isBurning() {
        return burningEuBuffer > 0;
    }

    public void disable() {
        burningEuBuffer = 0;
    }

    public double getBurningProgress() {
        return Math.min(1.0, (double) burningEuBuffer / (5 * 20 * maxEuProduction));
    }

    public void tick(List<ConfigurableItemStack> itemInputs, List<ConfigurableFluidStack> fluidInputs) {
        // Turn buffer into heat
        long maxEuInsertion = Math.min(burningEuBuffer, maxEuProduction);

        maxEuInsertion = Math.min(maxEuInsertion, (long) Math.floor(euPerDegree * (temperature.temperatureMax - temperature.getTemperature())));
        if (maxEuInsertion > 0) {
            burningEuBuffer -= maxEuInsertion;
            temperature.increaseTemperature((double) maxEuInsertion / euPerDegree);
        } else if (burningEuBuffer == 0) {
            temperature.decreaseTemperature(1);
        }

        // Refill buffer with item fuel
        outer: while (burningEuBuffer < maxEuProduction) {
            // Find first item fuel
            for (ConfigurableItemStack stack : itemInputs) {
                var fuel = stack.getResource().toStack((int) stack.getAmount());
                if (ItemStackHelper.consumeFuel(stack, true)) {
                    int fuelTime = CommonHooks.getBurnTime(fuel, null);
                    if (fuelTime > 0) {
                        burningEuBuffer += fuelTime * EU_PER_BURN_TICK * burningEuMultiplier;
                        ItemStackHelper.consumeFuel(stack, false);
                        continue outer;
                    }
                }
            }
            // Break if not found
            break;
        }
        // Refill buffer with fluid fuel
        outer: while (burningEuBuffer < 5 * 20 * maxEuProduction) {
            for (ConfigurableFluidStack stack : fluidInputs) {
                if (!stack.isEmpty()) {
                    long euPerMb = FluidFuel.getEu(stack.getResource().getFluid()) * burningEuMultiplier;
                    if (euPerMb != 0) {
                        long mbConsumedMax = (5 * 20 * maxEuProduction - burningEuBuffer) / euPerMb;
                        long mbConsumed = Math.min(mbConsumedMax, stack.getAmount());
                        if (mbConsumed > 0) {
                            stack.decrement(mbConsumed);
                            burningEuBuffer += mbConsumed * euPerMb;
                            continue outer;
                        }
                    }
                }
            }
            break;
        }
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putLong("burningEuBuffer", burningEuBuffer);

    }

    @Override
    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        burningEuBuffer = tag.getLong("burningEuBuffer");
    }

    public List<Component> getTooltips() {
        List<Component> returnList = new ArrayList<>();
        returnList.add(new MITooltips.Line(MIText.MaxEuProductionSteam).arg(
                this.maxEuProduction,
                MITooltips.EU_PER_TICK_PARSER)
                .arg(MIFluids.STEAM)
                .build());

        if (burningEuMultiplier == 2) {
            returnList.add(
                    new MITooltips.Line(MIText.DoubleFluidFuelEfficiency).build());
        }

        return returnList;
    }
}

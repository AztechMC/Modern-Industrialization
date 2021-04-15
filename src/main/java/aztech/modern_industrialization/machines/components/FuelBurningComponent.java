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

import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.util.ItemStackHelper;
import java.util.List;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

public class FuelBurningComponent implements IComponent {

    public final long maxEuProduction;
    public final long euPerDegree;
    public final TemperatureComponent temperature;
    private final long burningEuMultiplier;

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

        long maxEuInsertion = Math.min(burningEuBuffer, maxEuProduction);

        maxEuInsertion = Math.min(maxEuInsertion, (long) Math.floor(euPerDegree * (temperature.temperatureMax - temperature.getTemperature())));
        if (maxEuInsertion > 0) {
            burningEuBuffer -= maxEuInsertion;
            temperature.increaseTemperature((double) maxEuInsertion / euPerDegree);
        } else if (burningEuBuffer == 0) {
            temperature.decreaseTemperature(1);
        }

        boolean empty = false;
        while (burningEuBuffer == 0 && !empty) {
            empty = true;
            for (ConfigurableItemStack stack : itemInputs) {
                Item fuel = stack.getItemKey().getItem();
                if (ItemStackHelper.consumeFuel(stack, true)) {
                    Integer fuelTime = FuelRegistryImpl.INSTANCE.get(fuel);
                    if (fuelTime != null && fuelTime > 0) {
                        burningEuBuffer += fuelTime * 40 * burningEuMultiplier;
                        empty = false;
                        ItemStackHelper.consumeFuel(stack, false);
                        break;
                    }
                }
            }
        }
        empty = false;
        while (burningEuBuffer < 5 * 20 * maxEuProduction && !empty) {
            empty = true;
            for (ConfigurableFluidStack stack : fluidInputs) {
                if (!stack.isEmpty()) {
                    long euPerMb = FluidFuelRegistry.getEu(stack.getFluid()) * burningEuMultiplier;
                    if (euPerMb != 0) {
                        long mbConsumedMax = (5 * 20 * maxEuProduction - burningEuBuffer) / euPerMb;
                        long mbConsumed = Math.min(mbConsumedMax, stack.getAmount() / 81);
                        if (mbConsumed > 0) {
                            stack.decrement(mbConsumed * 81);
                            burningEuBuffer += mbConsumed * euPerMb;
                            empty = false;
                            break;
                        }
                    }
                }
            }

        }

    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putLong("burningEuBuffer", burningEuBuffer);

    }

    @Override
    public void readNbt(CompoundTag tag) {
        burningEuBuffer = tag.getLong("burningEuBuffer");
    }
}

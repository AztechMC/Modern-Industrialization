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

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.IComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

/**
 * A component that turns fluids into energy.
 */
public class FluidConsumerComponent implements IComponent.ServerOnly {
    private long euBuffer = 0;
    /**
     * The maximum EU that can be produced by one production operation, to limit the
     * maximum conversion rate of the machine.
     */
    private final long maxEuProduction;
    private final Predicate<Fluid> acceptedFluid;
    private final ToLongFunction<Fluid> fluidEUperMb;

    private final boolean fluidFuelsOnly;

    private FluidConsumerComponent(long maxEuProduction, Predicate<Fluid> acceptedFluid, ToLongFunction<Fluid> fluidEUperMb,
            boolean fluidFuelsOnly) {
        this.fluidFuelsOnly = fluidFuelsOnly;
        this.maxEuProduction = maxEuProduction;
        this.acceptedFluid = acceptedFluid;
        this.fluidEUperMb = fluidEUperMb;
    }

    public static FluidConsumerComponent of(long maxEuProduction, Predicate<Fluid> acceptedFluid, ToLongFunction<Fluid> fluidEUperMb) {
        return new FluidConsumerComponent(maxEuProduction, acceptedFluid, fluidEUperMb, false);
    }

    public static FluidConsumerComponent of(long maxEuProduction, Fluid acceptedFluid, long fluidEUperMb) {
        return of(maxEuProduction, (Fluid f) -> (f == acceptedFluid), (Fluid f) -> (fluidEUperMb));
    }

    public static FluidConsumerComponent ofFluidFuels(long maxEuProduction) {
        return new FluidConsumerComponent(maxEuProduction,
                (Fluid f) -> (FluidFuelRegistry.getEu(f) != 0),
                FluidFuelRegistry::getEu,
                true);
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putLong("euBuffer", euBuffer);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        euBuffer = tag.getLong("euBuffer");
    }

    public long getEuProduction(List<ConfigurableFluidStack> fluidInputs, long maxEnergyInsertable) {
        long maxEuProduced = Math.min(maxEnergyInsertable, maxEuProduction);

        if (maxEuProduced == 0) {
            return 0;
        }

        if (euBuffer >= maxEuProduced) {
            euBuffer -= maxEuProduced;
            return maxEuProduced;
        }

        long euProduced = 0;

        for (ConfigurableFluidStack stack : fluidInputs) {
            Fluid fluid = stack.getResource().getFluid();
            if (acceptedFluid.test(fluid) && stack.getAmount() > 0) {
                long fuelEu = fluidEUperMb.applyAsLong(fluid);
                long usedDroplets = Math.min((maxEuProduced - euProduced + fuelEu - 1) / fuelEu * 81, stack.getAmount());
                euProduced += usedDroplets * fuelEu / 81;
                stack.decrement(usedDroplets);

                if (euProduced >= maxEuProduced) {
                    euBuffer += euProduced - maxEuProduced;
                    return maxEuProduced;
                }
            }
        }
        return euProduced;
    }

    private record InformationEntry(long euPerMb, Fluid fluid) implements Comparable<InformationEntry> {
        @Override
        public int compareTo(@NotNull FluidConsumerComponent.InformationEntry o) {
            return Long.compare(euPerMb, o.euPerMb);
        }
    }

    public List<Component> getTooltips() {

        List<Component> returnList = new ArrayList<>();

        returnList.add(new MITooltips.Line(MIText.MaxEuProduction).arg(
                this.maxEuProduction,
                MITooltips.EU_PER_TICK_PARSER).build());

        if (this.fluidFuelsOnly) {
            returnList.add(new MITooltips.Line(MIText.AcceptAnyFluidFuels).build());

        } else {
            PriorityQueue<InformationEntry> informationEntries = new PriorityQueue<>();

            for (Fluid f : Registry.FLUID) {
                if (this.acceptedFluid.test(f)) {
                    informationEntries.add(
                            new InformationEntry(this.fluidEUperMb.applyAsLong(f), f));
                }
            }

            if (informationEntries.size() == 0) {
                throw new IllegalStateException("No fluids accepted for FluidConsumerComponent");
            } else if (informationEntries.size() == 1) {
                InformationEntry entry = informationEntries.poll();
                returnList.add(new MITooltips.Line(MIText.AcceptSingleFluid)
                        .arg(entry.fluid).arg(entry.euPerMb, MITooltips.EU_PARSER).build());
            } else {
                returnList.add(new MITooltips.Line(MIText.AcceptFollowingFluid).build());
                for (InformationEntry entry : informationEntries) {
                    returnList.add(
                            new MITooltips.Line(MIText.AcceptFollowingFluidEntry)
                                    .arg(entry.fluid).arg(entry.euPerMb, MITooltips.EU_PARSER).build());
                }
            }

        }
        return returnList;
    }

}

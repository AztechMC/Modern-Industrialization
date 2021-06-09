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

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.IComponent;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;

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

    public FluidConsumerComponent(long maxEuProduction, Predicate<Fluid> acceptedFluid, ToLongFunction<Fluid> fluidEUperMb) {
        this.maxEuProduction = maxEuProduction;
        this.acceptedFluid = acceptedFluid;
        this.fluidEUperMb = fluidEUperMb;
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putLong("euBuffer", euBuffer);
    }

    @Override
    public void readNbt(NbtCompound tag) {
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
            Fluid fluid = stack.getFluid().getFluid();
            if (acceptedFluid.test(fluid) && stack.getAmount() >= 81) {
                long fuelEu = fluidEUperMb.applyAsLong(fluid);
                long mbConsumedMax = Math.min((maxEuProduced - euProduced + fuelEu - 1) / fuelEu, stack.getAmount() / 81);
                euProduced += mbConsumedMax * fuelEu;
                stack.decrement(mbConsumedMax * 81);

                if (euProduced >= maxEuProduced) {
                    euBuffer += euProduced - maxEuProduced;
                    return maxEuProduced;
                }
            }
        }
        return euProduced;
    }

}

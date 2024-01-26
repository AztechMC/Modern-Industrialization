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
package aztech.modern_industrialization.machines.helper;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.util.Simulation;
import com.google.common.base.Preconditions;
import java.util.List;

public final class SteamHelper {
    public static long consumeSteamEu(List<ConfigurableFluidStack> fluidStacks, long maxEu, Simulation simulation) {
        Preconditions.checkArgument(maxEu >= 0, "May not consume < 0 EU.");

        long totalRem = 0;
        for (ConfigurableFluidStack stack : fluidStacks) {
            if (stack.getResource().getFluid() == MIFluids.STEAM.asFluid()) {
                long amount = stack.getAmount();
                long rem = Math.min(maxEu, amount);
                if (simulation.isActing()) {
                    stack.decrement(rem);
                }
                maxEu -= rem;
                totalRem += rem;
            }
        }
        return totalRem;
    }

    private SteamHelper() {
    }
}

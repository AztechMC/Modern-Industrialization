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
package aztech.modern_industrialization.nuclear;

import aztech.modern_industrialization.MIFluids;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public enum NuclearFluidCoolant {

    WATER(Fluids.WATER, MIFluids.STEAM, 1, 128, 0.2);

    public final Fluid fluid;
    public final Fluid fluidResult;
    public int heatConsumed;
    public double heatTransfer;
    public double neutronReflection;

    NuclearFluidCoolant(Fluid fluid, Fluid fluidResult, int heatConsumed, double heatTransfer, double neutronReflection) {
        this.fluid = fluid;
        this.fluidResult = fluidResult;
        this.heatConsumed = heatConsumed;
        this.heatTransfer = heatTransfer;
        this.neutronReflection = neutronReflection;
    }
}

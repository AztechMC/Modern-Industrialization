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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.fluid.Fluids;

public interface INuclearComponent<T extends TransferVariant> {

    double getHeatConduction();

    INeutronBehaviour getNeutronBehaviour();

    T getVariant();

    default T getNeutronProduct() {
        return null;
    }

    default long getNeutronProductAmount() {
        return 0;
    }

    default double getNeutronProductProbability() {
        return 1;
    }

    default int getMaxTemperature() {
        return Integer.MAX_VALUE;
    }

    static INuclearComponent of(FluidVariant variant, double heatConduction, double density, NuclearConstant.ScatteringType type,
            NuclearConstant.IsotopeParams params, FluidVariant neutronProduct, boolean highPressure) {

        return new INuclearComponent<FluidVariant>() {
            @Override
            public double getHeatConduction() {
                return heatConduction * density;
            }

            @Override
            public INeutronBehaviour getNeutronBehaviour() {
                return INeutronBehaviour.of(type, params, density);
            }

            public FluidVariant getVariant() {
                return variant;
            }

            public FluidVariant getNeutronProduct() {
                return neutronProduct;
            }

            public long getNeutronProductAmount() {
                return highPressure ? 8 : 1;
            }

            public double getNeutronProductProbability() {
                return highPressure ? 0.125 : 1;
            }
        };
    }

    public static INuclearComponent of(FluidVariant fluid) {
        if (fluid.equals(FluidVariant.of(Fluids.WATER))) {
            return INuclearComponent.of(fluid, NuclearConstant.BASE_HEAT_CONDUCTION * 5, 1, NuclearConstant.ScatteringType.ULTRA_LIGHT,
                    NuclearConstant.HYDROGEN, FluidVariant.of(MIFluids.DEUTERIUM), false);
        } else if (fluid.equals(FluidVariant.of(MIFluids.HEAVY_WATER))) {
            return INuclearComponent.of(fluid, NuclearConstant.BASE_HEAT_CONDUCTION * 6, 1, NuclearConstant.ScatteringType.LIGHT,
                    NuclearConstant.DEUTERIUM, FluidVariant.of(MIFluids.TRITIUM), false);
        } else if (fluid.equals(FluidVariant.of(MIFluids.HIGH_PRESSURE_WATER))) {
            return INuclearComponent.of(fluid, NuclearConstant.BASE_HEAT_CONDUCTION * 5, 2, NuclearConstant.ScatteringType.ULTRA_LIGHT,
                    NuclearConstant.HYDROGEN, FluidVariant.of(MIFluids.DEUTERIUM), true);
        } else if (fluid.equals(FluidVariant.of(MIFluids.HIGH_PRESSURE_HEAVY_WATER))) {
            return INuclearComponent.of(fluid, NuclearConstant.BASE_HEAT_CONDUCTION * 6, 2, NuclearConstant.ScatteringType.LIGHT,
                    NuclearConstant.DEUTERIUM, FluidVariant.of(MIFluids.TRITIUM), true);
        }
        return null;
    }

}

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
import net.minecraft.fluid.Fluids;

public interface INuclearComponent {

    double getHeatConduction();

    INeutronBehaviour getNeutronBehaviour();

    static INuclearComponent of(double heatConduction, double density, NuclearConstant.ScatteringType type, NuclearConstant.IsotopeParams params) {
        return new INuclearComponent() {
            @Override
            public double getHeatConduction() {
                return heatConduction * density;
            }

            @Override
            public INeutronBehaviour getNeutronBehaviour() {
                return INeutronBehaviour.of(type, params, density);
            }
        };
    }

    public static INuclearComponent of(FluidVariant fluid) {
        if (fluid == FluidVariant.of(Fluids.WATER)) {
            return INuclearComponent.of(NuclearConstant.BASE_HEAT_CONDUCTION * 5, 1, NuclearConstant.ScatteringType.ULTRA_LIGHT,
                    NuclearConstant.HYDROGEN);
        } else if (fluid == FluidVariant.of(MIFluids.HEAVY_WATER)) {
            return INuclearComponent.of(NuclearConstant.BASE_HEAT_CONDUCTION * 6, 1, NuclearConstant.ScatteringType.LIGHT, NuclearConstant.DEUTERIUM);
        } else if (fluid == FluidVariant.of(MIFluids.HIGH_PRESSURE_WATER)) {
            return INuclearComponent.of(NuclearConstant.BASE_HEAT_CONDUCTION * 5, 2, NuclearConstant.ScatteringType.ULTRA_LIGHT,
                    NuclearConstant.HYDROGEN);
        } else if (fluid == FluidVariant.of(MIFluids.HIGH_PRESSURE_HEAVY_WATER)) {
            return INuclearComponent.of(NuclearConstant.BASE_HEAT_CONDUCTION * 6, 2, NuclearConstant.ScatteringType.LIGHT, NuclearConstant.DEUTERIUM);
        }

        return null;
    }

}

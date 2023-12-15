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
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import java.util.*;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class FluidNuclearComponents {

    public static HashMap<Fluid, INuclearComponent<FluidVariant>> NUCLEAR_FLUIDS = new HashMap<>();

    private static final INuclearComponent<FluidVariant> WATER = create(Fluids.WATER,
            NuclearConstant.BASE_HEAT_CONDUCTION * 5, 1,
            NuclearConstant.ScatteringType.ULTRA_LIGHT, NuclearConstant.HYDROGEN,
            MIFluids.DEUTERIUM.variant(), false);
    private static final INuclearComponent<FluidVariant> HEAVY_WATER = create(MIFluids.HEAVY_WATER.asFluid(),
            NuclearConstant.BASE_HEAT_CONDUCTION * 6, 1,
            NuclearConstant.ScatteringType.LIGHT, NuclearConstant.DEUTERIUM,
            MIFluids.TRITIUM.variant(), false);
    private static final INuclearComponent<FluidVariant> HIGH_PRESSURE_WATER = create(MIFluids.HIGH_PRESSURE_WATER.asFluid(),
            NuclearConstant.BASE_HEAT_CONDUCTION * 5, 4,
            NuclearConstant.ScatteringType.ULTRA_LIGHT, NuclearConstant.HYDROGEN,
            MIFluids.DEUTERIUM.variant(), true);
    private static final INuclearComponent<FluidVariant> HIGH_PRESSURE_HEAVY_WATER = create(MIFluids.HIGH_PRESSURE_HEAVY_WATER.asFluid(),
            NuclearConstant.BASE_HEAT_CONDUCTION * 6, 4,
            NuclearConstant.ScatteringType.LIGHT, NuclearConstant.DEUTERIUM,
            MIFluids.TRITIUM.variant(), true);

    @Nullable
    public static INuclearComponent<FluidVariant> of(FluidVariant variant) {
        Fluid fluid = variant.getFluid();

        if (NUCLEAR_FLUIDS.containsKey(fluid)) {
            return NUCLEAR_FLUIDS.get(fluid);
        }
        return null;
    }

    public static INuclearComponent<FluidVariant> create(Fluid fluid, double heatConduction, double density, NuclearConstant.ScatteringType type,
            NuclearConstant.IsotopeParams params, FluidVariant neutronProduct, boolean highPressure) {
        FluidVariant variant = FluidVariant.of(fluid);

        if (NUCLEAR_FLUIDS.containsKey(fluid)) {
            throw new IllegalArgumentException("May not re-register fluid nuclear components");
        } else {
            return NUCLEAR_FLUIDS.put(fluid, new INuclearComponent<>() {
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

            });
        }
    }

    static {
        KubeJSProxy.instance.fireRegisterFluidNuclearComponentsEvent();
    }
}

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
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public record FluidNuclearComponent(
        Fluid fluid,
        double heatConduction,
        NeutronBehaviour neutronBehaviour,
        Fluid neutronProduct,
        int neutronProductAmount,
        double neutronProductProbability) implements NuclearComponent<FluidResource> {
    public FluidNuclearComponent(
            Fluid fluid,
            double heatConduction,
            double density,
            NuclearConstant.ScatteringType type,
            IsotopeParams params,
            Fluid neutronProduct,
            int neutronProductAmount,
            double neutronProductProbability) {
        this(
                fluid,
                heatConduction * density,
                NeutronBehaviour.of(type, params, density),
                neutronProduct,
                neutronProductAmount,
                neutronProductProbability);
    }

    @Override
    public FluidResource getVariant() {
        return FluidResource.of(fluid);
    }

    @Override
    public double getHeatConduction() {
        return heatConduction;
    }

    @Override
    public NeutronBehaviour getNeutronBehaviour() {
        return neutronBehaviour;
    }

    @Override
    public FluidResource getNeutronProduct() {
        return FluidResource.of(neutronProduct);
    }

    @Override
    public int getNeutronProductAmount() {
        return neutronProductAmount;
    }

    @Override
    public double getNeutronProductProbability() {
        return neutronProductProbability;
    }

    private static final Map<Fluid, FluidNuclearComponent> registry = new IdentityHashMap<>();

    public static void register(FluidNuclearComponent component) {
        Fluid fluid = component.fluid();
        if (registry.containsKey(fluid)) {
            throw new IllegalArgumentException("Already registered fluid-neutron interaction for " + fluid);
        }

        registry.put(fluid, component);
    }

    public static void remove(Fluid fluid) {
        registry.remove(fluid);
    }

    @Nullable
    public static FluidNuclearComponent get(Fluid fluid) {
        return registry.get(fluid);
    }

    public static void init() {
        register(new FluidNuclearComponent(Fluids.WATER,
                NuclearConstant.BASE_HEAT_CONDUCTION * 5,
                1,
                NuclearConstant.ScatteringType.ULTRA_LIGHT,
                NuclearConstant.HYDROGEN,
                MIFluids.DEUTERIUM.asFluid(),
                1,
                1));
        register(new FluidNuclearComponent(MIFluids.HEAVY_WATER.asFluid(),
                NuclearConstant.BASE_HEAT_CONDUCTION * 6,
                1,
                NuclearConstant.ScatteringType.LIGHT,
                NuclearConstant.DEUTERIUM,
                MIFluids.TRITIUM.asFluid(),
                1,
                1));
        register(new FluidNuclearComponent(MIFluids.HIGH_PRESSURE_WATER.asFluid(),
                NuclearConstant.BASE_HEAT_CONDUCTION * 5,
                4,
                NuclearConstant.ScatteringType.ULTRA_LIGHT,
                NuclearConstant.HYDROGEN,
                MIFluids.DEUTERIUM.asFluid(),
                8,
                0.125));
        register(new FluidNuclearComponent(MIFluids.HIGH_PRESSURE_HEAVY_WATER.asFluid(),
                NuclearConstant.BASE_HEAT_CONDUCTION * 6,
                4,
                NuclearConstant.ScatteringType.LIGHT,
                NuclearConstant.DEUTERIUM,
                MIFluids.TRITIUM.asFluid(),
                8,
                0.125));

        KubeJSProxy.instance.fireRegisterFluidNeutronInteractionsEvent();
    }
}

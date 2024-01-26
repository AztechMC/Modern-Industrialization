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
package aztech.modern_industrialization.compat.kubejs.registration;

import aztech.modern_industrialization.nuclear.FluidNuclearComponent;
import aztech.modern_industrialization.nuclear.IsotopeParams;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import dev.latvian.mods.kubejs.event.EventJS;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

public class RegisterFluidNeutronInteractionsEventJS extends EventJS {

    public void register(
            Fluid fluid,
            double heatConduction,
            double density,
            String scatteringString,
            // Isotope params
            double thermalAbsorption,
            double fastAbsorption,
            double thermalScattering,
            double fastScattering,
            // Products
            Fluid productFluid,
            long productPerNeutron,
            double productProbability) {

        var scatteringType = switch (scatteringString) {
        case "ultra_light" -> NuclearConstant.ScatteringType.ULTRA_LIGHT;
        case "light" -> NuclearConstant.ScatteringType.LIGHT;
        case "medium" -> NuclearConstant.ScatteringType.MEDIUM;
        case "heavy" -> NuclearConstant.ScatteringType.HEAVY;
        default -> throw new IllegalArgumentException("Invalid ScatteringType: " + scatteringString);
        };

        FluidNuclearComponent.register(
                new FluidNuclearComponent(
                        fluid,
                        heatConduction,
                        density,
                        scatteringType,
                        new IsotopeParams(thermalAbsorption, fastAbsorption, thermalScattering, fastScattering),
                        FluidVariant.of(productFluid),
                        productPerNeutron,
                        productProbability));
    }

    public void remove(Fluid fluid) {
        if (FluidNuclearComponent.get(fluid) == null) {
            throw new IllegalArgumentException("Fluid " + BuiltInRegistries.FLUID.getKey(fluid) + " is not a nuclear component!");
        }

        FluidNuclearComponent.remove(fluid);
    }

    public void modify(
            Fluid fluid,
            double heatConduction,
            double density,
            String scatteringString,
            // Isotope params
            double thermalAbsorption,
            double fastAbsorption,
            double thermalScattering,
            double fastScattering,
            // Products
            Fluid productFluid,
            long productPerNeutron,
            double productProbability) {

        remove(fluid);
        register(fluid, heatConduction, density, scatteringString, thermalAbsorption, fastAbsorption, thermalScattering, fastScattering, productFluid,
                productPerNeutron, productProbability);
    }
}

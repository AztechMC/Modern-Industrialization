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

import aztech.modern_industrialization.nuclear.FluidNuclearComponents;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import dev.latvian.mods.kubejs.event.EventJS;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class RegisterFluidNuclearComponentsEventJS extends EventJS {

    public void register(String fluidString, double heatConduction, double density, String scatteringString, String paramsString,
            String productString, boolean highPressure) {

        var fluid = Registry.FLUID.get(ResourceLocation.tryParse(fluidString));
        var neutronProduct = FluidVariant.of(Registry.FLUID.get(ResourceLocation.tryParse(productString)));
        var params = RegisterNuclearParams.of(paramsString);

        var scatteringType = switch (scatteringString) {
        case "ULTRA_LIGHT" -> NuclearConstant.ScatteringType.ULTRA_LIGHT;
        case "LIGHT" -> NuclearConstant.ScatteringType.LIGHT;
        case "MEDIUM" -> NuclearConstant.ScatteringType.MEDIUM;
        case "HEAVY" -> NuclearConstant.ScatteringType.HEAVY;
        default -> throw new IllegalArgumentException("Invalid ScatteringType: " + scatteringString);
        };

        FluidNuclearComponents.create(fluid,
                NuclearConstant.BASE_HEAT_CONDUCTION * heatConduction, density,
                scatteringType, params,
                neutronProduct, highPressure);
    }

    public void remove(String fluidString) {
        var fluid = Registry.FLUID.get(ResourceLocation.tryParse(fluidString));

        if (!FluidNuclearComponents.NUCLEAR_FLUIDS.containsKey(fluid)) {
            throw new IllegalArgumentException("Fluid " + fluidString + " is not a nuclear component!");
        }

        FluidNuclearComponents.NUCLEAR_FLUIDS.remove(fluid);

    }

    public void modify(String fluidString, double heatConduction, double density, String scatteringString, String paramsString,
            String productString, boolean highPressure) {

        remove(fluidString);
        register(fluidString, heatConduction, density, scatteringString, paramsString, productString, highPressure);

    }

}

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

import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.property.MaterialProperty;

public class IsotopeFuelParams extends IsotopeParams {
    public final int maxTemp;
    public final double neutronsMultiplication;
    public final double directEnergyFactor;
    public final int tempLimitLow;
    public final int tempLimitHigh;

    public IsotopeFuelParams(double thermalAbsorbProba, double thermalScatterings, int maxTemp, int tempLimitLow, int tempLimitHigh,
            double neutronsMultiplication, double directEnergyFactor) {
        super(thermalAbsorbProba, INeutronBehaviour.reduceCrossProba(thermalAbsorbProba, 0.1), thermalScatterings,
                INeutronBehaviour.reduceCrossProba(thermalScatterings, 0.5));

        this.maxTemp = maxTemp;
        this.neutronsMultiplication = neutronsMultiplication;
        this.directEnergyFactor = directEnergyFactor;
        this.tempLimitLow = tempLimitLow;
        this.tempLimitHigh = tempLimitHigh;
    }

    public static IsotopeFuelParams of(Material material) {
        var params = material.get(MaterialProperty.ISOTOPE);
        if (params == null) {
            throw new IllegalArgumentException("Material %s must be a fuel isotope".formatted(material.name));
        }
        return params;
    }

    public static IsotopeFuelParams mix(Material a, Material b, double factor) {
        return mix(of(a), of(b), factor);
    }

    public static IsotopeFuelParams mix(IsotopeFuelParams a, IsotopeFuelParams b, double factor) {
        factor = 1 - factor;

        double newThermalAbsorptionProba = INeutronBehaviour.probaFromCrossSection(mix(a.thermalAbsorption, b.thermalAbsorption, factor));
        double newScatteringProba = INeutronBehaviour.probaFromCrossSection(mix(a.thermalScattering, b.thermalScattering, factor));
        double newNeutronMultiplicationFactor = mix(a.neutronsMultiplication, b.neutronsMultiplication, factor);

        double totalEnergy = mix(a.neutronsMultiplication * (1 + a.directEnergyFactor), b.neutronsMultiplication * (1 + b.directEnergyFactor),
                factor);

        int newMaxTemp = (int) mix(a.maxTemp, b.maxTemp, factor);
        int newTempLimitLow = (int) mix(a.tempLimitLow, b.tempLimitLow, factor);
        int newTempLimitHigh = (int) mix(a.tempLimitHigh, b.tempLimitHigh, factor);

        double newDirectEnergyFactor = totalEnergy / (newNeutronMultiplicationFactor) - 1;

        return new IsotopeFuelParams(newThermalAbsorptionProba, newScatteringProba, newMaxTemp, newTempLimitLow, newTempLimitHigh,
                newNeutronMultiplicationFactor, newDirectEnergyFactor);
    }

    private static double mix(double a, double b, double r) {
        return r * a + (1 - r) * b;
    }
}

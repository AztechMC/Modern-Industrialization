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

public class NuclearConstant {

    public static final int EU_FOR_FAST_NEUTRON = 8;
    public static final int DESINTEGRATION_BY_ROD = 10240000;
    public static final double BASE_HEAT_CONDUCTION = 0.01;
    public static final double BASE_NEUTRON = 0.1;
    public static final int MAX_TEMPERATURE = 3250;
    public static final int EU_PER_DEGREE = 64;
    public static final int MAX_HATCH_EU_PRODUCTION = 8192;

    public enum ScatteringType {
        ULTRA_LIGHT(0.05),
        LIGHT(0.2),
        MEDIUM(0.5),
        HEAVY(0.85);

        ScatteringType(double fastFraction) {
            this.fastFraction = fastFraction;
            this.slowFraction = 1 - this.fastFraction;
        }

        public final double fastFraction;
        public final double slowFraction;
    }

    public static class IsotopeParams {
        public final double thermalAbsorption;
        public final double fastAbsorption;
        public final double fastScattering;
        public final double thermalScattering;

        public IsotopeParams(double thermalAbsorbProba, double fastAbsorptionProba, double thermalScatteringProba, double fastScatteringProba) {
            this.thermalAbsorption = INeutronBehaviour.crossSectionFromProba(thermalAbsorbProba);
            this.fastAbsorption = INeutronBehaviour.crossSectionFromProba(fastAbsorptionProba);
            this.thermalScattering = INeutronBehaviour.crossSectionFromProba(thermalScatteringProba);
            this.fastScattering = INeutronBehaviour.crossSectionFromProba(fastScatteringProba);
        }
    }

    public static class IsotopeFuelParams extends IsotopeParams {

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

        public IsotopeFuelParams mix(IsotopeFuelParams b, double factor) {
            return IsotopeFuelParams.mix(this, b, factor);
        }

    }

    public static final IsotopeFuelParams U235 = new IsotopeFuelParams(0.6, 0.35, 2400, 900, 2300, 8, 0.5);
    public static final IsotopeFuelParams U238 = new IsotopeFuelParams(0.6, 0.30, 3200, 1000, 3000, 6, 0.3);
    public static final IsotopeFuelParams Pu239 = new IsotopeFuelParams(0.9, 0.25, 2100, 600, 2000, 9, 0.25);

    public static final IsotopeFuelParams U = U238.mix(U235, 1.0 / 81);

    public static final IsotopeFuelParams LEU = U238.mix(U235, 1.0 / 9);
    public static final IsotopeFuelParams HEU = U238.mix(U235, 1.0 / 3);

    public static final IsotopeFuelParams LE_MOX = U238.mix(Pu239, 1.0 / 9);
    public static final IsotopeFuelParams HE_MOX = U238.mix(Pu239, 1.0 / 3);

    public static final IsotopeParams HYDROGEN = new IsotopeParams(0.1, 0.05, 0.25, 0.75);
    public static final IsotopeParams DEUTERIUM = new IsotopeParams(0.02, 0.01, 0.15, 0.65);

    public static final IsotopeParams CADMIUM = new IsotopeParams(0.95, 0.9, 0.05, 0.1);
    public static final IsotopeParams CARBON = new IsotopeParams(0.01, 0.005, 0.5, 0.85);
}

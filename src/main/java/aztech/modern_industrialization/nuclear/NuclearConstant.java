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

    public static final int EU_FOR_FAST_NEUTRON = 2048;
    public static final int DESINTEGRATION_BY_ROD = 64000;
    public static final double BASE_HEAT_CONDUCTION = 0.01;
    public static final double BASE_NEUTRON = 0.1;
    public static final int MAX_TEMPERATURE = 3800;
    public static final int EU_PER_DEGREE = 128;

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

        public IsotopeFuelParams(double thermalAbsorbProba, double thermalScatterings, int maxTemp, double neutronsMultiplication,
                double directEnergyFactor) {

            super(thermalAbsorbProba, INeutronBehaviour.reduceCrossProba(thermalAbsorbProba, 0.2), thermalScatterings,
                    INeutronBehaviour.reduceCrossProba(thermalScatterings, 0.5));

            this.maxTemp = maxTemp;
            this.neutronsMultiplication = neutronsMultiplication;
            this.directEnergyFactor = directEnergyFactor;

        }

        public static IsotopeFuelParams mix(IsotopeFuelParams a, IsotopeFuelParams b, double factor) {

            factor = 1 - factor;

            double newThermalAbsorptionProba = INeutronBehaviour.probaFromCrossSection(mix(a.thermalAbsorption, b.thermalAbsorption, factor));
            double newScatteringProba = INeutronBehaviour.probaFromCrossSection(mix(a.thermalScattering, b.thermalScattering, factor));
            double newNeutronMultiplicationFactor = mix(a.neutronsMultiplication, b.neutronsMultiplication, factor);

            double totalEnergy = mix(a.neutronsMultiplication * (1 + a.directEnergyFactor), b.neutronsMultiplication * (1 + b.directEnergyFactor),
                    factor);

            int newMaxTemp = (int) mix(a.maxTemp, b.maxTemp, factor);

            double newDirectEnergyFactor = totalEnergy / (newNeutronMultiplicationFactor) - 1;

            return new IsotopeFuelParams(newThermalAbsorptionProba, newScatteringProba, newMaxTemp, newNeutronMultiplicationFactor,
                    newDirectEnergyFactor);

        }

        private static double mix(double a, double b, double r) {
            return r * a + (1 - r) * b;
        }

        public IsotopeFuelParams mix(IsotopeFuelParams b, double factor) {
            return IsotopeFuelParams.mix(this, b, factor);
        }

    }

    public static final IsotopeFuelParams U235 = new IsotopeFuelParams(0.5, 0.35, 2200, 6, 0.5);
    public static final IsotopeFuelParams U238 = new IsotopeFuelParams(0.15, 0.30, 2800, 4, 0.3);
    public static final IsotopeFuelParams Pu239 = new IsotopeFuelParams(0.9, 0.25, 1700, 8, 0.25);

    public static final IsotopeFuelParams U = U238.mix(U235, 1.0 / 81);

    public static final IsotopeFuelParams LEU = U238.mix(U235, 1.0 / 9);
    public static final IsotopeFuelParams HEU = U238.mix(U235, 1.0 / 3);

    public static final IsotopeFuelParams LE_MOX = U238.mix(Pu239, 1.0 / 9);
    public static final IsotopeFuelParams HE_MOX = U238.mix(Pu239, 1.0 / 3);

    public static final IsotopeParams HYDROGEN = new IsotopeParams(0.3, 0.15, 0.5, 0.2);
    public static final IsotopeParams DEUTERIUM = new IsotopeParams(0.02, 0.01, 0.25, 0.15);

}

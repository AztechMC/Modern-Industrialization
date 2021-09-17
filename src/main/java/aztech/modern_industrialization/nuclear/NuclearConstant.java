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
    public static final int DESINTEGRATION_BY_ROD = 256000;
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

    public enum ScatteringProba {
        ZERO(0),
        ULTRA_LOW(0.1),
        LOW(0.25),
        MEDIUM_LOW(0.35),
        MEDIUM(0.5),
        MEDIUM_HIGH(0.65),
        HIGH(0.75),
        VERY_HIGH(0.85),
        ULTRA_HIGH(0.95);

        ScatteringProba(double probability) {
            this.probability = probability;
        }

        public final double probability;
    }

    public static class IsotopeParams {
        public final double thermalAbsorption;
        public final double fastAbsorption;
        public final double scattering;
        public final int maxTemp;
        public final double neutronsMultiplication;
        public final double directEnergyFactor;

        public IsotopeParams(double thermalAbsorbProba, double fastAbsorbProba, double scatteringProba, int maxTemp, double neutronsMultiplication,
                double directEnergyFactor) {

            this.maxTemp = maxTemp;
            this.neutronsMultiplication = neutronsMultiplication;
            this.directEnergyFactor = directEnergyFactor;
            this.thermalAbsorption = INeutronBehaviour.crossSectionFromProba(thermalAbsorbProba);
            this.fastAbsorption = INeutronBehaviour.crossSectionFromProba(fastAbsorbProba);
            this.scattering = INeutronBehaviour.crossSectionFromProba(scatteringProba);
        }

        public static IsotopeParams mix(IsotopeParams a, IsotopeParams b, double factor) {

            factor = 1 - factor;

            double newThermalAbsorptionProba = INeutronBehaviour.probaFromCrossSection(mix(a.thermalAbsorption, b.thermalAbsorption, factor));
            double newFastAbsorptionProba = INeutronBehaviour.probaFromCrossSection(mix(a.fastAbsorption, b.fastAbsorption, factor));
            double newScatteringProba = INeutronBehaviour.probaFromCrossSection(mix(a.scattering, b.scattering, factor));

            double newNeutronMultiplicationFactor = mix(a.neutronsMultiplication, b.neutronsMultiplication, factor);

            double totalEnergy = mix(a.neutronsMultiplication * (1 + a.directEnergyFactor), b.neutronsMultiplication * (1 + b.directEnergyFactor),
                    factor);

            int newMaxTemp = (int) mix(a.maxTemp, b.maxTemp, factor);

            double newDirectEnergyFactor = totalEnergy / (newNeutronMultiplicationFactor) - 1;

            return new IsotopeParams(newThermalAbsorptionProba, newFastAbsorptionProba, newScatteringProba, newMaxTemp,
                    newNeutronMultiplicationFactor, newDirectEnergyFactor);

        }

        private static double mix(double a, double b, double r) {
            return r * a + (1 - r) * b;
        }

        public IsotopeParams mix(IsotopeParams b, double factor) {
            return IsotopeParams.mix(this, b, factor);
        }

    }

    public static final IsotopeParams U235 = new IsotopeParams(0.5, 0.1, 0.35, 2200, 6, 0.5);
    public static final IsotopeParams U238 = new IsotopeParams(0, 0.03, 0.30, 2800, 4.5, 0.3);
    public static final IsotopeParams Pu239 = new IsotopeParams(0.8, 0.2, 0.25, 1700, 8, 0.25);

    public static final IsotopeParams U = U238.mix(U235, 1.0 / 81);

    public static final IsotopeParams LEU = U238.mix(U235, 1.0 / 9);
    public static final IsotopeParams HEU = U238.mix(U235, 1.0 / 3);

    public static final IsotopeParams LE_MOX = U238.mix(Pu239, 1.0 / 9);
    public static final IsotopeParams HE_MOX = U238.mix(Pu239, 1.0 / 3);
}

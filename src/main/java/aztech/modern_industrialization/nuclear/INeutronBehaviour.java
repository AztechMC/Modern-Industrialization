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

import com.google.common.base.Preconditions;

public interface INeutronBehaviour {
    double neutronSlowingProbability();

    double interactionTotalProbability(NeutronType type);

    double interactionRelativeProbability(NeutronType type, NeutronInteraction interaction);

    static INeutronBehaviour of(NuclearConstant.ScatteringType scatteringType, double thermalNeutronAbsorptionBarn, double fastNeutronAbsorptionBarn,
            double thermalNeutronScatteringBarn, double fastNeutronScatteringBarn, double size) {
        return new INeutronBehaviour() {
            final double thermalProbability = probaFromCrossSection((thermalNeutronAbsorptionBarn + thermalNeutronScatteringBarn) * Math.sqrt(size));
            final double fastProbability = probaFromCrossSection((fastNeutronAbsorptionBarn + fastNeutronScatteringBarn) * Math.sqrt(size));

            @Override
            public double neutronSlowingProbability() {
                return scatteringType.slowFraction;
            }

            @Override
            public double interactionTotalProbability(NeutronType type) {
                Preconditions.checkArgument(type != NeutronType.BOTH);
                if (type == NeutronType.FAST) {
                    return fastProbability;
                } else if (type == NeutronType.THERMAL) {
                    return thermalProbability;
                }
                return 0;
            }

            @Override
            public double interactionRelativeProbability(NeutronType type, NeutronInteraction interaction) {
                Preconditions.checkArgument(type != NeutronType.BOTH);
                if (type == NeutronType.THERMAL) {
                    if (interaction == NeutronInteraction.SCATTERING) {
                        return thermalNeutronScatteringBarn / (thermalNeutronAbsorptionBarn + thermalNeutronScatteringBarn);
                    } else if (interaction == NeutronInteraction.ABSORPTION) {
                        return thermalNeutronAbsorptionBarn / (thermalNeutronAbsorptionBarn + thermalNeutronScatteringBarn);
                    }
                } else if (type == NeutronType.FAST) {
                    if (interaction == NeutronInteraction.SCATTERING) {
                        return fastNeutronScatteringBarn / (fastNeutronAbsorptionBarn + fastNeutronScatteringBarn);
                    } else if (interaction == NeutronInteraction.ABSORPTION) {
                        return fastNeutronAbsorptionBarn / (fastNeutronAbsorptionBarn + fastNeutronScatteringBarn);
                    }
                }
                return 0;
            }
        };
    }

    static double crossSectionFromProba(double proba) {
        return -Math.log(1 - proba);
    }

    static double probaFromCrossSection(double crossSection) {
        return 1 - Math.exp(-crossSection);
    }

    static INeutronBehaviour of(NuclearConstant.ScatteringType scatteringType, IsotopeParams params, double size) {
        return of(scatteringType, params.thermalAbsorption, params.fastAbsorption, params.thermalScattering, params.fastScattering, size);
    }

    static double reduceCrossProba(double proba, double crossSectionFactor) {
        return probaFromCrossSection(crossSectionFromProba(proba) * crossSectionFactor);
    }

    INeutronBehaviour NO_INTERACTION = new INeutronBehaviour() {
        @Override
        public double neutronSlowingProbability() {
            return 0.5;
        }

        @Override
        public double interactionTotalProbability(NeutronType type) {
            return 0;
        }

        @Override
        public double interactionRelativeProbability(NeutronType type, NeutronInteraction interaction) {
            return 0.5;
        }
    };
}

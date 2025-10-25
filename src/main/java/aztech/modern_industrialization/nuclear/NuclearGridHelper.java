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

import static aztech.modern_industrialization.nuclear.NeutronFate.*;

import aztech.modern_industrialization.machines.components.NuclearEfficiencyHistoryComponent;
import java.util.Optional;
import java.util.Random;
import org.jetbrains.annotations.Nullable;

public class NuclearGridHelper {
    private static final int[] dX = { 1, 0, -1, 0 };
    private static final int[] dY = { 0, 1, 0, -1 };

    private static final Random rand = new Random();

    private static final int MAX_SPLIT = 30;

    public static boolean simulate(NuclearGrid grid, NuclearEfficiencyHistoryComponent efficiencyHistory) {
        int sizeX = grid.getSizeX();
        int sizeY = grid.getSizeY();

        boolean hasFuel = false;

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {

                final int x = i;
                final int y = j;

                @Nullable
                INuclearTile tile = grid.getNuclearTile(i, j);
                if (tile == null) {
                    continue;
                }

                // Get fuel before the generation tick, because the fuel might be consumed!
                Optional<NuclearFuel> maybeFuel = tile.getFuel();
                int neutronNumberPrime = tile.neutronGenerationTick(efficiencyHistory);
                if (neutronNumberPrime == 0) {
                    continue;
                }

                hasFuel = true;
                NuclearFuel fuel = maybeFuel.orElseThrow(() -> new IllegalStateException("Neutron generated without fuel"));

                tile.putHeat(neutronNumberPrime * fuel.directEUbyDesintegration / fuel.neutronMultiplicationFactor);

                int split = Math.min(neutronNumberPrime, MAX_SPLIT);
                int neutronNumberPerSplit = neutronNumberPrime / split;

                for (int k = 0; k < split + 1; k++) {

                    int neutronNumber = (k < split) ? neutronNumberPerSplit : neutronNumberPrime % split;

                    if (neutronNumber > 0) {
                        NeutronType type = NeutronType.FAST;
                        grid.registerNeutronCreation(neutronNumber, type);

                        int dir = rand.nextInt(4);
                        int posX = x;
                        int posY = y;

                        // Loop until we exit the grid
                        while (true) {
                            @Nullable
                            INuclearTile secondTile = grid.getNuclearTile(posX, posY);

                            if (secondTile == null) {
                                grid.registerNeutronFate(neutronNumber, type, ESCAPE);
                                break;
                            }

                            secondTile.addNeutronsToFlux(neutronNumber, type);

                            @Nullable
                            INuclearComponent<?> component = secondTile.getComponent();
                            if (component != null) {
                                double interactionProba = component.getNeutronBehaviour().interactionTotalProbability(type);

                                if (rand.nextDouble() < interactionProba) {

                                    double interactionSelector = rand.nextDouble();

                                    double probaAbsorption = component.getNeutronBehaviour().interactionRelativeProbability(type,
                                            NeutronInteraction.ABSORPTION);

                                    if (interactionSelector <= probaAbsorption) {
                                        secondTile.absorbNeutrons(neutronNumber, type);

                                        if (type == NeutronType.FAST) {
                                            secondTile.putHeat(neutronNumber * NuclearConstant.EU_FOR_FAST_NEUTRON);
                                        }

                                        if (secondTile.getFuel().isPresent()) {
                                            grid.registerNeutronFate(neutronNumber, type, ABSORBED_IN_FUEL);
                                        } else {
                                            grid.registerNeutronFate(neutronNumber, type, ABSORBED_NOT_IN_FUEL);
                                        }

                                        break;
                                    } else {
                                        dir = rand.nextInt(4);

                                        if (type == NeutronType.FAST
                                                && rand.nextDouble() < component.getNeutronBehaviour().neutronSlowingProbability()) {
                                            type = NeutronType.THERMAL;
                                            secondTile.putHeat(neutronNumber * NuclearConstant.EU_FOR_FAST_NEUTRON);
                                        }
                                    }
                                }
                            }

                            posX += dX[dir];
                            posY += dY[dir];
                        }
                    }
                }
            }
        }

        // HEAT

        // Cache heat transfer coefficients
        double heatTransferCoeff[][] = new double[grid.getSizeX()][grid.getSizeY()];
        for (int i = 0; i < sizeX; ++i) {
            for (int j = 0; j < sizeY; ++j) {
                @Nullable
                INuclearTile tile = grid.getNuclearTile(i, j);
                if (tile != null) {
                    heatTransferCoeff[i][j] = tile.getHeatTransferCoeff();
                }
            }
        }

        final int NUMERICAL_SUBSTEP = 10;

        for (int substep = 0; substep < NUMERICAL_SUBSTEP; substep++) {
            double[][] temperatureOut = new double[sizeX][sizeY];
            double[][] temperatureDelta = new double[sizeX][sizeY];

            for (int step = 0; step < 3; step++) {
                // step 0: compute temperatureOut = dT * coeff
                // step 1: compute temperatureDelta, clamping as necessary
                // step 2: set temperature
                for (int i = 0; i < sizeX; i++) {
                    for (int j = 0; j < sizeY; j++) {
                        @Nullable
                        INuclearTile tile = grid.getNuclearTile(i, j);
                        if (tile == null) {
                            continue;
                        }

                        double temperatureA = tile.getTemperature();
                        if (step == 2) {
                            tile.setTemperature(temperatureA + temperatureDelta[i][j]);
                        } else {
                            double coeffA = heatTransferCoeff[i][j];

                            if (step == 1) {
                                // clamp to avoid reaching < 0 temperatures
                                temperatureDelta[i][j] -= Math.min(temperatureA, temperatureOut[i][j]);
                            }
                            for (int k = 0; k < 4; k++) {
                                int i2 = i + dX[k];
                                int j2 = j + dY[k];

                                @Nullable
                                INuclearTile secondTile = grid.getNuclearTile(i2, j2);

                                if (secondTile != null) {
                                    double temperatureB = secondTile.getTemperature();
                                    double coeffB = heatTransferCoeff[i2][j2];
                                    double coeffTransfer = 0.5 * (coeffA + coeffB) / NUMERICAL_SUBSTEP;
                                    if (temperatureA > temperatureB) {
                                        if (step == 0) {
                                            temperatureOut[i][j] += (temperatureA - temperatureB) * coeffTransfer;
                                        } else {
                                            double frac = Math.min(1, temperatureA / temperatureOut[i][j]);
                                            temperatureDelta[i2][j2] += frac * (temperatureA - temperatureB) * coeffTransfer;
                                        }
                                    }
                                } else {
                                    double temperatureB = 0;
                                    double coeffTransfer = 0.5 * coeffA / NUMERICAL_SUBSTEP;
                                    if (step == 0) {
                                        temperatureOut[i][j] += (temperatureA - temperatureB) * coeffTransfer;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                @Nullable
                INuclearTile maybeTile = grid.getNuclearTile(i, j);

                if (maybeTile != null) {
                    maybeTile.nuclearTick(efficiencyHistory);
                }
            }
        }

        return hasFuel;
    }
}

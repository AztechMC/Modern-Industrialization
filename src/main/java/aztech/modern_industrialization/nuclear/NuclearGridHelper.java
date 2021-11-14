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

import java.util.Optional;
import java.util.Random;

public class NuclearGridHelper {

    private static final int[] dX = { 1, 0, -1, 0 };
    private static final int[] dY = { 0, 1, 0, -1 };

    private static final Random rand = new Random();

    private static final int MAX_STEP = 100;
    private static final int MAX_SPLIT = 30;

    public static boolean simulate(INuclearGrid grid) {

        int sizeX = grid.getSizeX();
        int sizeY = grid.getSizeY();

        boolean hasFuel = false;

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {

                final int x = i;
                final int y = j;

                Optional<INuclearTile> maybeTile = grid.getNuclearTile(i, j);

                if (maybeTile.isPresent()) {
                    INuclearTile tile = maybeTile.get();
                    Optional<NuclearFuel> maybeFuel = tile.getFuel();
                    int neutronNumberPrime = tile.neutronGenerationTick(grid);

                    if (neutronNumberPrime > 0) {
                        if (maybeFuel.isEmpty()) {
                            throw new IllegalStateException("Neutron generated without fuel");
                        }
                        hasFuel = true;
                        NuclearFuel fuel = maybeFuel.get();

                        tile.putHeat(neutronNumberPrime * fuel.directEUbyDesintegration / fuel.neutronMultiplicationFactor);

                        int split = Math.min(neutronNumberPrime, MAX_SPLIT);
                        int neutronNumberPerSplit = neutronNumberPrime / split;

                        for (int k = 0; k < split + 1; k++) {

                            int neutronNumber = (k < split) ? neutronNumberPerSplit : neutronNumberPrime % split;

                            if (neutronNumber > 0) {
                                NeutronType type = NeutronType.FAST;
                                grid.registerNeutronCreation(neutronNumber, type);

                                int dir = rand.nextInt(4);
                                int step = 0;
                                int posX = x;
                                int posY = y;

                                while (step < MAX_STEP) {
                                    step++;

                                    Optional<INuclearTile> maybeSecondTile = grid.getNuclearTile(posX, posY);

                                    if (maybeSecondTile.isPresent()) {

                                        INuclearTile secondTile = maybeSecondTile.get();
                                        secondTile.addNeutronsToFlux(neutronNumber, type);

                                        if (secondTile.getComponent().isPresent()) {
                                            INuclearComponent component = secondTile.getComponent().get();
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

                                    } else {
                                        grid.registerNeutronFate(neutronNumber, type, ESCAPE);
                                        break;
                                    }

                                    posX += dX[dir];
                                    posY += dY[dir];
                                }
                            }
                        }

                    }
                }

            }

        }

        // HEAT
        double[][] temperatureOut = new double[sizeX][sizeY];
        double[][] temperatureDelta = new double[sizeX][sizeY];

        for (int step = 0; step < 3; step++) {
            // step 0: compute temperatureOut = dT * coeff
            // step 1: compute temperatureDelta, clamping as necessary
            // step 2: set temperature
            for (int i = 0; i < sizeX; i++) {
                for (int j = 0; j < sizeY; j++) {

                    Optional<INuclearTile> maybeTile = grid.getNuclearTile(i, j);
                    if (maybeTile.isPresent()) {

                        INuclearTile tile = maybeTile.get();
                        double temperatureA = tile.getTemperature();
                        if (step == 2) {
                            tile.setTemperature(temperatureA + temperatureDelta[i][j]);
                        } else {
                            if (step == 1) {
                                // clamp to avoid reaching < 0 temperatures
                                temperatureDelta[i][j] -= Math.min(temperatureA, temperatureOut[i][j]);
                            }
                            for (int k = 0; k < 4; k++) {
                                int i2 = i + dX[k];
                                int j2 = j + dY[k];

                                Optional<INuclearTile> maybeSecondTile = grid.getNuclearTile(i2, j2);

                                if (maybeSecondTile.isPresent()) {
                                    INuclearTile secondTile = maybeSecondTile.get();
                                    double temperatureB = secondTile.getTemperature();
                                    double coeffTransfer = 0.5 * (tile.getHeatTransferCoeff() + secondTile.getHeatTransferCoeff());
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
                                    double coeffTransfer = 0.5 * (tile.getHeatTransferCoeff());
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
                Optional<INuclearTile> maybeTile = grid.getNuclearTile(i, j);
                if (maybeTile.isPresent()) {
                    INuclearTile tile = maybeTile.get();
                    tile.nuclearTick(grid);
                }
            }
        }

        return hasFuel;

    }

}

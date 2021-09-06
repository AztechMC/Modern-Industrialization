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

import java.util.Random;

public class NuclearGridHelper {

    private static final int[] dX = { 1, 0, -1, 0 };
    private static final int[] dY = { 0, 1, 0, -1 };

    private static final Random rand = new Random();

    private static final int MAX_STEP = 100;

    public static void simulate(INuclearGrid grid) {

        int sizeX = grid.getSizeX();
        int sizeY = grid.getSizeY();

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                if (grid.ok(i, j) & grid.isFuel(i, j)) {

                    int neutronNumber = grid.neutronProducedFromSimulation(i, j);

                    grid.putHeat(i, j, neutronNumber * grid.getSupplementaryHeatByNeutronGenerated(i, j));

                    if (neutronNumber > 0) {

                        NeutronType type = NeutronType.FAST;
                        grid.registerNeutronCreation(neutronNumber, type);

                        int dir = rand.nextInt(4);
                        int step = 0;
                        int posX = i;
                        int posY = j;

                        while (step < MAX_STEP) {
                            step++;
                            posX += dX[dir];
                            posY += dY[dir];
                            if (grid.ok(posX, posY)) {

                                double interactionProba = grid.interactionTotalProbability(posX, posY, type);

                                if (rand.nextDouble() < interactionProba) {

                                    double interactionSelector = rand.nextDouble();

                                    double probaAbsorption = grid.interactionRelativeProbability(posX, posY, type, NeutronInteraction.ABSORPTION);
                                    double probaScattering = grid.interactionRelativeProbability(posX, posY, type, NeutronInteraction.SCATTERING);

                                    if (interactionSelector <= probaAbsorption) {
                                        grid.absorbNeutrons(posX, posY, type, neutronNumber);

                                        if (type == NeutronType.FAST) {
                                            grid.putHeat(posX, posY, neutronNumber * NuclearConstant.EU_FOR_FAST_NEUTRON);
                                        }
                                        if (grid.isFuel(posX, posY)) {
                                            grid.registerNeutronFate(neutronNumber, type, ABSORBED_IN_FUEL);
                                        } else {
                                            grid.registerNeutronFate(neutronNumber, type, ABSORBED_NOT_IN_FUEL);
                                        }
                                        break;
                                    } else {
                                        int newDir = rand.nextInt(4);
                                        dir = newDir;

                                        if (rand.nextDouble() < grid.neutronSlowingProbability(posX, posY)) {
                                            type = NeutronType.THERMAL;
                                            grid.putHeat(posX, posY, neutronNumber * NuclearConstant.EU_FOR_FAST_NEUTRON);
                                        }
                                    }
                                }

                            } else {
                                grid.registerNeutronFate(neutronNumber, type, ESCAPE);
                                break;
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
                    if (grid.ok(i, j)) {
                        double temperatureA = grid.getTemperature(i, j);
                        if (step == 2) {
                            grid.setTemperature(i, j, temperatureA + temperatureDelta[i][j]);
                        } else {
                            if (step == 1) {
                                // clamp to avoid reaching < 0 temperatures
                                temperatureDelta[i][j] -= Math.min(temperatureA, temperatureOut[i][j]);
                            }
                            for (int k = 0; k < 4; k++) {
                                int i2 = i + dX[k];
                                int j2 = j + dY[k];
                                if (grid.ok(i2, j2)) {
                                    double temperatureB = grid.getTemperature(i2, j2);
                                    double coeffTransfer = 0.5 * (grid.getHeatTransferCoeff(i, j) + grid.getHeatTransferCoeff(i2, j2));
                                    if (temperatureA > temperatureB) {
                                        if (step == 0) {
                                            temperatureOut[i][j] += (temperatureA - temperatureB) * coeffTransfer;
                                        } else {
                                            double frac = Math.min(1, temperatureA / temperatureOut[i][j]);
                                            temperatureDelta[i2][j2] += frac * (temperatureA - temperatureB) * coeffTransfer;
                                        }
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
                if (grid.ok(i, j)) {
                    grid.nuclearTick(i, j);
                }

            }
        }

    }

}

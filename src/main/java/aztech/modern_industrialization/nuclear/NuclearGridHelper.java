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

import java.util.Random;

public class NuclearGridHelper {

    private static final int[] dX = { 1, 0, -1, 0 };
    private static final int[] dY = { 0, 1, 0, -1 };

    private static final Random rand = new Random();

    private static final boolean ok(int x, int y, int sizeX, int sizeY) {
        return x >= 0 && y >= 0 && x < sizeX && y < sizeY;
    }

    private static final int doubleToInt(double d) {
        int floor = (int) Math.floor(d);
        return 1 + rand.nextDouble() < (d - floor) ? 1 : 0;
    }

    public static final void simulateNuclearTick(INuclearGrid nuclearGrid) {

        int sizeX = nuclearGrid.getSizeX();
        int sizeY = nuclearGrid.getSizeY();

        double[][] neutronsReceived = new double[sizeX][sizeY];

        // NEUTRONS

        for (int step = 0; step < 3; step++) {
            double[][] neutronsReceivedNew = new double[sizeX][sizeY];
            for (int i = 0; i < sizeX; i++) {
                for (int j = 0; j < sizeY; j++) {
                    if (nuclearGrid.isFuel(i, j)) {
                        double neutronProduced;
                        if (step == 0) {
                            neutronProduced = nuclearGrid.sendNeutron(i, j, 1);
                        } else {
                            neutronProduced = nuclearGrid.sendNeutron(i, j, doubleToInt(neutronsReceived[i][j]));
                        }
                        if (step < 2) {
                            for (int k = 0; k < 4; k++) {
                                int i2 = i + dX[k];
                                int j2 = j + dY[k];
                                if (ok(i2, j2, sizeX, sizeY)) {
                                    double neutronDiffused = nuclearGrid.getFracDiffusedNeutron(i2, j2);
                                    neutronsReceivedNew[i2][j2] += 0.25 * (1 - neutronDiffused) * neutronProduced;
                                    for (int l = 0; l < 4; l++) {
                                        int i3 = i2 + dX[l];
                                        int j3 = j2 + dY[l];
                                        if (ok(i3, j3, sizeX, sizeY)) {
                                            neutronsReceivedNew[i3][j3] += 0.25 * 0.25 * neutronDiffused * neutronProduced
                                                    * nuclearGrid.getNeutronDiffusionAnisotropy(i2, j2, k, l);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
            neutronsReceived = neutronsReceivedNew;
        }

        // HEAT
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {

                double temperatureA = nuclearGrid.getTemperature(i, j);

                for (int k = 0; k < 4; k++) {
                    int i2 = i + dX[k];
                    int j2 = j + dY[k];
                    if (ok(i2, j2, sizeX, sizeY)) {
                        double temperatureB = nuclearGrid.getTemperature(i2, j2);

                    }
                }

            }
        }

    }

}

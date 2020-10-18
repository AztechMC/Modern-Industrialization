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
import net.minecraft.item.ItemStack;

public class NuclearReactorLogic {

    public static final int[] DX = { 1, 0, -1, 0 };
    public static final int[] DY = { 0, -1, 0, 1 };

    public static boolean itemStackOk(ItemStack stack) {

        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof MINuclearItem) {
            MINuclearItem item = (MINuclearItem) stack.getItem();
            if (item.getDurability() == -1 || stack.getDamage() < item.getDurability()) {
                return true;
            }
        }
        return false;
    }

    public static boolean inGrid(int x, int y) {
        return x >= 0 && y >= 0 && x < 8 && y < 8;
    }

    public static void tick(ItemStack[][] grid, Random rand, NuclearReactorBlockEntity nuclearReactor) {

        double neutronReceived[][] = new double[8][8];
        double currentHeat[][] = new double[8][8];

        // neutrons production
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (itemStackOk(grid[i][j])) {
                    MINuclearItem item = (MINuclearItem) grid[i][j].getItem();
                    double neutronPulse = item.getNeutronPulse(grid[i][j]);
                    for (int d1 = 0; d1 < 4; d1++) {
                        int rx = i + DX[d1];
                        int ry = j + DY[d1];
                        if (inGrid(rx, ry) && itemStackOk(grid[rx][ry])) {
                            MINuclearItem item1 = (MINuclearItem) grid[rx][ry].getItem();
                            for (int d2 = 0; d2 < 4; d2++) {
                                int rx2 = rx + DX[d2];
                                int ry2 = ry + DY[d2];
                                if (inGrid(rx2, ry2) && itemStackOk(grid[rx2][ry2])) {
                                    neutronReceived[rx2][ry2] += neutronPulse * item1.getNeutronReflection(grid[rx][ry], Math.abs((d1 + 2) % 4 - d2));
                                    // neutron deflection coefficient bewteen external normal direction
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (itemStackOk(grid[i][j])) {
                    MINuclearItem item = (MINuclearItem) grid[i][j].getItem();
                    double heatProduction = item.getHeatProduction(grid[i][j], neutronReceived[i][j]);
                    currentHeat[i][j] = item.getHeat(grid[i][j]) + heatProduction;
                }
            }
        }

        double heatTranferOut[][] = new double[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                for (int d = 0; d < 4; d++) {
                    int rx = i + DX[d];
                    int ry = j + DY[d];
                    if (inGrid(rx, ry) && itemStackOk(grid[rx][ry])) {
                        MINuclearItem item = (MINuclearItem) grid[rx][ry].getItem();
                        heatTranferOut[i][j] += item.getHeatTransferMax(grid[rx][ry]);
                    }
                }
            }
        }

        double deltaHeat[][] = new double[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (itemStackOk(grid[i][j])) {
                    double movedHeat = Math.min(heatTranferOut[i][j], currentHeat[i][j]);
                    deltaHeat[i][j] -= movedHeat;
                }
            }
        }

        double deltaHeat_2[][] = new double[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (itemStackOk(grid[i][j])) {
                    MINuclearItem item = (MINuclearItem) grid[i][j].getItem();
                    double heatTransferMax = item.getHeatTransferMax(grid[i][j]);
                    if (heatTransferMax > 0) {
                        double movedHeat = 0;
                        for (int d = 0; d < 4; d++) {
                            int rx = i + DX[d];
                            int ry = j + DY[d];
                            if (inGrid(rx, ry) && itemStackOk(grid[rx][ry])) {
                                double heatTransferTot = heatTranferOut[rx][ry];
                                movedHeat += (-deltaHeat[rx][ry]) * heatTransferMax / heatTransferTot;
                            }

                        }
                        double remainingHeat = movedHeat;
                        for (int d = 0; d < 4; d++) {
                            int rx2 = i + DX[d];
                            int ry2 = j + DY[d];
                            if (inGrid(rx2, ry2) && itemStackOk(grid[rx2][ry2])) {
                                double heatToR2 = movedHeat * item.getHeatTransferNeighbourFraction(grid[i][j]) * 0.25d;
                                deltaHeat_2[rx2][ry2] += heatToR2;
                                remainingHeat -= heatToR2;
                            }
                        }
                        deltaHeat_2[i][j] += remainingHeat;
                    }
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (itemStackOk(grid[i][j])) {
                    MINuclearItem item = (MINuclearItem) grid[i][j].getItem();
                    item.setHeat(grid[i][j], doubleToInt(currentHeat[i][j] + deltaHeat[i][j] + deltaHeat_2[i][j], rand));
                    item.tick(grid[i][j], nuclearReactor, neutronReceived[i][j], rand);
                }
            }
        }

    }

    public static int doubleToInt(double s, Random rand) {
        int q = (int) Math.floor(s);
        if (rand.nextDouble() <= s - q) {
            q += 1;
        }
        return q;
    }

}

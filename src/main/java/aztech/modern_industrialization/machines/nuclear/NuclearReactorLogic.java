package aztech.modern_industrialization.machines.nuclear;

public class NuclearReactorLogic {

    public static final int[] DX = { 1, 0, -1, 0 };
    public static final int[] DY = { 0, -1, 0, 1 };

    public static void tick(NuclearReactorGrid grid) {

        double neutronReceived[][] = new double[grid.getSizeX()][grid.getSizeY()];
        double currentHeat[][] = new double[grid.getSizeX()][grid.getSizeY()];

        // neutrons production
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                currentHeat[i][j] = grid.getHeat(i, j);
                double neutronPulse = grid.getComponent(i, j).getNeutronPulse();
                for (int d1 = 0; d1 < 4; d1++) {
                    int rx = i + DX[d1];
                    int ry = j + DY[d1];
                    if (grid.inGrid(rx, ry)) {
                        for (int d2 = 0; d2 < 4; d2++) {
                            int rx2 = rx + DX[d2];
                            int ry2 = ry + DY[d2];
                            if (grid.inGrid(rx2, ry2)) {
                                neutronReceived[rx2][ry2] += neutronPulse
                                        * grid.getComponent(rx, ry).getNeutronReflection(Math.abs((d1 + 2) % 4 - d2));
                                // neutron deflection coefficient bewteen external normal direction
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                currentHeat[i][j] += grid.getComponent(i, j).getHeatProduction(neutronReceived[i][j]);
            }
        }

        double heatTranferOut[][] = new double[grid.getSizeX()][grid.getSizeY()];
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                for (int d = 0; d < 4; d++) {
                    int rx = i + DX[d];
                    int ry = j + DY[d];
                    if (grid.inGrid(rx, ry)) {
                        heatTranferOut[i][j] += grid.getComponent(i, j).getHeatTransferMax();
                    }
                }
            }
        }
        double deltaHeat[][] = new double[grid.getSizeX()][grid.getSizeY()];

        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                double movedHeatFraction = Math.min(heatTranferOut[i][j] / currentHeat[i][j], 1.0);
                double movedHeat = movedHeatFraction * currentHeat[i][j];
                deltaHeat[i][j] -= movedHeat;
            }
        }

        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                NuclearReactorComponent component = grid.getComponent(i, j);
                double movedHeat = 0;
                for (int d = 0; d < 4; d++) {
                    int rx = i + DX[d];
                    int ry = j + DY[d];
                    movedHeat += (-deltaHeat[rx][ry]) * component.getHeatTransferMax() / heatTranferOut[rx][ry];
                }
                double remainingHeat = movedHeat;
                for (int d = 0; d < 4; d++) {
                    int rx2 = i + DX[d];
                    int ry2 = j + DY[d];
                    if (grid.inGrid(rx2, ry2)) {
                        double heatToR2 = movedHeat * component.getHeatTransferNeighbourFraction() / 4.0d;
                        deltaHeat[rx2][ry2] += heatToR2;
                        remainingHeat -= heatToR2;
                    }
                    deltaHeat[i][j] += remainingHeat;
                }
            }
        }

        double heatSink[][] = new double[grid.getSizeX()][grid.getSizeY()];

        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                currentHeat[i][j] += deltaHeat[i][j];
                if (currentHeat[i][j] < 0) {
                    throw new IllegalStateException("Negative heat : " + currentHeat[i][j] + " at position (" + i + "," + j + ")");
                }
                heatSink[i][j] = Math.min(currentHeat[i][j], grid.getComponent(i, j).getHeatSink());
                currentHeat[i][j] -= heatSink[i][j];
            }
        }

        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                grid.setHeat(i, j, currentHeat[i][j]);
                grid.getComponent(i, j).tick(neutronReceived[i][j], grid.getHeat(i, j), heatSink[i][j]);
            }
        }

    }

}

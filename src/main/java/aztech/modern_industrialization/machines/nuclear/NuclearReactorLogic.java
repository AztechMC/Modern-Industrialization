package aztech.modern_industrialization.machines.nuclear;

public class NuclearReactorLogic {

    public static final int[] DX = {1, 0, -1, 0};
    public static final int[] DY = {0, -1, 0, 1};

    public void tick(NuclearReactorGrid grid){

        double neutronReceived[][] = new double[grid.getSizeX()][grid.getSizeY()];
        double currentHeat[][] = new double[grid.getSizeX()][grid.getSizeY()];

        // neutrons production
        for(int i = 0; i < grid.getSizeX(); i++){
            for(int j = 0; j < grid.getSizeY(); j++){
                currentHeat[i][j] = grid.getHeat(i,j);
                double neutronPulse = grid.getComponent(i,j).getNeutronPulse();
                for(int d1 = 0; d1 < 4; d1++){
                    int rx  = i+DX[d1];
                    int ry = j+DY[d1];
                    if(grid.inGrid(rx, ry)) {
                        for (int d2 = 0; d2 < 4; d2++) {
                            int rx2  = rx+DX[d2];
                            int ry2 = ry+DY[d2];
                            if(grid.inGrid(rx2, ry2)) {
                                neutronReceived[rx2][ry2] += neutronPulse*grid.getComponent(rx, ry).getNeutronReflection(Math.abs((d1+2)%4 - d2));
                                // neutron deflection coefficient bewteen external normal direction
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0; i < grid.getSizeX(); i++){
            for(int j = 0; j < grid.getSizeY(); j++) {
                currentHeat[i][j] += grid.getComponent(i,j).getHeatProduction(neutronReceived[i][j]);
            }
        }

        for(int i = 0; i < grid.getSizeX(); i++){
            for(int j = 0; j < grid.getSizeY(); j++) {
                currentHeat[i][j] += grid.getComponent(i,j).getHeatProduction(neutronReceived[i][j]);
            }
        }

        double heattranferOut[][] = new double[grid.getSizeX()][grid.getSizeY()];
        for(int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                for(int d = 0; d < 4; d++){
                    int rx = i + DX[d];
                    int ry = j + DY[d];
                    if(grid.inGrid(rx, ry)){
                        heattranferOut[i][j] += grid.getComponent(i, j).getHeatTransferMax();
                    }
                }
            }
        }

        for(int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                for(int d = 0; d < 4; d++){
                    int rx = i + DX[d];
                    int ry = j + DY[d];
                    if(grid.inGrid(rx, ry)){
                        heattranferOut[i][j] += grid.getComponent(i, j).getHeatTransferMax();
                    }
                }
            }
        }
    }



}

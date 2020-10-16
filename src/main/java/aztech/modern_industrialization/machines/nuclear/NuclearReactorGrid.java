package aztech.modern_industrialization.machines.nuclear;

public class NuclearReactorGrid {

    private int sizeX, sizeY;
    private NuclearReactorComponent grid[];
    private double heat[];

    public NuclearReactorGrid(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        grid = new NuclearReactorComponent[sizeX * sizeY];
        heat = new double[sizeX * sizeY];
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public boolean inGrid(int x, int y) {
        return x >= 0 && y >= 0 && x < sizeX && y < sizeY;
    }

    public NuclearReactorComponent getComponent(int x, int y) {
        if (x >= 0 && y >= 0 && x < sizeX && y < sizeY) {
            return grid[x + y * sizeX];
        } else {
            throw new IllegalArgumentException("Position : " + x + "," + y + "outside of grid of size : " + sizeX + "," + sizeY);
        }
    }

    public void setComponent(int x, int y, NuclearReactorComponent component) {
        if (x >= 0 && y >= 0 && x < sizeX && y < sizeY) {
            grid[x + y * sizeX] = component;
        } else {
            throw new IllegalArgumentException("Position : " + x + "," + y + "outside of grid of size : " + sizeX + "," + sizeY);
        }
    }

    public double getHeat(int x, int y) {
        if (x >= 0 && y >= 0 && x < sizeX && y < sizeY) {
            return heat[x + y * sizeX];
        } else {
            throw new IllegalArgumentException("Position : " + x + "," + y + "outside of grid of size : " + sizeX + "," + sizeY);
        }
    }

    public void setHeat(int x, int y, double heat) {
        if (x >= 0 && y >= 0 && x < sizeX && y < sizeY) {
            this.heat[x + y * sizeX] = heat;
        } else {
            throw new IllegalArgumentException("Position : " + x + "," + y + "outside of grid of size : " + sizeX + "," + sizeY);
        }
    }
}

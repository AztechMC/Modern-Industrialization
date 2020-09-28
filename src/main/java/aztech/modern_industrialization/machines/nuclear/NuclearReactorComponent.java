package aztech.modern_industrialization.machines.nuclear;

public interface NuclearReactorComponent {

    public double getNeutronPulse();

    public double getMaxHeat();

    public double getHeatProduction(double neutronReceived);

    public double getNeutronReflection(int angle); // 0 = 0°, 1 = 90°, 2 = 180°

    public double getHeatTransferMax();

    public double getHeatTransferNeighbourFraction(); // the rest of heat is transfered to itself

}

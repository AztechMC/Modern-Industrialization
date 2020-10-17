package aztech.modern_industrialization.nuclear;

public enum NuclearFuelType {

    URANIUM(22000, 100000, 32, 10),
    PLUTONIUM(17000, 20000, 256, 20),
    MOX(20000, 60000, 128, 15);

    public final int maxHeat;
    public final int durability;
    public final double heatProduction;
    public final double neutronPulse;

    NuclearFuelType(int maxHeat, int durability, double heatProduction, double neutronPulse) {
        this.maxHeat = maxHeat;
        this.durability = durability;
        this.heatProduction = heatProduction;
        this.neutronPulse = neutronPulse;
    }
}

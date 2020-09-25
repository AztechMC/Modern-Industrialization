package aztech.modern_industrialization.api.energy;

public enum CableTier {
    LV("lv", 32),
    MV("mv", 32*4),
    HV("hv", 32*4*4),
    ;

    public final String name;
    public final long eu;

    CableTier(String name, long eu) {
        this.name = name;
        this.eu = eu;
    }

    public long getMaxInsert() {
        return eu * 8;
    }

    public long getEu() {
        return eu;
    }

    @Override
    public String toString() {
        return name;
    }
}

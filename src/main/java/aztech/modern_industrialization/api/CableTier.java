package aztech.modern_industrialization.api;

public enum CableTier {
    LV("lv", 32*8),
    MV("mv", 32*8*4),
    HV("hv", 32*8*4*4),
    ;

    public final String name;
    public final long maxInsert;

    CableTier(String name, long maxInsert) {
        this.name = name;
        this.maxInsert = maxInsert;
    }
}

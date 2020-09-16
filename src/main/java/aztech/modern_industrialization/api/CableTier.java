package aztech.modern_industrialization.api;

public enum CableTier {
    LV(32*8),
    MV(32*8*4),
    HV(32*8*4*4),
    ;

    public final long maxInsert;

    CableTier(long maxInsert) {
        this.maxInsert = maxInsert;
    }
}

package aztech.modern_industrialization.machines.impl;

public enum MachineTier {
    BRONZE("bronze", true, 2, 1, 1),
    STEEL("steel", true, 4, 2, 2),
    LV("lv", false, 16, 4, 8),
    MV("mv", false, 32, 8, 16),
    HV("hv", false, 64, 12, 32),
    EV("ev", false, 256, 16, 128),
    ;

    private final String name;
    private final boolean steam;
    private final int maxEu;
    private final int baseOverclock;
    private final int maxOverclock;

    MachineTier(String name, boolean steam, int maxEu, int baseOverclock, int maxOverclock) {
        this.name = name;
        this.steam = steam;
        this.maxEu = maxEu;
        this.baseOverclock = baseOverclock;
        this.maxOverclock = maxOverclock;
    }

    public boolean isSteam() {
        return steam;
    }

    public int getMaxEu() {
        return maxEu;
    }

    @Override
    public String toString() {
        return name;
    }
}

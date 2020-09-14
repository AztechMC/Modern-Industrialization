package aztech.modern_industrialization.machines.impl;

public enum MachineTier {
    BRONZE("bronze", true, 2, 2),
    STEEL("steel", true, 4, 4),
    LV("lv", false, 32, 8),
    ;

    private final String name;
    private final boolean steam;
    private final int maxEu;
    private final int baseEu;

    MachineTier(String name, boolean steam, int maxEu, int baseEu) {
        this.name = name;
        this.steam = steam;
        this.maxEu = maxEu;
        this.baseEu = baseEu;
    }

    public boolean isSteam() {
        return steam;
    }

    public boolean isElectric() {
        return !steam;
    }

    public int getBaseEu() {
        return baseEu;
    }

    public int getMaxEu() {
        return maxEu;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getMaxStoredEu() {
        return isSteam() ? 0 : getMaxEu() * 100;
    }
}

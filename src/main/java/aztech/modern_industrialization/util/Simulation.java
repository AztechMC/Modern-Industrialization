package aztech.modern_industrialization.util;

public enum Simulation {
    SIMULATE,
    ACT;

    public boolean isSimulating() {
        return this == SIMULATE;
    }

    public boolean isActing() {
        return this == ACT;
    }
}

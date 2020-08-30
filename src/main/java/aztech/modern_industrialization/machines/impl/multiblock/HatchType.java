package aztech.modern_industrialization.machines.impl.multiblock;

public enum HatchType {
    ITEM_INPUT(0),
    ITEM_OUTPUT(1),
    FLUID_INPUT(2),
    FLUID_OUTPUT(3),
    ;

    private final int id;

    HatchType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

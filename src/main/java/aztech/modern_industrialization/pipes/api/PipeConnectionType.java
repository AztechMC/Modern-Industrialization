package aztech.modern_industrialization.pipes.api;

/**
 * A pipe connection type, used for rendering with the correct texture.
 */
public enum PipeConnectionType {
    FLUID(0),
    FLUID_IN(1),
    FLUID_IN_OUT(2),
    FLUID_OUT(3),
    ITEM(4),
    ITEM_IN(5),
    ITEM_IN_OUT(6),
    ITEM_OUT(7),
    ELECTRICITY(8)
    ;

    private final int id;

    PipeConnectionType(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public static PipeConnectionType byId(int id) {
        if(id == 0) return FLUID;
        else if(id == 1) return FLUID_IN;
        else if(id == 2) return FLUID_IN_OUT;
        else if(id == 3) return FLUID_OUT;
        else if(id == 4) return ITEM;
        else if(id == 5) return ITEM_IN;
        else if(id == 6) return ITEM_IN_OUT;
        else if(id == 7) return ITEM_OUT;
        else if(id == 8) return ELECTRICITY;
        else return null;
    }

    public boolean opensGui() {
        return this == ITEM_IN || this == ITEM_IN_OUT || this == ITEM_OUT;
    }
}

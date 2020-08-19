package aztech.modern_industrialization.pipes.api;

/**
 * A pipe connection type, used for rendering with the correct texture.
 */
public enum PipeConnectionType {
    FLUID(0),
    FLUID_IN(1),
    FLUID_IN_OUT(2),
    FLUID_OUT(3),
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
        else return null;
    }
}

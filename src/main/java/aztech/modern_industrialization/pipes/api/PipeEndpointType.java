package aztech.modern_industrialization.pipes.api;

/**
 * The type of an endpoint.
 */
public enum PipeEndpointType {
    /**
     * The endpoint of this connection is another pipe.
     */
    PIPE(0),
    /**
     * The endpoint of this connection is not another pipe, but there is no display difference except the shape.
     */
    BLOCK(1),
    /**
     * The endpoint of this connection is not another pipe, and the endpoint should be displayed as inserting.
     */
    BLOCK_IN(2),
    /**
     * The endpoint of this connection is not another pipe, and the endpoint should be displayed as inserting and extracting.
     */
    BLOCK_IN_OUT(3),
    /**
     * The endpoint of this connection is not another pipe, and the endpoint should be displayed as extracting.
     */
    BLOCK_OUT(4),
    ;

    private final int id;

    PipeEndpointType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PipeEndpointType byId(int id) {
        if(id == 0) return PIPE;
        else if(id == 1) return BLOCK;
        else if(id == 2) return BLOCK_IN;
        else if(id == 3) return BLOCK_IN_OUT;
        else if(id == 4) return BLOCK_OUT;
        else return null;
    }
}

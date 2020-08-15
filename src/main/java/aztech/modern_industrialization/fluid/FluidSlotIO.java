package aztech.modern_industrialization.fluid;

public enum FluidSlotIO {
    INPUT_ONLY(0), OUTPUT_ONLY(1), INPUT_AND_OUTPUT(2);

    private final int value;

    private FluidSlotIO(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


}

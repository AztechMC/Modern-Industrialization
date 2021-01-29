package aztech.modern_industrialization.materials.part;

public enum PipeType {
    FLUID("fluid", "fluid_pipe"),
    ITEM("item", "item_pipe"),
    CABLE("electricity", "cable"),
    ;

    PipeType(String internalName, String partName) {
        this.internalName = internalName;
        this.partName = partName;
    }

    public final String internalName;
    public final String partName;
}

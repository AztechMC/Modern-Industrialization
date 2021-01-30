package aztech.modern_industrialization.materials.part;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MIParts {
    public static final String BLADE = "blade";
    public static final String BLOCK = "block";
    public static final String BOLT = "bolt";
    public static final String CRUSHED_DUST = "crushed_dust";
    public static final String CURVED_PLATE = "curved_plate";
    public static final String DOUBLE_INGOT = "double_ingot";
    public static final String DUST = "dust";
    public static final String FLUID_PIPE = "fluid_pipe";
    public static final String GEAR = "gear";
    public static final String INGOT = "ingot";
    public static final String ITEM_PIPE = "item_pipe";
    public static final String LARGE_PLATE = "large_plate";
    public static final String NUGGET = "nugget";
    public static final String ORE = "ore";
    public static final String PLATE = "plate";
    public static final String RING = "ring";
    public static final String ROD = "rod";
    public static final String ROTOR = "rotor";
    public static final String TINY_DUST = "tiny_dust";

    public static final String[] ITEM_BASE = new String[]{CRUSHED_DUST, CURVED_PLATE, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE, TINY_DUST};

    public static final List<String> TAGGED_PARTS_LIST = Arrays.asList();
    public static final Set<String> TAGGED_PARTS = new HashSet<>(TAGGED_PARTS_LIST);
}

package aztech.modern_industrialization.machinesv2.models;

import net.minecraft.util.math.Direction;

public class MachineModelClientData {
    public final MachineCasingModel casing;
    public final Direction frontDirection;
    public boolean isActive = false;
    /**
     * May be null for no output.
     */
    public Direction outputDirection = null;
    public boolean itemAutoExtract = false;
    public boolean fluidAutoExtract = false;

    public MachineModelClientData(MachineCasingModel casing, Direction frontDirection) {
        this.casing = casing;
        this.frontDirection = frontDirection;
    }
}

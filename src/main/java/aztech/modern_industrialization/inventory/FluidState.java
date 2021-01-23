package aztech.modern_industrialization.inventory;

import net.minecraft.fluid.Fluid;

public class FluidState {
    public Fluid fluid;
    public long amount;

    public FluidState(Fluid fluid, long amount) {
        this.fluid = fluid;
        this.amount = amount;
    }
}

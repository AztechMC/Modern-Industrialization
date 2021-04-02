package aztech.modern_industrialization.pipes.fluid;

import alexiil.mc.lib.attributes.fluid.FluidTransferable;

/**
 * A target to be used during a transfer operation.
 */
public class FluidTarget {
    public final int priority;
    public final FluidTransferable transferable;

    // A temporary value used to sort fluid targets
    long simulationResult;

    public FluidTarget(int priority, FluidTransferable transferable) {
        this.priority = priority;
        this.transferable = transferable;
    }
}

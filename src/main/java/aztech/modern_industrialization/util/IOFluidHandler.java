package aztech.modern_industrialization.util;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class IOFluidHandler implements IFluidHandler {
    private final IFluidHandler handler;
    private final boolean allowInsert, allowExtract;

    public IOFluidHandler(IFluidHandler handler, boolean allowInsert, boolean allowExtract) {
        this.handler = handler;
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
    }

    @Override
    public int getTanks() {
        return handler.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return handler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return handler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return allowInsert && handler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return allowInsert ? handler.fill(resource, action) : 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return allowExtract ? handler.drain(resource, action) : FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return allowExtract ? handler.drain(maxDrain, action) : FluidStack.EMPTY;
    }
}

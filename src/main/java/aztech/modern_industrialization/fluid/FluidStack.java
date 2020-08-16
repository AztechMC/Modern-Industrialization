package aztech.modern_industrialization.fluid;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public class FluidStack {
    private Fluid fluid;
    private int amount;
    private int capacity;

    public FluidStack(Fluid fluid, int amount, int capacity) {
        this.fluid = fluid;
        this.amount = amount;
        this.capacity = capacity;
    }

    public Fluid getFluid() { return fluid; }
    public int getAmount() { return amount; }
    public int getCapacity() { return capacity; }

    public void setFluid(Fluid fluid) { this.fluid = fluid; }
    public void setAmount(int amount) {
        this.amount = Math.min(amount, capacity);
        if(amount == 0) setFluid(Fluids.EMPTY);
    }
    public void increment(int additional) {
        setAmount(this.amount + additional);
    }
}

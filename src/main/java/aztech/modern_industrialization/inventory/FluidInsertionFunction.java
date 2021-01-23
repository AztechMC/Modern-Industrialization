package aztech.modern_industrialization.inventory;

import net.fabricmc.fabric.api.transfer.v1.base.FixedDenominatorStorageFunction;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.fluid.Fluid;

import java.util.function.Predicate;

/**
 * A fluid insertion function that can also lock slots.
 */
@FunctionalInterface
public interface FluidInsertionFunction extends FixedDenominatorStorageFunction<Fluid> {
    /**
     * @param filter Return false to skip some ConfigurableFluidStacks.
     * @param lockSlots Whether to lock slots or not.
     */
    long apply(Fluid fluid, long amount, Transaction tx, Predicate<ConfigurableFluidStack> filter, boolean lockSlots);

    @Override
    default long denominator() {
        return 81000;
    }

    @Override
    default long applyFixedDenominator(Fluid fluid, long amount, Transaction tx) {
        return apply(fluid, amount, tx, ConfigurableFluidStack::canPipesInsert, false);
    }
}

package aztech.modern_industrialization.inventory;

import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.fabricmc.fabric.api.transfer.v1.base.CombinedStorageFunction;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.item.ItemPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageFunction;
import net.minecraft.fluid.Fluid;

import java.util.List;
import java.util.stream.Collectors;

public class MIFluidStorage implements Storage<Fluid> {
    private final List<ConfigurableFluidStack> stacks;
    private final FluidInsertionFunction insertionFunction;
    private final StorageFunction<Fluid> extractionFunction;

    public MIFluidStorage(List<ConfigurableFluidStack> stacks) {
        this.stacks = stacks;
        this.extractionFunction = new CombinedStorageFunction<>(stacks.stream().map(ConfigurableFluidStack::extractionFunction).collect(Collectors.toList()));
        this.insertionFunction = (fluid, amount, tx, filter, lockSlots) -> {
            FluidPreconditions.notEmptyNotNegative(fluid, amount);
            for (int iter = 0; iter < 2; ++iter) {
                boolean insertIntoEmptySlots = iter == 1;
                for (ConfigurableFluidStack stack : stacks) {
                    if (filter.test(stack) && stack.isValid(fluid)) {
                        if ((stack.getAmount() == 0 && insertIntoEmptySlots) || stack.getFluid() == fluid) {
                            long inserted = Math.min(amount, stack.getRemainingSpace());

                            if (inserted > 0) {
                                tx.enlist(stack);
                                stack.decrement(inserted);

                                if (lockSlots) {
                                    stack.enableMachineLock(fluid);
                                }

                                return inserted;
                            }
                        }
                    }
                }
            }
            return 0;
        };
    }

    @Override
    public FluidInsertionFunction insertionFunction() {
        return insertionFunction;
    }

    @Override
    public StorageFunction<Fluid> extractionFunction() {
        return extractionFunction;
    }

    @Override
    public boolean forEach(Storage.Visitor<Fluid> visitor) {
        for (ConfigurableFluidStack stack : stacks) {
            if (stack.getAmount() > 0) {
                if (visitor.visit(stack)) {
                    return true;
                }
            }
        }

        return false;
    }
}

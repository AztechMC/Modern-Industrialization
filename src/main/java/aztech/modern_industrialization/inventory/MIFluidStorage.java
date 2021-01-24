/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.inventory;

import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.transfer.v1.base.CombinedStorageFunction;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageFunction;
import net.minecraft.fluid.Fluid;

public class MIFluidStorage implements Storage<Fluid> {
    private final List<ConfigurableFluidStack> stacks;
    private final FluidInsertionFunction insertionFunction;
    private final StorageFunction<Fluid> extractionFunction;

    public MIFluidStorage(List<ConfigurableFluidStack> stacks) {
        this.stacks = stacks;
        this.extractionFunction = new CombinedStorageFunction<>(
                stacks.stream().map(ConfigurableFluidStack::extractionFunction).collect(Collectors.toList()));
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

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
package aztech.modern_industrialization.thirdparty.fabrictransfer.api.bridge;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleSlotStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import com.google.common.primitives.Ints;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public record SlotFluidHandler(SingleSlotStorage<FluidVariant> storage) implements IFluidHandler {
    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int slot) {
        return storage.getResource().toStack(Ints.saturatedCast(storage.getAmount()));
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        if (stack.isEmpty()) {
            return 0;
        }
        try (var tx = Transaction.hackyOpen()) {
            var inserted = storage.insert(FluidVariant.of(stack), stack.getAmount(), tx);
            if (action.execute()) {
                tx.commit();
            }
            return (int) inserted;
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || storage.isResourceBlank() || !resource.isFluidEqual(storage.getResource().toStack(1))) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int amount, FluidAction action) {
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        try (var tx = Transaction.hackyOpen()) {
            var resource = storage.getResource();
            if (resource.isBlank()) {
                return FluidStack.EMPTY;
            }
            var extracted = storage.extract(resource, amount, tx);
            if (action.execute()) {
                tx.commit();
            }
            return extracted == 0 ? FluidStack.EMPTY : resource.toStack((int) extracted);
        }
    }

    @Override
    public int getTankCapacity(int slot) {
        return Ints.saturatedCast(storage.getCapacity());
    }

    @Override
    public boolean isFluidValid(int slot, @NotNull FluidStack stack) {
        return true;
    }
}

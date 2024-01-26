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

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StorageUtil;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import com.google.common.primitives.Ints;
import java.util.List;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class MIFluidStorage extends MIStorage<Fluid, FluidVariant, ConfigurableFluidStack> {
    public final IFluidHandler fluidHandler = new FluidHandler();

    public MIFluidStorage(List<ConfigurableFluidStack> stacks) {
        super(stacks, true);
    }

    public class FluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return stacks.size();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            var stack = stacks.get(tank);
            return stack.getVariant().toStack(Ints.saturatedCast(stack.getAmount()));
        }

        @Override
        public int getTankCapacity(int tank) {
            return Ints.saturatedCast(stacks.get(tank).getCapacity());
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return stacks.get(tank).isResourceAllowedByLock(stack.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            try (var tx = Transaction.hackyOpen()) {
                long result = insert(FluidVariant.of(resource), resource.getAmount(), tx);
                if (result > 0 && action.execute()) {
                    tx.commit();
                }
                return (int) result;
            }
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            try (var tx = Transaction.hackyOpen()) {
                long result = extract(FluidVariant.of(resource), resource.getAmount(), tx);
                if (result > 0 && action.execute()) {
                    tx.commit();
                }
                var ret = resource.copy();
                ret.setAmount((int) result);
                return ret;
            }
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            try (var tx = Transaction.hackyOpen()) {
                var result = StorageUtil.extractAny(MIFluidStorage.this, maxDrain, tx);
                if (result == null) {
                    return FluidStack.EMPTY;
                }
                if (action.execute()) {
                    tx.commit();
                }
                return result.resource().toStack((int) result.amount());
            }
        }
    }
}

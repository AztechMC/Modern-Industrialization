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

package aztech.modern_industrialization.pipes.fluid;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Act as a pipe network extension when connected to a fluid pipe in I/O mode.
 */
public class FluidNetworkExtensionTank extends DelegatingResourceHandler<FluidResource> {
    private static final int NOT_CLAIMED = -1;
    private int lastClaimTick = NOT_CLAIMED;

    public FluidNetworkExtensionTank(ResourceHandler<FluidResource> handler) {
        if (handler.size() != 1) {
            throw new IllegalArgumentException("Can only have a handler of size 1, received: " + handler.size());
        }
        if (handler.getCapacityAsLong(0, FluidResource.EMPTY) > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Can only have a handler with int capacity");
        }
        super(handler);
    }

    public boolean tryClaimForNetwork(Level level, FluidResource networkFluid) {
        var handler = delegate.get();
        if (handler.getAmountAsLong(0) != 0 && !handler.getResource(0).equals(networkFluid)) {
            return false;
        }

        try (var tx = Transaction.openRoot()) {
            handler.extract(networkFluid, handler.getAmountAsInt(0), tx);
            long inserted = handler.insert(networkFluid, handler.getCapacityAsInt(0, FluidResource.EMPTY), tx);
            if (inserted != handler.getCapacityAsInt(0, FluidResource.EMPTY)) {
                // Tank locked to a different fluid
                return false;
            }
        }

        int tick = level.getServer().getTickCount();
        if (tick > lastClaimTick) {
            lastClaimTick = tick;
            return true;
        } else {
            return false;
        }
    }

    public void clear() {
        var handler = delegate.get();
        if (handler.getAmountAsLong(0) == 0) {
            return;
        }
        try (var tx = Transaction.openRoot()) {
            handler.extract(handler.getResource(0), handler.getAmountAsInt(0), tx);
            tx.commit();
        }
        if (handler.getAmountAsInt(0) > 0) {
            throw new IllegalStateException("Internal MI error: extension %s should be empty after clearing it.".formatted(this));
        }
    }

    public void releaseFromNetwork(FluidResource fluid, int amount) {
        var handler = delegate.get();
        if (handler.getAmountAsLong(0) > 0) {
            throw new IllegalStateException("Internal MI error: extension %s should be empty when being released.".formatted(this));
        }
        lastClaimTick = NOT_CLAIMED;
        if (fluid.isEmpty()) {
            if (handler.getAmountAsLong(0) != 0) {
                throw new IllegalStateException("Internal MI error: releasing extension %s from network with non-empty tank.".formatted(this));
            }
        }
        try (var tx = Transaction.openRoot()) {
            long inserted = handler.insert(fluid, amount, tx);
            tx.commit();
            if (inserted != amount) {
                throw new IllegalStateException(
                        "Internal MI error: releasing extension %s, only inserted %d out of %d.".formatted(this, inserted, amount));
            }
        }
    }

    public long getAmount() {
        return getAmountAsLong(0);
    }

    public long getCapacity() {
        return getCapacityAsLong(0, FluidResource.EMPTY);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (disallowIo()) {
            return 0;
        }
        return super.insert(resource, amount, transaction);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (disallowIo()) {
            return 0;
        }
        return super.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        if (disallowIo()) {
            return 0;
        }
        return super.extract(resource, amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (disallowIo()) {
            return 0;
        }
        return super.extract(index, resource, amount, transaction);
    }

    protected boolean disallowIo() {
        return lastClaimTick != NOT_CLAIMED;
    }

    @Override
    public String toString() {
        return "FluidNetworkExtensionTank{" + delegate.get() + '}';
    }
}

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

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.bridge.SlotFluidHandler;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleSlotStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import net.minecraft.world.level.Level;

/**
 * Act as a pipe network extension when connected to a fluid pipe in I/O mode.
 */
public class FluidNetworkExtensionTank extends SlotFluidHandler {
    private static final int NOT_CLAIMED = -1;
    private int lastClaimTick = NOT_CLAIMED;

    public FluidNetworkExtensionTank(SingleSlotStorage<FluidVariant> storage) {
        super(storage);
    }

    public boolean tryClaimForNetwork(Level level, FluidVariant networkFluid) {
        if (storage.getAmount() != 0 && !storage.getResource().equals(networkFluid)) {
            return false;
        }

        try (var tx = Transaction.openRoot()) {
            storage.extract(networkFluid, storage.getAmount(), tx);
            long inserted = storage.insert(networkFluid, storage.getCapacity(), tx);
            if (inserted != storage.getCapacity()) {
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
        if (storage.getAmount() == 0) {
            return;
        }
        try (var tx = Transaction.openRoot()) {
            storage.extract(storage.getResource(), storage.getAmount(), tx);
            tx.commit();
        }
        if (storage.getAmount() > 0) {
            throw new IllegalStateException("Internal MI error: extension %s should be empty after clearing it.".formatted(this));
        }
    }

    public void releaseFromNetwork(FluidVariant fluid, long amount) {
        if (storage.getAmount() > 0) {
            throw new IllegalStateException("Internal MI error: extension %s should be empty when being released.".formatted(this));
        }
        lastClaimTick = NOT_CLAIMED;
        if (fluid.isBlank()) {
            if (storage.getAmount() != 0) {
                throw new IllegalStateException("Internal MI error: releasing extension %s from network with non-empty tank.".formatted(this));
            }
        }
        try (var tx = Transaction.openRoot()) {
            long inserted = storage.insert(fluid, amount, tx);
            tx.commit();
            if (inserted != amount) {
                throw new IllegalStateException(
                        "Internal MI error: releasing extension %s, only inserted %d out of %d.".formatted(this, inserted, amount));
            }
        }
    }

    public long getCapacity() {
        return storage.getCapacity();
    }

    @Override
    protected boolean disallowIo() {
        return lastClaimTick != NOT_CLAIMED;
    }

    @Override
    public String toString() {
        return "FluidNetworkExtensionTank{" + storage + '}';
    }
}

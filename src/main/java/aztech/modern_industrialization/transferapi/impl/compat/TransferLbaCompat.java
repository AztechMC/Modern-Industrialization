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
package aztech.modern_industrialization.transferapi.impl.compat;

import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import aztech.modern_industrialization.transferapi.api.item.ItemApi;
import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.util.math.Direction;

public class TransferLbaCompat {
    public static void init() {
        FluidAttributes.forEachInv(inv -> inv.appendBlockAdder((world, pos, state, to) -> {
            Direction direction = to.getTargetSide();
            if (direction != null && !to.hasOfferedAny()) {
                Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(world, pos, direction);
                if (fluidStorage != null) {
                    WrappedFluidStorage wrapped = new WrappedFluidStorage(fluidStorage);

                    if (FluidAttributes.GROUPED_INVENTORY_BASED.contains(to.attribute)) {
                        to.offer(wrapped);
                    } else {
                        to.offer(new FixedGroupedFluidInv(wrapped));
                    }
                }
            }
        }));
        ItemAttributes.forEachInv(inv -> inv.appendBlockAdder((world, pos, state, to) -> {
            Direction direction = to.getTargetSide();
            // Must check hasOfferedAny otherwise both LBA and MI will offer a wrapper for
            // vanilla Inventories.
            if (direction != null && !to.hasOfferedAny()) {
                Storage<ItemKey> itemStorage = ItemApi.SIDED.find(world, pos, direction);
                if (itemStorage != null) {
                    WrappedItemStorage wrapped = new WrappedItemStorage(itemStorage);

                    if (ItemAttributes.GROUPED_INVENTORY_BASED.contains(to.attribute)) {
                        to.offer(wrapped);
                    } else {
                        to.offer(new FixedGroupedItemInv(wrapped));
                    }
                }
            }
        }));
    }

    /**
     * The call to filter.test(...) inside the LBA wrappers might cause a nested
     * simulation, so we have to be careful and store the transaction during the
     * extraction operation.
     */
    static final ThreadLocal<Transaction> OPEN_TRANSACTION = new ThreadLocal<>();

    static Transaction openPossiblyNestedTransaction() {
        Transaction extractionTransaction = OPEN_TRANSACTION.get();
        if (extractionTransaction != null) {
            return extractionTransaction.openNested();
        } else {
            return Transaction.openOuter();
        }
    }
}

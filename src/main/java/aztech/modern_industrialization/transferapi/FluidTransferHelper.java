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
package aztech.modern_industrialization.transferapi;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class FluidTransferHelper {

    /**
     * Return an extractable fluid, or EMPTY if none could be found.
     */
    public static FluidKey findExtractableFluid(Storage<FluidKey> storage) {
        try (Transaction tx = Transaction.openOuter()) {
            for (StorageView<FluidKey> view : storage.iterable(tx)) {
                FluidKey key = view.resource();
                if (!key.isEmpty() && view.extract(key, Integer.MAX_VALUE, tx) > 0) {
                    return key;
                }
            }
        }
        return FluidKey.empty();
    }

    /**
     * Find a contained fluid, or EMPTY if there is no fluid or if the storage is
     * null.
     */
    public static FluidKey findFluid(@Nullable Storage<FluidKey> storage) {
        if (storage == null) {
            return FluidKey.empty();
        } else {
            FluidKey fluid = FluidKey.empty();
            try (Transaction tx = Transaction.openOuter()) {
                for (StorageView<FluidKey> view : storage.iterable(tx)) {
                    fluid = view.resource();
                    break;
                }
            }
            return fluid;
        }
    }
}

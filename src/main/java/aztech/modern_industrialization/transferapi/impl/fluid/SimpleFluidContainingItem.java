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
package aztech.modern_industrialization.transferapi.impl.fluid;

import aztech.modern_industrialization.transferapi.api.context.ContainerItemContext;
import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import java.util.Iterator;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public class SimpleFluidContainingItem implements ExtractionOnlyStorage<FluidKey>, StorageView<FluidKey> {
    private final ContainerItemContext ctx;
    private final ItemKey sourceKey;
    private final FluidKey fluid;
    private final long amount;
    private final Function<ItemKey, ItemKey> keyMapping;

    public SimpleFluidContainingItem(ContainerItemContext ctx, ItemKey sourceKey, FluidKey fluid, long amount,
            Function<ItemKey, ItemKey> keyMapping) {
        this.ctx = ctx;
        this.sourceKey = sourceKey;
        this.fluid = fluid;
        this.amount = amount;
        this.keyMapping = keyMapping;
    }

    @Override
    public FluidKey resource() {
        return fluid;
    }

    @Override
    public long amount() {
        return amount;
    }

    @Override
    public long capacity() {
        return amount;
    }

    @Override
    public boolean isEmpty() {
        return resource().isEmpty();
    }

    @Override
    public long extract(FluidKey resource, long maxAmount, Transaction transaction) {
        FluidPreconditions.notEmptyNotNegative(resource, maxAmount);

        if (maxAmount >= amount && resource == fluid && ctx.getCount(transaction) > 0) {
            if (ctx.transform(1, keyMapping.apply(sourceKey), transaction)) {
                return amount;
            }
        }

        return 0;
    }

    @Override
    public Iterator<StorageView<FluidKey>> iterator(Transaction transaction) {
        return SingleViewIterator.create(this, transaction);
    }
}

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
import aztech.modern_industrialization.transferapi.api.fluid.ItemFluidApi;
import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EmptyItemsRegistry {
    private static final Map<Item, EmptyItemProvider> PROVIDERS = new IdentityHashMap<>();

    public static synchronized void registerEmptyItem(Item emptyItem, FluidVariant fluid, long amount, Function<ItemKey, ItemKey> keyMapping) {
        PROVIDERS.computeIfAbsent(emptyItem, item -> {
            EmptyItemProvider provider = new EmptyItemProvider();
            ItemFluidApi.ITEM.registerForItems(provider, emptyItem);
            return provider;
        });
        EmptyItemProvider provider = PROVIDERS.get(emptyItem);

        // We use a copy-on-write strategy to register the fluid filling if possible
        Map<FluidVariant, FillInfo> copy = new IdentityHashMap<>(provider.acceptedFluids);
        copy.putIfAbsent(fluid, new FillInfo(amount, keyMapping));
        provider.acceptedFluids = copy;
    }

    private static class EmptyItemProvider implements ItemApiLookup.ItemApiProvider<Storage<FluidVariant>, ContainerItemContext> {
        private volatile Map<FluidVariant, FillInfo> acceptedFluids = new IdentityHashMap<>();

        @Override
        public @Nullable Storage<FluidVariant> find(ItemStack stack, ContainerItemContext context) {
            return new EmptyItemStorage(ItemKey.of(stack), context);
        }

        private class EmptyItemStorage implements InsertionOnlyStorage<FluidVariant> {
            private final ItemKey initialKey;
            private final ContainerItemContext ctx;

            private EmptyItemStorage(ItemKey initialKey, ContainerItemContext ctx) {
                this.initialKey = initialKey;
                this.ctx = ctx;
            }

            @Override
            public long insert(FluidVariant fluid, long maxAmount, TransactionContext transaction) {
                StoragePreconditions.notBlankNotNegative(fluid, maxAmount);

                if (ctx.getCount(transaction) == 0)
                    return 0;
                FillInfo fillInfo = acceptedFluids.get(fluid);
                if (fillInfo == null)
                    return 0;

                if (maxAmount >= fillInfo.amount) {
                    if (ctx.transform(1, fillInfo.keyMapping.apply(initialKey), transaction)) {
                        return fillInfo.amount;
                    }
                }

                return 0;
            }

            @Override
            public Iterator<StorageView<FluidVariant>> iterator(TransactionContext transaction) {
                return Collections.emptyIterator();
            }
        }
    }

    private static class FillInfo {
        private final long amount;
        private final Function<ItemKey, ItemKey> keyMapping;

        private FillInfo(long amount, Function<ItemKey, ItemKey> keyMapping) {
            this.amount = amount;
            this.keyMapping = keyMapping;
        }
    }
}

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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.transferapi.api.context.ContainerItemContext;
import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

/**
 * Helper class for fluid items that can only contain FluidFuels
 */
public interface FluidFuelItemHelper {
    static FluidKey getFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? FluidKey.empty() : NbtHelper.getFluidCompatible(tag, "fluid");
    }

    static void setFluid(ItemStack stack, FluidKey fluid) {
        if (!fluid.isEmpty()) {
            NbtHelper.putFluid(stack.getOrCreateTag(), "fluid", fluid);
        } else {
            stack.removeSubTag("fluid");
        }
    }

    static long getAmount(ItemStack stack) {
        if (getFluid(stack).isEmpty()) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return tag.contains("amount") ? tag.getInt("amount") * 81 : tag.getLong("amt");
        } else {
            return 0;
        }
    }

    static void setAmount(ItemStack stack, long amount) {
        if (amount != 0) {
            stack.getOrCreateTag().putLong("amt", amount);
        } else {
            stack.removeSubTag("amt");
            stack.removeSubTag("fluid");
        }
    }

    static void decrement(ItemStack stack) {
        long amount = getAmount(stack);
        if (amount > 0) {
            setAmount(stack, Math.max(0, amount - 81));
        }
    }

    class ItemStorage implements Storage<FluidKey>, StorageView<FluidKey> {
        private final Item item;
        private final FluidKey fluid;
        private final long amount;
        private final long capacity;
        private final ContainerItemContext ctx;

        public ItemStorage(long capacity, ItemStack stack, ContainerItemContext ctx) {
            this.item = stack.getItem();
            this.fluid = FluidFuelItemHelper.getFluid(stack);
            this.amount = getAmount(stack);
            this.capacity = capacity;
            this.ctx = ctx;
        }

        private boolean updateItem(FluidKey fluid, long amount, Transaction tx) {
            ItemStack stack = new ItemStack(item);
            setAmount(stack, amount);
            setFluid(stack, fluid);
            return ctx.transform(1, ItemKey.of(stack), tx);
        }

        @Override
        public boolean supportsInsertion() {
            return true;
        }

        @Override
        public long insert(FluidKey fluid, long maxAmount, Transaction tx) {
            FluidPreconditions.notEmptyNotNegative(fluid, maxAmount);
            if (ctx.getCount(tx) == 0)
                return 0;

            long inserted = 0;
            if (ItemStorage.this.fluid.isEmpty()) {
                inserted = Math.min(capacity, maxAmount);
            } else if (ItemStorage.this.fluid.equals(fluid)) {
                inserted = Math.min(capacity - amount, maxAmount);
            }
            if (inserted > 0) {
                try (Transaction nested = tx.openNested()) {
                    if (updateItem(fluid, amount + inserted, nested)) {
                        nested.commit();
                        return inserted;
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        public long extract(FluidKey fluid, long maxAmount, Transaction tx) {
            FluidPreconditions.notEmptyNotNegative(fluid, maxAmount);
            if (ctx.getCount(tx) == 0)
                return 0;

            long extracted = 0;
            if (ItemStorage.this.fluid.equals(fluid)) {
                extracted = Math.min(maxAmount, amount);
            }
            if (extracted > 0) {
                try (Transaction nested = tx.openNested()) {
                    if (!updateItem(fluid, amount - extracted, nested)) {
                        nested.commit();
                        return extracted;
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return resource().isEmpty();
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
            return capacity;
        }

        @Override
        public Iterator<StorageView<FluidKey>> iterator(Transaction transaction) {
            return SingleViewIterator.create(this, transaction);
        }
    }

    static void appendTooltip(ItemStack stack, List<Text> tooltip, long capacity) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
        FluidKey fluid = getFluid(stack);
        tooltip.add(FluidHelper.getFluidName(fluid, true));
        if (!fluid.isEmpty()) {
            tooltip.add(FluidHelper.getFluidAmount(getAmount(stack), capacity).setStyle(style));
        }
    }
}

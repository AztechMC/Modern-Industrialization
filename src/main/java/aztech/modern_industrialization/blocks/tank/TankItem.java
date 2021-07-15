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
package aztech.modern_industrialization.blocks.tank;

import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class TankItem extends BlockItem {
    public final long capacity;

    public TankItem(Block block, Settings settings, long capacity) {
        super(block, settings);
        this.capacity = capacity;
    }

    public void registerItemApi() {
        FluidStorage.ITEM.registerForItems(TankItemStorage::new, this);
    }

    public boolean isEmpty(ItemStack stack) {
        return stack.getSubTag("BlockEntityTag") == null;
    }

    public FluidVariant getFluid(ItemStack stack) {
        return NbtHelper.getFluidCompatible(stack.getSubTag("BlockEntityTag"), "fluid");
    }

    private void setFluid(ItemStack stack, FluidVariant fluid) {
        NbtHelper.putFluid(stack.getOrCreateSubTag("BlockEntityTag"), "fluid", fluid);
    }

    public long getAmount(ItemStack stack) {
        if (getFluid(stack).isBlank()) {
            return 0;
        }
        NbtCompound tag = stack.getSubTag("BlockEntityTag");
        if (tag == null)
            return 0;
        else
            return tag.getLong("amt");
    }

    private void setAmount(ItemStack stack, long amount) {
        stack.getOrCreateSubTag("BlockEntityTag").putLong("amt", amount);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
        if (!isEmpty(stack)) {
            tooltip.add(FluidHelper.getFluidName(getFluid(stack), true));
            tooltip.add(FluidHelper.getFluidAmount(getAmount(stack), capacity));
        } else {
            tooltip.add(new TranslatableText("text.modern_industrialization.empty").setStyle(style));
        }
    }

    class TankItemStorage implements Storage<FluidVariant>, StorageView<FluidVariant> {
        private final ContainerItemContext ctx;

        TankItemStorage(ItemStack stack, ContainerItemContext ctx) {
            this.ctx = ctx;
        }

        private boolean updateTank(FluidVariant fluid, long amount, TransactionContext tx) {
            ItemStack result = new ItemStack(TankItem.this);
            if (amount > 0) {
                setFluid(result, fluid);
                setAmount(result, amount);
            }
            ItemVariant into = ItemVariant.of(result);

            return ctx.transform(into, 1, tx) == 1;
        }

        @Override
        public boolean supportsInsertion() {
            return true;
        }

        @Override
        public long insert(FluidVariant insertedFluid, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(insertedFluid, maxAmount);

            ItemStack current = ctx.getItemVariant().toStack();
            if (!current.isOf(TankItem.this))
                return 0;

            long amount = TankItem.this.getAmount(current);
            FluidVariant fluid = TankItem.this.getFluid(current);

            long inserted = 0;
            if (fluid.isBlank()) {
                inserted = Math.min(capacity, maxAmount);
            } else if (fluid.equals(insertedFluid)) {
                inserted = Math.min(capacity - amount, maxAmount);
            }
            if (inserted > 0) {
                if (updateTank(insertedFluid, amount + inserted, transaction)) {
                    return inserted;
                }
            }
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        public long extract(FluidVariant extractedFluid, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(extractedFluid, maxAmount);

            ItemStack current = ctx.getItemVariant().toStack();
            if (!current.isOf(TankItem.this))
                return 0;

            long amount = TankItem.this.getAmount(current);
            FluidVariant fluid = TankItem.this.getFluid(current);

            long extracted = 0;
            if (fluid.equals(extractedFluid)) {
                extracted = Math.min(maxAmount, amount);
            }
            if (extracted > 0) {
                if (updateTank(fluid, amount - extracted, transaction)) {
                    return extracted;
                }
            }
            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public FluidVariant getResource() {
            ItemStack current = ctx.getItemVariant().toStack();
            if (current.isOf(TankItem.this)) {
                return TankItem.this.getFluid(current);
            } else {
                return FluidVariant.blank();
            }
        }

        @Override
        public long getAmount() {
            ItemStack current = ctx.getItemVariant().toStack();
            if (current.isOf(TankItem.this)) {
                return TankItem.this.getAmount(current);
            } else {
                return 0;
            }
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator(TransactionContext transaction) {
            return SingleViewIterator.create(this, transaction);
        }
    }
}

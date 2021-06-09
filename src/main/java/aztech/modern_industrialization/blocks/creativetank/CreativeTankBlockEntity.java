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
package aztech.modern_industrialization.blocks.creativetank;

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.transferapi.api.context.ContainerItemContext;
import aztech.modern_industrialization.transferapi.api.fluid.ItemFluidApi;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.Iterator;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;

public class CreativeTankBlockEntity extends FastBlockEntity
        implements ExtractionOnlyStorage<FluidKey>, StorageView<FluidKey>, BlockEntityClientSerializable {
    FluidKey fluid = FluidKey.empty();

    public CreativeTankBlockEntity() {
        super(MITanks.CREATIVE_BLOCK_ENTITY_TYPE);
    }

    @Override
    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        NbtHelper.putFluid(tag, "fluid", fluid);
        return tag;
    }

    public void onChanged() {
        markDirty();
        if (!world.isClient)
            sync();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        toClientTag(tag);
        return super.writeNbt(tag);
    }

    @Override
    public void readNbt(BlockState state, NbtCompound tag) {
        fromClientTag(tag);
        super.readNbt(state, tag);
    }

    public boolean onPlayerUse(PlayerEntity player) {
        Storage<FluidKey> handIo = ItemFluidApi.ITEM.find(player.getMainHandStack(), ContainerItemContext.ofPlayerHand(player, Hand.MAIN_HAND));
        if (handIo != null) {
            if (isEmpty()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    for (StorageView<FluidKey> view : handIo.iterable(transaction)) {
                        if (!view.isEmpty()) {
                            fluid = view.resource();
                            onChanged();
                            break;
                        }
                    }
                }
                return !isEmpty();
            } else {
                try (Transaction tx = Transaction.openOuter()) {
                    long inserted = handIo.insert(fluid, Integer.MAX_VALUE, tx);
                    tx.commit();
                    return inserted > 0;
                }
            }
        }
        return false;
    }

    @Override
    public long extract(FluidKey fluid, long maxAmount, Transaction transaction) {
        FluidPreconditions.notEmptyNotNegative(fluid, maxAmount);
        return maxAmount;
    }

    @Override
    public FluidKey resource() {
        return fluid;
    }

    @Override
    public long capacity() {
        return Integer.MAX_VALUE / 100; // NOTE: this can overflow otherwise, fix this?
    }

    @Override
    public Iterator<StorageView<FluidKey>> iterator(Transaction transaction) {
        return SingleViewIterator.create(this, transaction);
    }

    @Override
    public long amount() {
        return Integer.MAX_VALUE;
    }
}

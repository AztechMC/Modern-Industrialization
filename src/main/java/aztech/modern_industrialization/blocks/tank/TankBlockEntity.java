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

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.Iterator;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class TankBlockEntity extends FastBlockEntity implements Storage<FluidVariant>, StorageView<FluidVariant>, BlockEntityClientSerializable {
    FluidVariant fluid = FluidVariant.blank();
    long amount;
    final long capacity;
    private long version = 0;
    private final TankParticipant participant = new TankParticipant();

    public TankBlockEntity(BlockEntityType<?> bet, BlockPos pos, BlockState state, long capacity) {
        super(bet, pos, state);
        this.capacity = capacity;
    }

    public boolean isEmpty() {
        return amount == 0;
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
        amount = tag.getLong("amt");

        if (fluid.isBlank()) {
            amount = 0;
        }
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        NbtHelper.putFluid(tag, "fluid", fluid);
        tag.putLong("amt", amount);
        return tag;
    }

    public void onChanged() {
        version++;
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
    public void readNbt(NbtCompound tag) {
        fromClientTag(tag);
        super.readNbt(tag);
    }

    public boolean onPlayerUse(PlayerEntity player) {
        Storage<FluidVariant> handIo = ContainerItemContext.ofPlayerHand(player, Hand.MAIN_HAND).find(FluidStorage.ITEM);
        if (handIo != null) {
            // move from hand into this tank
            if (StorageUtil.move(handIo, this, f -> true, Integer.MAX_VALUE, null) > 0)
                return true;
            // move from this tank into hand
            if (StorageUtil.move(this, handIo, f -> true, Integer.MAX_VALUE, null) > 0)
                return true;
        }
        return false;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public long insert(FluidVariant fluid, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluid, maxAmount);
        if (this.fluid.isBlank() || TankBlockEntity.this.fluid.equals(fluid)) {
            long inserted = Math.min(maxAmount, capacity - amount);
            if (inserted > 0) {
                participant.updateSnapshots(transaction);
                amount += inserted;
                TankBlockEntity.this.fluid = fluid;
            }
            return inserted;
        }
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public long extract(FluidVariant fluid, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluid, maxAmount);
        if (fluid.equals(TankBlockEntity.this.fluid)) {
            long extracted = Math.min(maxAmount, amount);
            if (extracted > 0) {
                participant.updateSnapshots(transaction);
                amount -= extracted;
                if (amount == 0) {
                    TankBlockEntity.this.fluid = FluidVariant.blank();
                }
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public FluidVariant getResource() {
        return fluid;
    }

    @Override
    public boolean isResourceBlank() {
        return getResource().isBlank();
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator(TransactionContext transaction) {
        return SingleViewIterator.create(this, transaction);
    }

    @Override
    public long getVersion() {
        return version;
    }

    private class TankParticipant extends SnapshotParticipant<ResourceAmount<FluidVariant>> {
        @Override
        protected ResourceAmount<FluidVariant> createSnapshot() {
            return new ResourceAmount<>(fluid, amount);
        }

        @Override
        protected void readSnapshot(ResourceAmount<FluidVariant> snapshot) {
            fluid = snapshot.resource();
            amount = snapshot.amount();
        }

        @Override
        protected void onFinalCommit() {
            onChanged();
        }
    }
}

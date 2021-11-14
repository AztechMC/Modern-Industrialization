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
import aztech.modern_industrialization.api.WrenchableBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.CreativeTankSetup;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.Iterator;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class CreativeTankBlockEntity extends FastBlockEntity
        implements ExtractionOnlyStorage<FluidVariant>, StorageView<FluidVariant>, BlockEntityClientSerializable, WrenchableBlockEntity {
    FluidVariant fluid = FluidVariant.blank();

    public CreativeTankBlockEntity(BlockPos pos, BlockState state) {
        super(CreativeTankSetup.CREATIVE_BLOCK_ENTITY_TYPE, pos, state);
    }

    @Override
    public boolean isResourceBlank() {
        return fluid.isBlank();
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
    public void writeNbt(NbtCompound tag) {
        toClientTag(tag);
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        fromClientTag(tag);
        super.readNbt(tag);
    }

    public boolean onPlayerUse(PlayerEntity player) {
        Storage<FluidVariant> handIo = ContainerItemContext.ofPlayerHand(player, Hand.MAIN_HAND).find(FluidStorage.ITEM);
        if (handIo != null) {
            if (isResourceBlank()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    for (StorageView<FluidVariant> view : handIo.iterable(transaction)) {
                        if (!view.isResourceBlank()) {
                            fluid = view.getResource();
                            onChanged();
                            break;
                        }
                    }
                }
                return !isResourceBlank();
            } else {
                try (Transaction tx = Transaction.openOuter()) {
                    long inserted = handIo.insert(fluid, Long.MAX_VALUE, tx);
                    tx.commit();
                    return inserted > 0;
                }
            }
        }
        return false;
    }

    @Override
    public long extract(FluidVariant fluid, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluid, maxAmount);
        return maxAmount;
    }

    @Override
    public FluidVariant getResource() {
        return fluid;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator(TransactionContext transaction) {
        return SingleViewIterator.create(this, transaction);
    }

    @Override
    public long getAmount() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean useWrench(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking()) {
            CreativeTankBlock tank = (CreativeTankBlock) getCachedState().getBlock();
            world.spawnEntity(new ItemEntity(world, hit.getPos().x, hit.getPos().y, hit.getPos().z, tank.getStack(world.getBlockEntity(pos))));
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            // TODO: play sound
            return true;
        }
        return false;
    }
}

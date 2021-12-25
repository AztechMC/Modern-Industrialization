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

import aztech.modern_industrialization.MIBlockEntityTypes;
import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.api.WrenchableBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.Iterator;
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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CreativeTankBlockEntity extends FastBlockEntity
        implements ExtractionOnlyStorage<FluidVariant>, StorageView<FluidVariant>, WrenchableBlockEntity {
    FluidVariant fluid = FluidVariant.blank();

    public CreativeTankBlockEntity(BlockPos pos, BlockState state) {
        super(MIBlockEntityTypes.CREATIVE_TANK, pos, state);
    }

    @Override
    public boolean isResourceBlank() {
        return fluid.isBlank();
    }

    public void onChanged() {
        setChanged();
        if (!level.isClientSide)
            sync();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        NbtHelper.putFluid(tag, "fluid", fluid);
    }

    @Override
    public void load(CompoundTag tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public boolean onPlayerUse(Player player) {
        Storage<FluidVariant> handIo = ContainerItemContext.ofPlayerHand(player, InteractionHand.MAIN_HAND).find(FluidStorage.ITEM);
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
    public boolean useWrench(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            CreativeTankBlock tank = (CreativeTankBlock) getBlockState().getBlock();
            level.addFreshEntity(new ItemEntity(level, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                    tank.getStack(level.getBlockEntity(worldPosition))));
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
            // TODO: play sound
            return true;
        }
        return false;
    }
}

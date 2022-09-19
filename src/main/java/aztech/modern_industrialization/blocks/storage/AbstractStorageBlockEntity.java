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
package aztech.modern_industrialization.blocks.storage;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.api.WrenchableBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractStorageBlockEntity<T extends TransferVariant<?>> extends FastBlockEntity
        implements SingleSlotStorage<T>, WrenchableBlockEntity {

    protected T resource;
    protected long amount;
    private long version;
    private boolean isLocked;

    private final ResourceParticipant participant = new ResourceParticipant();

    public AbstractStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        resource = getBlankResource();
    }

    public void onChanged() {
        version++;
        setChanged();
        if (!level.isClientSide)
            sync();
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

    @Override
    public boolean useWrench(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            var block = (AbstractStorageBlock) getBlockState().getBlock();
            level.addFreshEntity(new ItemEntity(level, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, block.getStack(this)));
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
        } else {
            this.toggleLocked();
            player.displayClientMessage(
                    isLocked ? MIText.Locked.text() : MIText.Unlocked.text(), true);

        }
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if ((this.resource.isBlank() && !this.isLocked) || this.resource.equals(resource)) {
            long inserted = Math.min(maxAmount, getCapacityForResource(resource) - amount);
            if (inserted > 0) {
                participant.updateSnapshots(transaction);
                amount += inserted;
                this.resource = resource;
            }
            return inserted;
        }
        return 0;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (resource.equals(this.resource)) {
            long extracted = Math.min(maxAmount, amount);
            if (extracted > 0) {
                participant.updateSnapshots(transaction);
                amount -= extracted;
                if (amount == 0 && !isLocked) {
                    this.resource = getBlankResource();
                }
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return getResource().isBlank();
    }

    public abstract T getBlankResource();

    @Override
    public T getResource() {
        return resource;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    public boolean isEmpty() {
        return amount == 0;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public long getCapacity() {
        return this.getCapacityForResource(resource);
    }

    public abstract long getCapacityForResource(T resource);

    private class ResourceParticipant extends SnapshotParticipant<ResourceAmount<T>> {
        @Override
        protected ResourceAmount<T> createSnapshot() {
            return new ResourceAmount<>(resource, amount);
        }

        @Override
        protected void readSnapshot(ResourceAmount<T> snapshot) {
            resource = snapshot.resource();
            amount = snapshot.amount();
        }

        @Override
        protected void onFinalCommit() {
            onChanged();
        }
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean toggleLocked() {
        isLocked = !isLocked;
        if (!isLocked && amount == 0) {
            resource = getBlankResource();
        }
        return isLocked;
    }

    @Override
    public void load(CompoundTag tag) {
        amount = tag.getLong("amt");
        isLocked = tag.getBoolean("locked");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putLong("amt", amount);
        tag.putBoolean("locked", isLocked);
    }
}

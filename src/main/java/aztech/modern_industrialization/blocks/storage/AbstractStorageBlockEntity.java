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
import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.blocks.WrenchableBlockEntity;
import aztech.modern_industrialization.transfer.LongResourceStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractStorageBlockEntity<T extends Resource> extends FastBlockEntity
        implements ResourceHandler<T>, WrenchableBlockEntity {
    protected T resource;
    protected long amount;
    private boolean isLocked;

    public final StorageBehaviour<T> behaviour;

    private final ResourceParticipant participant = new ResourceParticipant();

    public AbstractStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        resource = getEmptyResource();
        this.behaviour = ((AbstractStorageBlock<T>) state.getBlock()).behavior;
    }

    @Override
    protected boolean shouldSkipComparatorUpdate() {
        return behaviour.isCreative();
    }

    public void onChanged() {
        setChanged();
        if (!level.isClientSide())
            sync();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public boolean useWrench(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            var block = (AbstractStorageBlock) getBlockState().getBlock();
            // TODO 26.1
//            level.addFreshEntity(new ItemEntity(level, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, block.getStack(this)));
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
        } else {
            if (this.behaviour.isLockable()) {
                this.toggleLocked();
                player.sendOverlayMessage(isLocked() ? MIText.Locked.text() : MIText.Unlocked.text());
            }
        }
        return true;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmpty(resource);
        return !this.isLocked() || this.resource.equals(resource);
    }

    public int insert(int index, T resource, int maxAmount, TransactionContext transaction, boolean ignoreLock) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

        if (behaviour.isCreative()) {
            return 0;
        }

        if ((this.resource.isEmpty() && (ignoreLock || !this.isLocked())) || this.resource.equals(resource)) {
            int inserted = (int) Math.min(maxAmount, behaviour.getCapacityForResource(resource) - amount);
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
    public int insert(int index, T resource, int maxAmount, TransactionContext transaction) {
        return insert(index, resource, maxAmount, transaction, false);
    }

    @Override
    public int extract(int index, T resource, int maxAmount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

        if (behaviour.isCreative()) {
            return maxAmount;
        } else {
            if (resource.equals(this.resource)) {
                int extracted = (int) Math.min(maxAmount, amount);
                if (extracted > 0) {
                    participant.updateSnapshots(transaction);
                    amount -= extracted;
                    if (amount == 0 && !isLocked()) {
                        this.resource = getEmptyResource();
                    }
                }
                return extracted;
            }
            return 0;
        }
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return resource;
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty()) {
            return 0;
        }
        if (!behaviour.isCreative()) {
            return amount;
        } else {
            return Long.MAX_VALUE;
        }
    }

    public boolean isEmpty() {
        if (!behaviour.isCreative()) {
            return amount == 0;
        } else {
            return resource.isEmpty();
        }
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());
        // TODO 26.1: check handling of the empty resource
        return behaviour.getCapacityForResource(resource);
    }

    private class ResourceParticipant extends SnapshotJournal<LongResourceStack<T>> {
        @Override
        protected LongResourceStack<T> createSnapshot() {
            return new LongResourceStack<>(resource, amount);
        }

        @Override
        protected void revertToSnapshot(LongResourceStack<T> snapshot) {
            resource = snapshot.resource();
            amount = snapshot.amount();
        }

        @Override
        protected void onRootCommit(LongResourceStack<T> originalState) {
            onChanged();
        }
    }

    public boolean isLocked() {
        if (!behaviour.isLockable()) {
            return false;
        }
        return isLocked;
    }

    public void toggleLocked() {
        if (behaviour.isLockable()) {
            isLocked = !isLocked;
            if (!isLocked && amount == 0) {
                resource = getEmptyResource();
            }
            setChanged();
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);

        var storage = input.get(componentType());
        if (storage != null) {
            resource = storage.resource();
            if (behaviour.isLockable()) {
                isLocked = storage.locked();
            }
            if (!behaviour.isCreative()) {
                amount = storage.amount();
            }
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        builder.set(componentType(), new ResourceStorage<>(
                resource,
                amount,
                isLocked));
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);

        output.discard("locked");
        output.discard("amt");
    }

    @Override
    public void loadAdditional(ValueInput input) {
        resource = loadResource(input);

        if (behaviour.isLockable()) {
            isLocked = input.getBooleanOr("locked", false);
        }

        if (!behaviour.isCreative()) {
            amount = input.getLongOr("amt", 0);
            if (resource.isEmpty()) {
                amount = 0;
            }
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        if (behaviour.isLockable()) {
            output.putBoolean("locked", isLocked);
        }

        if (!behaviour.isCreative()) {
            output.putLong("amt", amount);
        }
        saveResource(resource, output);
    }

    public void setResource(T resource) {
        this.resource = resource;
    }

    public abstract DataComponentType<ResourceStorage<T>> componentType();

    public abstract T loadResource(ValueInput input);

    public abstract void saveResource(T resource, ValueOutput output);

    public abstract T getEmptyResource();
}

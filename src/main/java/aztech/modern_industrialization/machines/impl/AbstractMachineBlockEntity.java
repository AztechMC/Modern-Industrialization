package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.mixin_impl.WorldRendererGetter;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

/**
 * A generic machine BlockEntity.
 */
public abstract class AbstractMachineBlockEntity extends BlockEntity implements RenderAttachmentBlockEntity, BlockEntityClientSerializable {
    protected Direction facingDirection;
    protected Direction outputDirection;
    protected boolean isActive = false;

    protected AbstractMachineBlockEntity(BlockEntityType<?> blockEntityType, Direction facingDirection) {
        super(blockEntityType);
        this.facingDirection = facingDirection;
        this.outputDirection = null;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        facingDirection = Direction.byId(tag.getInt("facingDirection"));
        if(tag.contains("outputDirection")) {
            outputDirection = Direction.byId(tag.getInt("outputDirection"));
        }
        isActive = tag.getBoolean("isActive");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt("facingDirection", this.facingDirection.getId());
        if(outputDirection != null) {
            tag.putInt("outputDirection", this.outputDirection.getId());
        }
        tag.putBoolean("isActive", this.isActive);
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        setFacingDirection(Direction.byId(tag.getInt("facingDirection")));
        if(tag.contains("outputDirection")) {
            outputDirection = Direction.byId(tag.getInt("outputDirection"));
        }
        this.isActive = tag.getBoolean("isActive");
        ClientWorld clientWorld = (ClientWorld)world;
        WorldRendererGetter wrg = (WorldRendererGetter)clientWorld;
        wrg.modern_industrialization_getWorldRenderer().updateBlock(null, this.pos, null, null, 0);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("facingDirection", this.facingDirection.getId());
        if(outputDirection != null) {
            tag.putInt("outputDirection", this.outputDirection.getId());
        }
        tag.putBoolean("isActive", this.isActive);
        return tag;
    }

    public void setFacingDirection(Direction facingDirection) {
        this.facingDirection = facingDirection;
        markDirty();
        if(!world.isClient) {
            sync();
        }
    }

    public void setOutputDirection(Direction outputDirection) {
        this.outputDirection = outputDirection;
        markDirty();
        if(!world.isClient) {
            sync();
        }
    }

    @Override
    public Object getRenderAttachmentData() {
        return new AttachmentData(this);
    }

    public static class AttachmentData {
        public final Direction facingDirection;
        public final Direction outputDirection;
        public final boolean isActive;

        public AttachmentData(AbstractMachineBlockEntity blockEntity) {
            this.facingDirection = blockEntity.facingDirection;
            this.outputDirection = blockEntity.outputDirection;
            this.isActive = blockEntity.isActive;
        }
    }
}

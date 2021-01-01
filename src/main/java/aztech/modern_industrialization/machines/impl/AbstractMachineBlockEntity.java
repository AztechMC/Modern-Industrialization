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
package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.mixin_impl.WorldRendererGetter;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

/**
 * A generic machine_recipe BlockEntity.
 */
public abstract class AbstractMachineBlockEntity extends FastBlockEntity implements RenderAttachmentBlockEntity, BlockEntityClientSerializable {
    protected Direction facingDirection;
    protected Direction outputDirection;
    protected boolean extractItems = false;
    protected boolean extractFluids = false;
    protected boolean isActive = false;
    protected MachineModel casingOverride = null;

    protected AbstractMachineBlockEntity(BlockEntityType<?> blockEntityType, Direction facingDirection) {
        super(blockEntityType);
        this.facingDirection = facingDirection;
        this.outputDirection = hasOutput() ? facingDirection.getOpposite() : null;
    }

    public boolean hasOutput() {
        return true;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        facingDirection = Direction.byId(tag.getInt("facingDirection"));
        outputDirection = tag.contains("outputDirection") ? Direction.byId(tag.getInt("outputDirection")) : null;
        extractItems = tag.getBoolean("extractItems");
        extractFluids = tag.getBoolean("extractFluids");
        isActive = tag.getBoolean("isActive");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt("facingDirection", this.facingDirection.getId());
        if (outputDirection != null) {
            tag.putInt("outputDirection", this.outputDirection.getId());
        }
        tag.putBoolean("extractItems", this.extractItems);
        tag.putBoolean("extractFluids", this.extractFluids);
        tag.putBoolean("isActive", this.isActive);
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        setFacingDirection(Direction.byId(tag.getInt("facingDirection")));
        if (tag.contains("outputDirection")) {
            outputDirection = Direction.byId(tag.getInt("outputDirection"));
        }
        extractItems = tag.getBoolean("extractItems");
        extractFluids = tag.getBoolean("extractFluids");
        this.isActive = tag.getBoolean("isActive");
        ClientWorld clientWorld = (ClientWorld) world;
        WorldRendererGetter wrg = (WorldRendererGetter) clientWorld;
        wrg.modern_industrialization_getWorldRenderer().updateBlock(null, this.pos, null, null, 0);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("facingDirection", this.facingDirection.getId());
        if (outputDirection != null) {
            tag.putInt("outputDirection", this.outputDirection.getId());
        }
        tag.putBoolean("extractItems", this.extractItems);
        tag.putBoolean("extractFluids", this.extractFluids);
        tag.putBoolean("isActive", this.isActive);
        return tag;
    }

    public void setFacingDirection(Direction facingDirection) {
        this.facingDirection = facingDirection;
        markDirty();
        if (!world.isClient) {
            sync();
        }
    }

    public void setOutputDirection(Direction outputDirection) {
        this.outputDirection = outputDirection;
        markDirty();
        if (!world.isClient) {
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
        public final boolean extractItems;
        public final boolean extractFluids;
        public final boolean isActive;
        public final MachineModel casingOverride;

        public AttachmentData(AbstractMachineBlockEntity blockEntity) {
            this.facingDirection = blockEntity.facingDirection;
            this.outputDirection = blockEntity.outputDirection;
            this.extractItems = blockEntity.extractItems;
            this.extractFluids = blockEntity.extractFluids;
            this.isActive = blockEntity.isActive;
            this.casingOverride = blockEntity.casingOverride;
        }
    }
}

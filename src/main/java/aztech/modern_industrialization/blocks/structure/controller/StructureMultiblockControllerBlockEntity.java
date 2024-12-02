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
package aztech.modern_industrialization.blocks.structure.controller;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.blocks.structure.StructureMemberOverride;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockInputFormatters;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMember;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StructureMultiblockControllerBlockEntity extends FastBlockEntity implements StructureMemberOverride {
    private StructureControllerMode mode;

    private String inputId;

    private ResourceLocation id;
    private StructureControllerBounds bounds = new StructureControllerBounds(0, 0, 0, 1, 1, 1);
    private boolean showBounds;
    private boolean includeBlockEntities;

    private List<BlockPos> ignoreNBTPositions = List.of();

    public StructureMultiblockControllerBlockEntity(BlockPos pos, BlockState state) {
        super(MIRegistries.STRUCTURE_MULTIBLOCK_CONTROLLER_BE.get(), pos, state);
        mode = state.getValue(StructureMultiblockControllerBlock.MODE);
    }

    public StructureControllerMode getMode() {
        return mode;
    }

    public void setMode(StructureControllerMode mode) {
        Objects.requireNonNull(mode);
        this.mode = mode;
        this.updateBlockState();
    }

    @Nullable
    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
        id = StructureMultiblockInputFormatters.id(inputId);
    }

    @Nullable
    public ResourceLocation getId() {
        return id;
    }

    public StructureControllerBounds getBounds() {
        return bounds;
    }

    public void setBounds(StructureControllerBounds bounds) {
        Objects.requireNonNull(bounds);
        this.bounds = bounds;
    }

    public boolean shouldShowBounds() {
        return showBounds;
    }

    public void setShowBounds(boolean showBounds) {
        this.showBounds = showBounds;
    }

    public boolean includeBlockEntities() {
        return includeBlockEntities;
    }

    public void setIncludeBlockEntities(boolean includeBlockEntities) {
        this.includeBlockEntities = includeBlockEntities;
    }

    public List<BlockPos> getIgnoreNBTPositions() {
        return ignoreNBTPositions;
    }

    public void setIgnoreNBTPositions(List<BlockPos> ignoreNBTPositions) {
        Objects.requireNonNull(ignoreNBTPositions);
        this.ignoreNBTPositions = ignoreNBTPositions;
    }

    @Override
    public boolean isController() {
        return true;
    }

    @Override
    public StructureMember getMemberOverride() {
        return null;
    }

    @Override
    public boolean isConfigurationValid() {
        return !bounds.isEmpty();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("mode", mode.toString().toLowerCase(Locale.ROOT));
        if (inputId != null) {
            tag.putString("structure_id", inputId);
        }
        CompoundTag boundsTag = new CompoundTag();
        boundsTag.put("origin", NbtUtils.writeBlockPos(new BlockPos(bounds.x(), bounds.y(), bounds.z())));
        boundsTag.put("size", NbtUtils.writeBlockPos(new BlockPos(bounds.sizeX(), bounds.sizeY(), bounds.sizeZ())));
        tag.put("bounds", boundsTag);
        tag.putBoolean("show_bounds", showBounds);
        tag.putBoolean("include_block_entities", includeBlockEntities);
        NbtHelper.putBlockPosList(tag, "ignore_nbt_positions", ignoreNBTPositions);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mode = StructureControllerMode.valueOf(tag.getString("mode").toUpperCase(Locale.ROOT));
        this.setInputId(tag.getString("structure_id"));
        if (tag.contains("bounds", Tag.TAG_COMPOUND)) {
            CompoundTag boundsTag = tag.getCompound("bounds");
            BlockPos origin = NbtUtils.readBlockPos(boundsTag, "origin").orElseThrow();
            BlockPos size = NbtUtils.readBlockPos(boundsTag, "size").orElseThrow();
            bounds = new StructureControllerBounds(
                    origin.getX(), origin.getY(), origin.getZ(),
                    size.getX(), size.getY(), size.getZ());
        } else {
            bounds = new StructureControllerBounds(0, 0, 0, 1, 1, 1);
        }
        showBounds = tag.getBoolean("show_bounds");
        includeBlockEntities = tag.getBoolean("include_block_entities");
        List<BlockPos> ignoreNBTPositions = new ArrayList<>();
        NbtHelper.getBlockPosList(tag, "ignore_nbt_positions", ignoreNBTPositions);
        this.ignoreNBTPositions = ignoreNBTPositions;
        this.updateBlockState();
    }

    private void updateBlockState() {
        if (level != null) {
            BlockPos pos = getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.is(MIBlock.STRUCTURE_MULTIBLOCK_CONTROLLER.get())) {
                level.setBlock(pos, state.setValue(StructureMultiblockControllerBlock.MODE, mode), 2);
            }
        }
    }
}

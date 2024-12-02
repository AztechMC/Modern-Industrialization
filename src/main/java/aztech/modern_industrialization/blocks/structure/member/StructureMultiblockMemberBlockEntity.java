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
package aztech.modern_industrialization.blocks.structure.member;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.blocks.structure.StructureMemberOverride;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockInputFormatters;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureNBTMode;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMemberEntry;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StructureMultiblockMemberBlockEntity extends FastBlockEntity implements StructureMemberOverride {
    private StructureMemberMode mode;

    private String inputName;
    private String inputPreview;
    private String inputMembers;
    private String inputCasing;
    private String inputHatchFlags;

    private StructureMemberEntry preview;
    private List<StructureMemberTest> members;
    private StructureNBTMode nbtMode = StructureNBTMode.WEAK;
    private MachineCasing casing;
    private HatchFlags hatchFlags = HatchFlags.NO_HATCH;

    public StructureMultiblockMemberBlockEntity(BlockPos pos, BlockState state) {
        super(MIRegistries.STRUCTURE_MULTIBLOCK_MEMBER_BE.get(), pos, state);
        mode = state.getValue(StructureMultiblockMemberBlock.MODE);
    }

    public StructureMemberMode getMode() {
        return mode;
    }

    public void setMode(StructureMemberMode mode) {
        Objects.requireNonNull(mode);
        this.mode = mode;
        this.updateBlockState();
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    @Nullable
    public String getInputPreview() {
        return inputPreview;
    }

    public void setInputPreview(String inputPreview) {
        this.inputPreview = inputPreview;
        preview = StructureMultiblockInputFormatters.preview(inputPreview);
    }

    @Nullable
    public StructureMemberEntry getPreview() {
        return preview;
    }

    @Nullable
    public String getInputMembers() {
        return inputMembers;
    }

    public void setInputMembers(String inputMembers) {
        this.inputMembers = inputMembers;
        members = StructureMultiblockInputFormatters.members(inputMembers);
    }

    @Nullable
    public List<StructureMemberTest> getMembers() {
        return members;
    }

    public StructureNBTMode getNBTMode() {
        return nbtMode;
    }

    public void setNBTMode(StructureNBTMode nbtMode) {
        Objects.requireNonNull(nbtMode);
        this.nbtMode = nbtMode;
    }

    @Nullable
    public String getInputCasing() {
        return inputCasing;
    }

    public void setInputCasing(String inputCasing) {
        this.inputCasing = inputCasing;
        casing = StructureMultiblockInputFormatters.casing(inputCasing);
    }

    @Nullable
    public String getInputHatchFlags() {
        return inputHatchFlags;
    }

    public void setInputHatchFlags(String inputHatchFlags) {
        this.inputHatchFlags = inputHatchFlags;
        hatchFlags = StructureMultiblockInputFormatters.hatchFlags(inputHatchFlags);
    }

    @Nullable
    public MachineCasing getCasing() {
        return casing;
    }

    @Nullable
    public HatchFlags getHatchFlags() {
        return hatchFlags;
    }

    @Override
    public StructureMember getMemberOverride() {
        return switch (getMode()) {
        case HATCH -> StructureMember.hatch(preview, members, nbtMode, casing, hatchFlags);
        case SIMPLE -> StructureMember.simple(preview, members, nbtMode);
        case VARIABLE -> StructureMember.variable(inputName);
        };
    }

    @Override
    public boolean isConfigurationValid() {
        if (mode == StructureMemberMode.VARIABLE) {
            return inputName != null && !inputName.isEmpty();
        } else if (preview != null && members != null && !members.isEmpty()) {
            if (mode == StructureMemberMode.HATCH) {
                return casing != null && hatchFlags != null && !inputHatchFlags.isEmpty();
            }
            return true;
        }
        return false;
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
        if (inputName != null) {
            tag.putString("name", inputName);
        }
        if (inputPreview != null) {
            tag.putString("preview", inputPreview);
        }
        tag.putString("nbt_mode", StructureMultiblockInputFormatters.nbtMode(nbtMode));
        if (inputMembers != null) {
            tag.putString("members", inputMembers);
        }
        if (inputCasing != null) {
            tag.putString("casing", inputCasing);
        }
        if (inputHatchFlags != null) {
            tag.putString("hatch_flags", inputHatchFlags);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mode = StructureMemberMode.valueOf(tag.getString("mode").toUpperCase(Locale.ROOT));
        this.setInputName(tag.getString("name"));
        this.setInputPreview(tag.getString("preview"));
        this.setInputMembers(tag.getString("members"));
        nbtMode = StructureMultiblockInputFormatters.nbtMode(tag.getString("nbt_mode"), StructureNBTMode.WEAK);
        this.setInputCasing(tag.getString("casing"));
        this.setInputHatchFlags(tag.getString("hatch_flags"));
        this.updateBlockState();
    }

    private void updateBlockState() {
        if (level != null) {
            BlockPos pos = getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.is(MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.get())) {
                level.setBlock(pos, state.setValue(StructureMultiblockMemberBlock.MODE, mode), 2);
            }
        }
    }
}

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
package aztech.modern_industrialization.blocks.structure;

import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockFormatters;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMemberTest;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StructureMultiblockMemberBlockEntity extends FastBlockEntity implements StructureMemberOverride {
    private String inputPreview;
    private String inputMembers;

    private BlockState preview;
    private List<StructureMemberTest> members;

    public StructureMultiblockMemberBlockEntity(BlockPos pos, BlockState state) {
        super(MIRegistries.STRUCTURE_MULTIBLOCK_MEMBER_BE.get(), pos, state);
    }

    @Nullable
    public String getInputPreview() {
        return inputPreview;
    }

    public void setInputPreview(String inputPreview) {
        this.inputPreview = inputPreview;
        preview = StructureMultiblockFormatters.preview(inputPreview);
    }

    @Nullable
    public BlockState getPreview() {
        return preview;
    }

    @Nullable
    public String getInputMembers() {
        return inputMembers;
    }

    public void setInputMembers(String inputMembers) {
        this.inputMembers = inputMembers;
        members = StructureMultiblockFormatters.members(inputMembers);
    }

    @Nullable
    public List<StructureMemberTest> getMembers() {
        return members;
    }

    @Override
    public StructureMember getMemberOverride() {
        return new StructureMember(preview, members, null);
    }

    @Override
    public boolean isConfigurationValid() {
        return preview != null &&
                members != null && !members.isEmpty();
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
        if (inputPreview != null) {
            tag.putString("preview", inputPreview);
        }
        if (inputMembers != null) {
            tag.putString("members", inputMembers);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.setInputPreview(tag.getString("preview"));
        this.setInputMembers(tag.getString("members"));
    }
}

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
package aztech.modern_industrialization.machines.multiblocks.structure.member;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.blocks.structure.member.StructureMemberMode;
import aztech.modern_industrialization.blocks.structure.member.StructureMultiblockMemberBlock;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockInputFormatters;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class HatchStructureMember extends SimpleStructureMember {
    protected MachineCasing casing;
    protected HatchFlags hatchFlags;

    public HatchStructureMember(Supplier<BlockState> previewSupplier, List<StructureMemberTest> tests, MachineCasing casing, HatchFlags hatchFlags) {
        super(previewSupplier, tests);
        Objects.requireNonNull(casing);
        Objects.requireNonNull(hatchFlags);
        this.casing = casing;
        this.hatchFlags = hatchFlags;
    }

    public HatchStructureMember() {
    }

    public MachineCasing casing() {
        assertLoaded();
        return casing;
    }

    public HatchFlags hatchFlags() {
        assertLoaded();
        return hatchFlags;
    }

    @Override
    public String typeId() {
        return "hatch";
    }

    @Override
    public boolean isLoaded() {
        return super.isLoaded() && casing != null && hatchFlags != null;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (!tag.contains("casing", Tag.TAG_STRING) ||
                !tag.contains("hatch_flags", Tag.TAG_INT)) {
            throw new IllegalArgumentException("Invalid structure member format for type \"" + typeId() + "\": " + tag);
        }

        ResourceLocation casingId = ResourceLocation.tryParse(tag.getString("casing"));
        if (casingId == null || !MachineCasings.registeredCasings.containsKey(casingId)) {
            throw new IllegalArgumentException("Invalid structure member format for type \"" + typeId() + "\": " + tag);
        }
        casing = MachineCasings.get(casingId);

        int hatchFlagsValue = tag.getInt("hatch_flags");
        hatchFlags = hatchFlagsValue == 0 ? HatchFlags.NO_HATCH : new HatchFlags(hatchFlagsValue);
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);

        tag.putString("casing", casing.key.toString());

        tag.putInt("hatch_flags", hatchFlags.flags);
    }

    @Override
    public Optional<Pair<BlockState, FastBlockEntity>> asStructureBlock(BlockPos pos, boolean attempt) {
        assertLoaded();

        if (attempt) {
            var state = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.asBlock().defaultBlockState();
            state = state.setValue(StructureMultiblockMemberBlock.MODE, StructureMemberMode.HATCH);
            var be = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.get().newBlockEntity(pos, state);
            be.setInputPreview(StructureMultiblockInputFormatters.preview(getPreviewState()));
            be.setInputMembers(StructureMultiblockInputFormatters.members(tests));
            be.setInputCasing(StructureMultiblockInputFormatters.casing(casing));
            be.setInputFlags(StructureMultiblockInputFormatters.hatchFlags(hatchFlags));
            return Optional.of(Pair.of(state, be));
        }

        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        assertLoaded();
        if (o instanceof HatchStructureMember other && this.getClass() == other.getClass()) {
            other.assertLoaded();
            return this.getPreviewState() == other.getPreviewState() &&
                    tests.containsAll(other.tests) && other.tests.containsAll(tests) &&
                    casing.equals(other.casing) &&
                    hatchFlags.equals(other.hatchFlags);
        }
        return false;
    }
}

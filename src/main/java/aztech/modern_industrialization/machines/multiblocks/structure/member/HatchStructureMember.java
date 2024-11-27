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
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockInputFormatters;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import aztech.modern_industrialization.util.MIExtraCodecs;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class HatchStructureMember extends SimpleStructureMember {
    public static final MapCodec<HatchStructureMember> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    MIExtraCodecs.LAZY_BLOCK_STATE.fieldOf("preview").forGetter(member -> member.previewSupplier),
                    StructureMemberTest.CODEC.listOf().fieldOf("tests").forGetter(HatchStructureMember::tests),
                    MachineCasing.CODEC.fieldOf("casing").forGetter(HatchStructureMember::casing),
                    HatchFlags.CODEC.fieldOf("hatch_flags").forGetter(HatchStructureMember::hatchFlags))
            .apply(instance, HatchStructureMember::new));

    private final MachineCasing casing;
    private final HatchFlags hatchFlags;

    public HatchStructureMember(Supplier<BlockState> previewSupplier, List<StructureMemberTest> tests, MachineCasing casing, HatchFlags hatchFlags) {
        super(previewSupplier, tests);
        Objects.requireNonNull(casing);
        Objects.requireNonNull(hatchFlags);
        this.casing = casing;
        this.hatchFlags = hatchFlags;
    }

    public MachineCasing casing() {
        return casing;
    }

    public HatchFlags hatchFlags() {
        return hatchFlags;
    }

    @Override
    public StructureMemberType<?> type() {
        return StructureMemberType.HATCH;
    }

    @Override
    public Optional<Pair<BlockState, FastBlockEntity>> asStructureBlock(BlockPos pos, boolean required) {
        if (required) {
            var state = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.asBlock().defaultBlockState();
            state = state.setValue(StructureMultiblockMemberBlock.MODE, StructureMemberMode.HATCH);
            var be = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.get().newBlockEntity(pos, state);
            be.setInputPreview(StructureMultiblockInputFormatters.preview(getPreviewState()));
            be.setInputMembers(StructureMultiblockInputFormatters.members(tests));
            be.setInputCasing(StructureMultiblockInputFormatters.casing(casing));
            be.setInputHatchFlags(StructureMultiblockInputFormatters.hatchFlags(hatchFlags));
            return Optional.of(Pair.of(state, be));
        }

        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HatchStructureMember other && this.getClass() == other.getClass()) {
            return this.getPreviewState() == other.getPreviewState() &&
                    tests.containsAll(other.tests) && other.tests.containsAll(tests) &&
                    casing.equals(other.casing) &&
                    hatchFlags.equals(other.hatchFlags);
        }
        return false;
    }
}

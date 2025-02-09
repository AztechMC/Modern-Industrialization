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
import aztech.modern_industrialization.machines.multiblocks.structure.StructureNBTMode;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class HatchStructureMember extends SimpleStructureMember {
    public static final MapCodec<HatchStructureMember> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    StructureMemberEntry.CODEC.fieldOf("preview").forGetter(member -> member.preview),
                    StructureMemberTest.CODEC.listOf().fieldOf("tests").forGetter(HatchStructureMember::tests),
                    StructureNBTMode.CODEC.optionalFieldOf("nbt_mode")
                            .forGetter(member -> member.nbtMode() != StructureNBTMode.WEAK ? Optional.of(member.nbtMode()) : Optional.empty()),
                    MachineCasing.CODEC.fieldOf("casing").forGetter(HatchStructureMember::casing),
                    HatchFlags.CODEC.fieldOf("hatch_flags").forGetter(HatchStructureMember::hatchFlags))
            .apply(instance, (preview, tests, nbtMode, casing, hatchFlags) -> new HatchStructureMember(preview, tests,
                    nbtMode.orElse(StructureNBTMode.WEAK), casing, hatchFlags)));

    private final MachineCasing casing;
    private final HatchFlags hatchFlags;

    public HatchStructureMember(StructureMemberEntry preview, List<StructureMemberTest> tests, StructureNBTMode nbtMode, MachineCasing casing,
            HatchFlags hatchFlags) {
        super(preview, tests, nbtMode);
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
    public Optional<Pair<BlockState, @Nullable FastBlockEntity>> asStructureBlock(BlockPos pos, boolean required) {
        if (required) {
            var state = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.asBlock().defaultBlockState();
            state = state.setValue(StructureMultiblockMemberBlock.MODE, StructureMemberMode.HATCH);
            var be = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.get().newBlockEntity(pos, state);
            be.setInputPreview(StructureMultiblockInputFormatters.preview(preview));
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
            return preview.equals(other.preview) &&
                    tests.containsAll(other.tests) && other.tests.containsAll(tests) &&
                    casing.equals(other.casing) &&
                    hatchFlags.equals(other.hatchFlags);
        }
        return false;
    }
}

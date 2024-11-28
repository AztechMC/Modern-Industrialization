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

import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.structure.MIStructureTemplateManager;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StateStructureMemberTest;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import aztech.modern_industrialization.util.MIExtraCodecs;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

public final class LiteralStructureMember extends SimpleStructureMember {
    public static final MapCodec<LiteralStructureMember> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    MIExtraCodecs.LAZY_BLOCK_STATE.fieldOf("state").forGetter(member -> member.preview),
                    CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(member -> Optional.ofNullable(member.nbt())))
            .apply(instance, (preview, nbt) -> new LiteralStructureMember(preview, nbt.orElse(null))));

    private final CompoundTag nbt;

    public LiteralStructureMember(Lazy<BlockState> previewSupplier, @Nullable CompoundTag nbt) {
        super(previewSupplier, List.of(new StateStructureMemberTest(previewSupplier)));
        this.nbt = nbt;
    }

    @Nullable
    public CompoundTag nbt() {
        return nbt != null ? nbt.copy() : null;
    }

    @Override
    public StructureMemberType<?> type() {
        return StructureMemberType.LITERAL;
    }

    @Override
    public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity) {
        CompoundTag beTag = MIStructureTemplateManager.maybeTag(blockEntity);
        for (StructureMemberTest test : tests) {
            if (test.matchesState(state) && NbtUtils.compareNbt(nbt, beTag, true)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Pair<BlockState, FastBlockEntity>> asStructureBlock(BlockPos pos, boolean required) {
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LiteralStructureMember other && this.getClass() == other.getClass()) {
            return this.getPreviewState() == other.getPreviewState() &&
                    tests.containsAll(other.tests) && other.tests.containsAll(tests) &&
                    NbtUtils.compareNbt(nbt, other.nbt, true);
        }
        return false;
    }
}

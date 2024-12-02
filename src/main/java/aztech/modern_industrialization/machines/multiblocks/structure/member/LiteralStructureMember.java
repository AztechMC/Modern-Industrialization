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
import aztech.modern_industrialization.machines.multiblocks.structure.StructureNBTMode;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StateStructureMemberTest;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class LiteralStructureMember extends SimpleStructureMember {
    public static final MapCodec<LiteralStructureMember> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    StructureMemberEntry.CODEC.fieldOf("value").forGetter(LiteralStructureMember::preview),
                    StructureNBTMode.CODEC.optionalFieldOf("nbt_mode")
                            .forGetter(member -> member.nbtMode() == StructureNBTMode.WEAK ? Optional.of(member.nbtMode()) : Optional.empty()))
            .apply(instance, (value, mode) -> new LiteralStructureMember(value, mode.orElse(StructureNBTMode.STRONG))));

    public LiteralStructureMember(StructureMemberEntry preview, StructureNBTMode nbtMode) {
        super(preview, List.of(new StateStructureMemberTest(preview)), nbtMode);
    }

    @Override
    public StructureMemberType<?> type() {
        return StructureMemberType.LITERAL;
    }

    @Override
    public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity) {
        return tests.getFirst().matchesState(state, blockEntity, nbtMode);
    }

    @Override
    public Optional<Pair<BlockState, FastBlockEntity>> asStructureBlock(BlockPos pos, boolean required) {
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LiteralStructureMember other && this.getClass() == other.getClass()) {
            return preview.equals(other.preview);
        }
        return false;
    }
}

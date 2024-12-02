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

import aztech.modern_industrialization.util.MIExtraCodecs;
import aztech.modern_industrialization.util.NbtHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

public record StructureMemberEntry(Lazy<BlockState> state, @Nullable CompoundTag nbt) {
    public static final Codec<StructureMemberEntry> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    MIExtraCodecs.LAZY_BLOCK_STATE.fieldOf("state").forGetter(StructureMemberEntry::state),
                    CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(entry -> Optional.ofNullable(entry.nbt())))
            .apply(instance, (state, nbt) -> new StructureMemberEntry(state, nbt.orElse(null))));

    public StructureMemberEntry(BlockState state) {
        this(Lazy.of(() -> state), null);
    }

    public StructureMemberEntry(BlockStateParser.BlockResult result) {
        this(Lazy.of(result::blockState), result.nbt());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StructureMemberEntry other) {
            return state == other.state &&
                    NbtHelper.equals(nbt, other.nbt);
        }
        return false;
    }
}

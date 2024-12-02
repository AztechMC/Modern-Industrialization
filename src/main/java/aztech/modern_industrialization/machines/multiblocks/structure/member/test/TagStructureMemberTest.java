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
package aztech.modern_industrialization.machines.multiblocks.structure.member.test;

import aztech.modern_industrialization.machines.multiblocks.structure.StructureNBTMode;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class TagStructureMemberTest extends StructureMemberTest {
    public static final MapCodec<TagStructureMemberTest> CODEC = TagKey.codec(Registries.BLOCK)
            .xmap(TagStructureMemberTest::new, TagStructureMemberTest::blockTag).fieldOf("tag");

    private final TagKey<Block> blockTag;

    public TagStructureMemberTest(TagKey<Block> blockTag) {
        Objects.requireNonNull(blockTag);
        this.blockTag = blockTag;
    }

    public TagKey<Block> blockTag() {
        return blockTag;
    }

    @Override
    public StructureMemberTestType<?> type() {
        return StructureMemberTestType.TAG;
    }

    @Override
    public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity, StructureNBTMode mode) {
        return state.is(blockTag);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TagStructureMemberTest other) {
            return blockTag == other.blockTag ||
                    blockTag.location().equals(other.blockTag.location());
        }
        return false;
    }
}

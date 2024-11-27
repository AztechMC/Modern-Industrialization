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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TagStructureMemberTest extends StructureMemberTest {
    public static final MapCodec<TagStructureMemberTest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(TagStructureMemberTest::blockTag))
            .apply(instance, TagStructureMemberTest::new));

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
    public boolean matchesState(BlockState state) {
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

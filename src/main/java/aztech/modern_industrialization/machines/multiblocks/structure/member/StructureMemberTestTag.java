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

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class StructureMemberTestTag implements StructureMemberTest {
    private TagKey<Block> blockTag;

    public StructureMemberTestTag(TagKey<Block> blockTag) {
        this.blockTag = blockTag;
    }

    public StructureMemberTestTag() {
        this(null);
    }

    public TagKey<Block> blockTag() {
        return blockTag;
    }

    @Override
    public String typeId() {
        return "tag";
    }

    @Override
    public boolean matchesState(BlockState state) {
        return blockTag != null && state.is(blockTag);
    }

    @Override
    public void load(CompoundTag tag) {
        blockTag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(tag.getString("tag")));
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putString("tag", blockTag.location().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StructureMemberTestTag other) {
            return blockTag == other.blockTag ||
                    (blockTag != null && other.blockTag != null && blockTag.location().equals(other.blockTag.location()));
        }
        return false;
    }
}

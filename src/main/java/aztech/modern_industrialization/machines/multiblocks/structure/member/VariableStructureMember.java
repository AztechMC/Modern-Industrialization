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
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

public class VariableStructureMember extends StructureMember {
    protected String name;

    public VariableStructureMember(String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    public VariableStructureMember() {
    }

    public String name() {
        assertLoaded();
        return name;
    }

    @Override
    public String typeId() {
        return "variable";
    }

    @Override
    public boolean isLoaded() {
        return name != null;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (!tag.contains("name", Tag.TAG_STRING)) {
            throw new IllegalArgumentException("Invalid structure member format for type \"" + typeId() + "\": " + tag);
        }

        name = tag.getString("name");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Invalid structure member format for type \"" + typeId() + "\": " + tag);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);

        tag.putString("name", name);
    }

    @Override
    public Optional<Pair<BlockState, FastBlockEntity>> asStructureBlock(BlockPos pos, boolean required) {
        assertLoaded();

        var state = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.asBlock().defaultBlockState();
        state = state.setValue(StructureMultiblockMemberBlock.MODE, StructureMemberMode.VARIABLE);
        var be = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.get().newBlockEntity(pos, state);
        be.setInputName(name);

        return Optional.of(Pair.of(state, be));
    }

    @Override
    public boolean matchesState(BlockState state) {
        throw new UnsupportedOperationException("Tried to use a variable structure member without replacing it in the template.");
    }

    @Override
    public BlockState getPreviewState() {
        throw new UnsupportedOperationException("Tried to use a variable structure member without replacing it in the template.");
    }

    @Override
    public boolean equals(Object o) {
        assertLoaded();
        if (o instanceof VariableStructureMember other && this.getClass() == other.getClass()) {
            other.assertLoaded();
            return name.equals(other.name);
        }
        return false;
    }
}

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
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StateStructureMemberTest;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public interface StructureMember extends SimpleMember {
    String typeId();

    boolean isLoaded();

    default void assertLoaded() {
        if (!isLoaded()) {
            throw new IllegalStateException("Member is not loaded");
        }
    }

    void load(CompoundTag tag);

    default void save(CompoundTag tag) {
        assertLoaded();
    }

    Optional<Pair<BlockState, FastBlockEntity>> asStructureBlock(BlockPos pos, boolean attempt);

    static SimpleStructureMember simple(Supplier<BlockState> preview, List<StructureMemberTest> tests) {
        return new SimpleStructureMember(preview, tests);
    }

    static SimpleStructureMember literal(Supplier<BlockState> blockState) {
        return simple(blockState, List.of(new StateStructureMemberTest(blockState)));
    }

    static HatchStructureMember hatch(Supplier<BlockState> preview, List<StructureMemberTest> tests, MachineCasing casing, HatchFlags hatchFlags) {
        return new HatchStructureMember(preview, tests, casing, hatchFlags);
    }

    static VariableStructureMember variable(String name) {
        return new VariableStructureMember(name);
    }

    static StructureMember from(CompoundTag tag) {
        if (!tag.contains("type", CompoundTag.TAG_STRING)) {
            throw new IllegalArgumentException("Invalid structure member format: " + tag);
        }
        String type = tag.getString("type");
        StructureMember member = switch (type) {
        case "simple" -> new SimpleStructureMember();
        case "hatch" -> new HatchStructureMember();
        case "variable" -> new VariableStructureMember();
        default -> throw new IllegalStateException("Unexpected value: " + type);
        };
        member.load(tag);
        return member;
    }
}

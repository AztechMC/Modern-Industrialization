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

import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

public record StructureMember(BlockState preview, List<StructureMemberTest> tests, HatchFlags hatchFlags) implements SimpleMember {
    public StructureMember(BlockState state) {
        this(state, List.of(new StructureMemberTestState(state)), null);
    }

    @Override
    public boolean matchesState(BlockState state) {
        for (StructureMemberTest test : tests) {
            if (test.matchesState(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState getPreviewState() {
        return preview;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StructureMember other) {
            return preview == other.preview() &&
                    tests.containsAll(other.tests()) && other.tests().containsAll(tests) &&
                    Objects.equals(hatchFlags, other.hatchFlags());
        }
        return false;
    }

    public void save(CompoundTag tag) {
        tag.put("preview", NbtUtils.writeBlockState(preview));

        ListTag testsTag = new ListTag();
        for (StructureMemberTest test : tests) {
            CompoundTag testTag = new CompoundTag();
            testTag.putString("id", test.id());
            test.save(testTag);
            testsTag.add(testTag);
        }
        tag.put("tests", testsTag);

        if (hatchFlags != null) {
            tag.putInt("hatch_flags", hatchFlags.flags);
        }
    }

    public static StructureMember from(CompoundTag tag) {
        if (tag.isEmpty() ||
                !tag.contains("preview", Tag.TAG_COMPOUND) ||
                !tag.contains("tests", Tag.TAG_LIST)) {
            return null;
        }

        var registry = BuiltInRegistries.BLOCK.asLookup();

        BlockState preview = NbtUtils.readBlockState(registry, tag.getCompound("preview"));

        ListTag testsTag = tag.getList("tests", Tag.TAG_COMPOUND);
        List<StructureMemberTest> tests = new ArrayList<>();
        for (int i = 0; i < testsTag.size(); i++) {
            CompoundTag testTag = testsTag.getCompound(i);
            StructureMemberTest test = StructureMemberTest.from(testTag);
            if (test != null) {
                tests.add(test);
            }
        }
        if (tests.isEmpty()) {
            return null;
        }

        int hatchFlagsValue = tag.getInt("hatch_flags");
        HatchFlags hatchFlags = null;
        if (hatchFlagsValue != 0) {
            hatchFlags = new HatchFlags(hatchFlagsValue);
        }

        return new StructureMember(preview, tests, hatchFlags);
    }
}

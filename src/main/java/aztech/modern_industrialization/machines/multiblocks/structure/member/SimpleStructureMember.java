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
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockInputFormatters;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleStructureMember implements StructureMember {
    protected Supplier<BlockState> previewSupplier;
    protected List<StructureMemberTest> tests;

    private BlockState preview;

    public SimpleStructureMember(Supplier<BlockState> previewSupplier, List<StructureMemberTest> tests) {
        Objects.requireNonNull(previewSupplier);
        Objects.requireNonNull(tests);
        this.previewSupplier = previewSupplier;
        this.tests = tests;
    }

    public SimpleStructureMember() {
    }

    public List<StructureMemberTest> tests() {
        if (!isLoaded()) {
            throw new IllegalStateException("Member is not loaded");
        }
        return Collections.unmodifiableList(tests);
    }

    @Override
    public String typeId() {
        return "simple";
    }

    @Override
    public boolean isLoaded() {
        return previewSupplier != null && tests != null;
    }

    @Override
    public void load(CompoundTag tag) {
        Objects.requireNonNull(tag);
        if (!tag.contains("preview", CompoundTag.TAG_COMPOUND) ||
                !tag.contains("tests", CompoundTag.TAG_LIST)) {
            throw new IllegalArgumentException("Invalid structure member format for type \"" + typeId() + "\": " + tag);
        }

        previewSupplier = () -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("preview"));

        ListTag testsTag = tag.getList("tests", Tag.TAG_COMPOUND);
        if (testsTag.isEmpty()) {
            throw new IllegalArgumentException("Member cannot have no tests: " + tag);
        }
        tests = new ArrayList<>();
        for (int i = 0; i < testsTag.size(); i++) {
            CompoundTag testTag = testsTag.getCompound(i);
            StructureMemberTest test = StructureMemberTest.from(testTag);
            if (test != null) {
                tests.add(test);
            }
        }
    }

    @Override
    public void save(CompoundTag tag) {
        Objects.requireNonNull(tag);
        StructureMember.super.save(tag);

        tag.put("preview", NbtUtils.writeBlockState(getPreviewState()));

        ListTag testsTag = new ListTag();
        for (StructureMemberTest test : tests) {
            CompoundTag testTag = new CompoundTag();
            testTag.putString("type", test.typeId());
            test.save(testTag);
            testsTag.add(testTag);
        }
        if (testsTag.isEmpty()) {
            throw new IllegalArgumentException("Member cannot have no tests");
        }
        tag.put("tests", testsTag);
    }

    @Override
    public Optional<Pair<BlockState, FastBlockEntity>> asStructureBlock(BlockPos pos, boolean attempt) {
        assertLoaded();

        if (attempt && tests.size() > 1) {
            var state = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.asBlock().defaultBlockState();
            state = state.setValue(StructureMultiblockMemberBlock.MODE, StructureMemberMode.SIMPLE);
            var be = MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.get().newBlockEntity(pos, state);
            be.setInputPreview(StructureMultiblockInputFormatters.preview(getPreviewState()));
            be.setInputMembers(StructureMultiblockInputFormatters.members(tests));
            return Optional.of(Pair.of(state, be));
        }

        return Optional.empty();
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
        if (preview == null) {
            preview = previewSupplier.get();
            if (preview == null) {
                preview = Blocks.AIR.defaultBlockState();
            }
        }
        return preview;
    }

    @Override
    public boolean equals(Object o) {
        assertLoaded();
        if (o instanceof SimpleStructureMember other && this.getClass() == other.getClass()) {
            other.assertLoaded();
            return this.getPreviewState() == other.getPreviewState() &&
                    tests.containsAll(other.tests) && other.tests.containsAll(tests);
        }
        return false;
    }
}

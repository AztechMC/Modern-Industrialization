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

import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class StructureMember implements SimpleMember {
    private final String name;
    private final Supplier<BlockState> previewSupplier;
    private final List<StructureMemberTest> tests;
    private final MachineCasing casing;
    private final HatchFlags hatchFlags;

    private BlockState preview;

    private StructureMember(@Nullable String name, @Nullable Supplier<BlockState> preview, List<StructureMemberTest> tests,
            @Nullable MachineCasing casing, @Nullable HatchFlags hatchFlags) {
        this.name = name;
        this.previewSupplier = preview;
        this.tests = tests;
        this.casing = casing;
        this.hatchFlags = hatchFlags;
    }

    public static StructureMember literal(Supplier<BlockState> state) {
        return new StructureMember(null, state, List.of(new StructureMemberTestState(state)), null, null);
    }

    public static StructureMember variable(String name) {
        Objects.requireNonNull(name);
        return new StructureMember(name, null, List.of(), null, null);
    }

    public static StructureMember simple(Supplier<BlockState> preview, List<StructureMemberTest> tests) {
        Objects.requireNonNull(preview);
        Objects.requireNonNull(tests);
        return new StructureMember(null, preview, tests, null, null);
    }

    public static StructureMember hatch(Supplier<BlockState> preview, List<StructureMemberTest> tests, MachineCasing casing, HatchFlags hatchFlags) {
        Objects.requireNonNull(preview);
        Objects.requireNonNull(tests);
        Objects.requireNonNull(casing);
        Objects.requireNonNull(hatchFlags);
        return new StructureMember(null, preview, tests, casing, hatchFlags);
    }

    public String name() {
        return name;
    }

    public BlockState preview() {
        if (previewSupplier == null) {
            return null;
        }
        if (preview == null) {
            preview = previewSupplier.get();
        }
        return preview;
    }

    public List<StructureMemberTest> tests() {
        return Collections.unmodifiableList(tests);
    }

    @Nullable
    public MachineCasing casing() {
        return casing;
    }

    @Nullable
    public HatchFlags hatchFlags() {
        return hatchFlags;
    }

    @Override
    public boolean matchesState(BlockState state) {
        if (name != null) {
            throw new IllegalStateException("Tried to use a structure member with a name without replacing it in the template.");
        }
        for (StructureMemberTest test : tests) {
            if (test.matchesState(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState getPreviewState() {
        if (name != null) {
            throw new IllegalStateException("Tried to use a structure member with a name without replacing it in the template.");
        }
        return this.preview() == null ? Blocks.AIR.defaultBlockState() : this.preview();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StructureMember other) {
            return Objects.equals(name, other.name) &&
                    this.preview() == other.preview() &&
                    tests.containsAll(other.tests) && other.tests.containsAll(tests) &&
                    Objects.equals(casing, other.casing) &&
                    Objects.equals(hatchFlags, other.hatchFlags);
        }
        return false;
    }

    public void save(CompoundTag tag) {
        if (name != null) {
            tag.putString("name", name);
        }

        BlockState preview = this.preview();
        if (preview != null) {
            tag.put("preview", NbtUtils.writeBlockState(preview));
        }

        ListTag testsTag = new ListTag();
        for (StructureMemberTest test : tests) {
            CompoundTag testTag = new CompoundTag();
            testTag.putString("type", test.typeId());
            test.save(testTag);
            testsTag.add(testTag);
        }
        if (!testsTag.isEmpty()) {
            tag.put("tests", testsTag);
        }

        if (casing != null) {
            tag.putString("casing", casing.key.toString());
        }

        if (hatchFlags != null) {
            tag.putInt("hatch_flags", hatchFlags.flags);
        }
    }

    public static StructureMember from(CompoundTag tag) {
        if (tag.isEmpty()) {
            return null;
        }

        String name = tag.contains("name", Tag.TAG_STRING) ? tag.getString("name") : null;

        Supplier<BlockState> preview = null;
        if (tag.contains("preview", Tag.TAG_COMPOUND)) {
            preview = () -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("preview"));
        }

        ListTag testsTag = tag.getList("tests", Tag.TAG_COMPOUND);
        List<StructureMemberTest> tests = new ArrayList<>();
        for (int i = 0; i < testsTag.size(); i++) {
            CompoundTag testTag = testsTag.getCompound(i);
            StructureMemberTest test = StructureMemberTest.from(testTag);
            if (test != null) {
                tests.add(test);
            }
        }

        MachineCasing casing = null;
        if (tag.contains("casing", Tag.TAG_STRING)) {
            ResourceLocation casingId = ResourceLocation.tryParse(tag.getString("casing"));
            if (casingId == null || !MachineCasings.registeredCasings.containsKey(casingId)) {
                return null;
            }
            casing = MachineCasings.get(casingId);
        }

        int hatchFlagsValue = tag.getInt("hatch_flags");
        HatchFlags hatchFlags = null;
        if (hatchFlagsValue != 0) {
            hatchFlags = new HatchFlags(hatchFlagsValue);
        }

        return new StructureMember(name, preview, tests, casing, hatchFlags);
    }
}

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

import aztech.modern_industrialization.machines.multiblocks.structure.MIStructureTemplateManager;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureNBTMode;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMemberEntry;
import aztech.modern_industrialization.util.NbtHelper;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class StateStructureMemberTest extends StructureMemberTest {
    public static final MapCodec<StateStructureMemberTest> CODEC = StructureMemberEntry.CODEC
            .xmap(StateStructureMemberTest::new, StateStructureMemberTest::entry).fieldOf("value");

    private final StructureMemberEntry entry;

    public StateStructureMemberTest(StructureMemberEntry entry) {
        Objects.requireNonNull(entry);
        this.entry = entry;
    }

    public StructureMemberEntry entry() {
        return entry;
    }

    public BlockState blockState() {
        return entry.state().get();
    }

    public CompoundTag nbt() {
        return entry.nbt() != null ? entry.nbt().copy() : null;
    }

    @Override
    public StructureMemberTestType<?> type() {
        return StructureMemberTestType.STATE;
    }

    @Override
    public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity, StructureNBTMode mode) {
        CompoundTag nbt = entry.nbt();
        CompoundTag beNbt = MIStructureTemplateManager.maybeTag(blockEntity);
        return this.blockState() == state
                && (nbt == null || mode.isIgnore() || (mode.isStrong() ? NbtHelper.equals(nbt, beNbt) : NbtHelper.compare(nbt, beNbt)));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StateStructureMemberTest other) {
            return entry.equals(other.entry);
        }
        return false;
    }
}

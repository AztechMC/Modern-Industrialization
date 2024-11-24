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

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class StateStructureMemberTest extends StructureMemberTest {
    private Supplier<BlockState> blockStateSupplier;

    private BlockState blockState;

    public StateStructureMemberTest(Supplier<BlockState> blockState) {
        Objects.requireNonNull(blockState);
        this.blockStateSupplier = blockState;
    }

    public StateStructureMemberTest() {
    }

    public BlockState blockState() {
        assertLoaded();
        if (blockState == null) {
            blockState = blockStateSupplier.get();
            if (blockState == null) {
                blockState = Blocks.AIR.defaultBlockState();
            }
        }
        return blockState;
    }

    @Override
    public String typeId() {
        return "state";
    }

    @Override
    public boolean matchesState(BlockState state) {
        assertLoaded();
        return this.blockState() == state;
    }

    @Override
    public boolean isLoaded() {
        return blockStateSupplier != null;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (!tag.contains("state", Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("Invalid structure member test format for type \"" + typeId() + "\": " + tag);
        }

        blockStateSupplier = () -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("state"));
        blockState = null;
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);

        tag.put("state", NbtUtils.writeBlockState(this.blockState()));
    }

    @Override
    public boolean equals(Object o) {
        assertLoaded();
        if (o instanceof StateStructureMemberTest other) {
            other.assertLoaded();
            return this.blockState() == other.blockState();
        }
        return false;
    }
}

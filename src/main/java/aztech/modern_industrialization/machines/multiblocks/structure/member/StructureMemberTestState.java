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

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

public class StructureMemberTestState implements StructureMemberTest {
    private Supplier<BlockState> blockStateSupplier;

    private BlockState blockState;

    public StructureMemberTestState(Supplier<BlockState> blockState) {
        this.blockStateSupplier = blockState;
    }

    public StructureMemberTestState() {
        this(null);
    }

    private BlockState blockState() {
        if (blockState == null) {
            blockState = blockStateSupplier.get();
        }
        return blockState;
    }

    @Override
    public String id() {
        return "state";
    }

    @Override
    public boolean matchesState(BlockState state) {
        return this.blockState() == state;
    }

    @Override
    public void load(CompoundTag tag) {
        blockStateSupplier = () -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("state"));
        blockState = null;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.put("state", NbtUtils.writeBlockState(this.blockState()));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StructureMemberTestState other) {
            return this.blockState() == other.blockState();
        }
        return false;
    }
}

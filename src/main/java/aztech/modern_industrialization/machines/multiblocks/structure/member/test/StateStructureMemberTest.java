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

import aztech.modern_industrialization.util.MIExtraCodecs;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class StateStructureMemberTest extends StructureMemberTest {
    public static final MapCodec<StateStructureMemberTest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    MIExtraCodecs.LAZY_BLOCK_STATE.fieldOf("state").forGetter(test -> test.blockStateSupplier))
            .apply(instance, StateStructureMemberTest::new));

    private final Supplier<BlockState> blockStateSupplier;

    private BlockState blockState;

    public StateStructureMemberTest(Supplier<BlockState> blockStateSupplier) {
        Objects.requireNonNull(blockStateSupplier);
        this.blockStateSupplier = blockStateSupplier;
    }

    public BlockState blockState() {
        if (blockState == null) {
            blockState = blockStateSupplier.get();
            if (blockState == null) {
                blockState = Blocks.AIR.defaultBlockState();
            }
        }
        return blockState;
    }

    @Override
    public StructureMemberTestType<?> type() {
        return StructureMemberTestType.STATE;
    }

    @Override
    public boolean matchesState(BlockState state) {
        return this.blockState() == state;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StateStructureMemberTest other) {
            return this.blockState() == other.blockState();
        }
        return false;
    }
}

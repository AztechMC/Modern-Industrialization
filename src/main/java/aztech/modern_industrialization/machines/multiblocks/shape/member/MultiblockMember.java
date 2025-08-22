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
package aztech.modern_industrialization.machines.multiblocks.shape.member;

import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.shape.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.shape.test.BlockMultiblockMemberTest;
import aztech.modern_industrialization.machines.multiblocks.shape.test.MultiblockMemberTest;
import aztech.modern_industrialization.machines.multiblocks.shape.test.StateMultiblockMemberTest;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

// TODO SWEDZ MULTIBLOCKS: remove methods that become irrelevant once structures have been implemented fully and we are
//  done making multiblocks using code
public abstract class MultiblockMember {
    public static SimpleMultiblockMember simple(MultiblockMemberState preview, List<MultiblockMemberTest> tests) {
        return new SimpleMultiblockMember(preview, tests);
    }

    public static SimpleMultiblockMember simple(Supplier<? extends Block> block) {
        return simple(new MultiblockMemberState(Lazy.of(() -> block.get().defaultBlockState())),
                List.of(new BlockMultiblockMemberTest(Lazy.of(block))));
    }

    public static SimpleMultiblockMember simple(ResourceLocation blockId) {
        return simple(() -> BuiltInRegistries.BLOCK.get(blockId));
    }

    public static SimpleMultiblockMember simple(BlockState state) {
        var memberState = new MultiblockMemberState(Lazy.of(() -> state));
        return simple(memberState, List.of(new StateMultiblockMemberTest(memberState)));
    }

    public static SimpleMultiblockMember verticalChain() {
        var chainState = new MultiblockMemberState(
                Lazy.of(() -> Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)));
        var chainStateWaterlogged = new MultiblockMemberState(Lazy.of(
                () -> Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y).setValue(ChainBlock.WATERLOGGED, true)));
        return MultiblockMember.simple(chainState, List.of(new StateMultiblockMemberTest(List.of(chainState, chainStateWaterlogged))));
    }

    public static HatchMultiblockMember hatch(MultiblockMemberState preview, List<MultiblockMemberTest> tests, MachineCasing casing,
            HatchFlags hatchFlags) {
        return new HatchMultiblockMember(preview, tests, casing, hatchFlags);
    }

    public static HatchMultiblockMember hatch(SimpleMultiblockMember parent, MachineCasing casing, HatchFlags hatchFlags) {
        return new HatchMultiblockMember(parent.preview(), parent.tests(), casing, hatchFlags);
    }

    public abstract void forceLoad();

    // TODO SWEDZ MULTIBLOCKS: account for nbt
    public abstract boolean matchesState(BlockState state);

    public abstract MultiblockMemberState getPreviewState();

    /**
     * It is required that {@link Object#equals(Object)} be implemented so that codecs can reliably identify
     * differences between types.
     */
    public abstract boolean equals(Object o);
}

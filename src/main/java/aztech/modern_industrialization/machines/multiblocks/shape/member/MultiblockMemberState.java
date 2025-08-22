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

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

/**
 * <p>
 * A specific member state represented as a {@link BlockState}. Used for previews in the hologram and recipe
 * category, as well as for representing such data in a string for users to write/read.
 * </p>
 *
 * <p>
 * The {@link BlockState} is stored lazily so that instances can be created before all blocks are registered. This
 * allows us to load multiblock structure files on startup without any problems.
 * </p>
 */
public record MultiblockMemberState(Lazy<BlockState> lazyState) {
    public MultiblockMemberState(BlockState state) {
        this(Lazy.of(() -> state));
    }

    public MultiblockMemberState(BlockStateParser.BlockResult result) {
        this(Lazy.of(result::blockState));
    }

    public BlockState state() {
        return lazyState.get();
    }

    // TODO SWEDZ MULTIBLOCKS: account for nbt + rotation
    // TODO SWEDZ MULTIBLOCKS: boolean field for if we want the structure block
    public void setBlock(Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, this.state());
    }
}

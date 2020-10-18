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
package aztech.modern_industrialization.machines.impl.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

public class MultiblockShapes {
    public static MultiblockShape.Entry block(Block block) {
        return new MultiblockShape.Entry() {
            @Override
            public boolean matches(BlockView world, BlockPos pos) {
                return world.getBlockState(pos).isOf(block);
            }

            @Override
            public Text getErrorMessage() {
                return new TranslatableText("text.modern_industrialization.shape_error_block", new TranslatableText(block.getTranslationKey()));
            }
        };
    }

    public static MultiblockShape.Entry verticalChain() {
        return new MultiblockShape.Entry() {
            @Override
            public boolean matches(BlockView world, BlockPos pos) {
                return world.getBlockState(pos).isOf(Blocks.CHAIN) && world.getBlockState(pos).get(PillarBlock.AXIS) == Direction.Axis.Y;
            }

            @Override
            public Text getErrorMessage() {
                return new TranslatableText("text.modern_industrialization.shape_error_vertical_chain");
            }
        };
    }

    public static MultiblockShape.Entry blockId(Identifier id) {
        return new MultiblockShape.Entry() {
            @Override
            public boolean matches(BlockView world, BlockPos pos) {
                return Registry.BLOCK.getId(world.getBlockState(pos).getBlock()).equals(id);
            }

            @Override
            public Text getErrorMessage() {
                Block block = Registry.BLOCK.get(id);
                return new TranslatableText("text.modern_industrialization.shape_error_block", new TranslatableText(block.getTranslationKey()));
            }
        };
    }

    public static final int HATCH_FLAG_ITEM_INPUT = 1;
    public static final int HATCH_FLAG_ITEM_OUTPUT = 1 << 1;
    public static final int HATCH_FLAG_FLUID_INPUT = 1 << 2;
    public static final int HATCH_FLAG_FLUID_OUTPUT = 1 << 3;
    public static final int HATCH_FLAG_ENERGY_INPUT = 1 << 4;
    public static final int HATCH_FLAG_ENERGY_OUTPUT = 1 << 5;

    public static MultiblockShape.Entry hatch(int hatchesFlag) {
        return new MultiblockShape.Entry() {
            @Override
            public boolean matches(BlockView world, BlockPos pos) {
                if (world.getBlockEntity(pos) instanceof HatchBlockEntity) {
                    HatchBlockEntity entity = (HatchBlockEntity) world.getBlockEntity(pos);
                    return entity.isUnlinked() && (hatchesFlag & (1 << entity.type.getId())) > 0;
                }
                return false;
            }

            @Override
            public Text getErrorMessage() {
                return new TranslatableText("text.modern_industrialization.shape_error_hatch", writeHatchTypes(hatchesFlag));
            }
        };
    }

    private static Text writeHatchTypes(int hatchesFlag) {
        MutableText text = new LiteralText("");
        for (int i = 0; i < 6; ++i) {
            if ((hatchesFlag & (1 << i)) > 0) {
                if (i != 0) {
                    text.append(new TranslatableText("text.modern_industrialization.shape_error_hatch_separator"));
                }
                text.append(new TranslatableText("text.modern_industrialization.shape_error_hatch_" + i));
            }
        }
        return text;
    }

    public static MultiblockShape.Entry or(MultiblockShape.Entry entry1, MultiblockShape.Entry entry2) {
        return new MultiblockShape.Entry() {
            @Override
            public boolean matches(BlockView world, BlockPos pos) {
                return entry1.matches(world, pos) || entry2.matches(world, pos);
            }

            @Override
            public Text getErrorMessage() {
                return new TranslatableText("text.modern_industrialization.shape_error_or", entry1.getErrorMessage(), entry2.getErrorMessage());
            }
        };
    }
}

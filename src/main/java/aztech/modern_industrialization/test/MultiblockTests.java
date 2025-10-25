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

package aztech.modern_industrialization.test;

import aztech.modern_industrialization.machines.blockentities.multiblocks.LargeTankMultiblockBlockEntity;
import aztech.modern_industrialization.machines.init.MultiblockHatches;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.test.framework.MIGameTest;
import aztech.modern_industrialization.test.framework.MIGameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class MultiblockTests {
    @MIGameTest
    public void largeTankEmitsRedstone(MIGameTestHelper helper) {
        var controllerPos = new BlockPos(3, 1, 2);
        helper.setBlock(controllerPos, MultiblockMachines.LARGE_TANK.asBlock());

        LargeTankMultiblockBlockEntity largeTank = helper.getBlockEntity(controllerPos);
        largeTank.createShapeMatcher().buildMultiblock(helper.getLevel());

        var comparatorPos0 = new BlockPos(3, 1, 1);
        helper.setBlock(comparatorPos0.below(), Blocks.STONE);
        helper.setBlock(comparatorPos0, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, Direction.SOUTH));

        var hatchPos1 = new BlockPos(3, 0, 4);
        helper.setBlock(hatchPos1, MultiblockHatches.LARGE_TANK.asBlock());
        var comparatorPos1 = new BlockPos(3, 0, 5);
        helper.setBlock(comparatorPos1, Blocks.COMPARATOR);

        var hatchPos2 = new BlockPos(4, 0, 2);
        helper.setBlock(hatchPos2, MultiblockHatches.LARGE_TANK.asBlock());
        var comparatorPos2 = new BlockPos(5, 0, 2);
        helper.setBlock(comparatorPos2, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, Direction.WEST));
        var redstonePos = new BlockPos(6, 0, 2);
        helper.setBlock(redstonePos, Blocks.REDSTONE_WIRE);

        helper.startSequence()
                .thenIdle(1) // formation time
                .thenExecute(() -> {
                    helper.assertBlockProperty(comparatorPos0, ComparatorBlock.POWERED, false);
                    helper.assertBlockProperty(comparatorPos1, ComparatorBlock.POWERED, false);
                    helper.assertBlockProperty(comparatorPos2, ComparatorBlock.POWERED, false);
                    helper.assertBlockProperty(redstonePos, RedStoneWireBlock.POWER, 0);
                })
                .thenExecute(() -> {
                    largeTank.getExposedFluidHandler().fill(new FluidStack(Fluids.WATER, 1), IFluidHandler.FluidAction.EXECUTE);
                })
                .thenIdle(3) // redstone update time (gametests run after the level tick so the delay is 1 more than usual)
                .thenExecute(() -> {
                    helper.assertBlockProperty(comparatorPos0, ComparatorBlock.POWERED, true);
                    helper.assertBlockProperty(comparatorPos1, ComparatorBlock.POWERED, true);
                    helper.assertBlockProperty(comparatorPos2, ComparatorBlock.POWERED, true);
                    helper.assertBlockProperty(redstonePos, RedStoneWireBlock.POWER, 1);
                })
                .thenExecute(() -> {
                    largeTank.getExposedFluidHandler().fill(new FluidStack(Fluids.WATER, Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
                })
                .thenIdle(3) // redstone update time
                .thenExecute(() -> {
                    helper.assertBlockProperty(comparatorPos0, ComparatorBlock.POWERED, true);
                    helper.assertBlockProperty(comparatorPos1, ComparatorBlock.POWERED, true);
                    helper.assertBlockProperty(comparatorPos2, ComparatorBlock.POWERED, true);
                    helper.assertBlockProperty(redstonePos, RedStoneWireBlock.POWER, 15);
                })
                .thenExecute(() -> {
                    helper.setBlock(hatchPos1.above(2), Blocks.AIR);
                })
                .thenIdle(3) // redstone update time
                .thenExecute(() -> {
                    helper.assertBlockProperty(comparatorPos0, ComparatorBlock.POWERED, false);
                    helper.assertBlockProperty(comparatorPos1, ComparatorBlock.POWERED, false);
                    helper.assertBlockProperty(comparatorPos2, ComparatorBlock.POWERED, false);
                    helper.assertBlockProperty(redstonePos, RedStoneWireBlock.POWER, 0);
                })
                .thenSucceed();
    }
}

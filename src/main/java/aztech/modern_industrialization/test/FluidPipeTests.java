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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.blocks.storage.tank.TankBlockEntity;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkNode;
import aztech.modern_industrialization.test.framework.MIGameTest;
import aztech.modern_industrialization.test.framework.MIGameTestHelper;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;

public class FluidPipeTests {
    private final PipeNetworkType fluidPipe = PipeNetworkType.get(MI.id("fluid_pipe"));

    @MIGameTest
    public void testSinglePipeThroughput(MIGameTestHelper helper) {
        helper.creativeTank(new BlockPos(0, 1, 0), Fluids.WATER);
        var tankToFill = new BlockPos(2, 1, 0);
        helper.emptyTank(tankToFill);
        helper.pipe(new BlockPos(1, 1, 0), fluidPipe, pipe -> {
            pipe.addInConnection(Direction.EAST);
            pipe.addOutConnection(Direction.WEST);
        });
        helper.startSequence()
                .thenIdle(1)
                .thenExecute(() -> {
                    helper.assertFluid(tankToFill, Fluids.WATER, 1000);
                })
                .thenSucceed();
    }

    @MIGameTest
    public void testDoublePipeThroughput(MIGameTestHelper helper) {
        helper.creativeTank(new BlockPos(0, 1, 0), Fluids.WATER);
        var tankToFill = new BlockPos(2, 1, 0);
        helper.emptyTank(tankToFill);
        helper.pipe(new BlockPos(1, 1, 0), fluidPipe, pipe -> {
            pipe.addInConnection(Direction.EAST);
            pipe.addOutConnection(Direction.WEST);
        });
        helper.pipe(new BlockPos(1, 2, 0), fluidPipe, pipe -> {
        });
        helper.startSequence()
                .thenIdle(1)
                .thenExecute(() -> {
                    helper.assertFluid(tankToFill, Fluids.WATER, 2000);
                })
                .thenSucceed();
    }

    @MIGameTest
    public void testTankExtendedThroughput(MIGameTestHelper helper) {
        helper.creativeTank(new BlockPos(0, 1, 0), Fluids.WATER);
        var tankToFill = new BlockPos(2, 1, 0);
        helper.emptyTank(tankToFill);
        var extensionTank = new BlockPos(1, 2, 0);
        helper.emptyTank(extensionTank, MIMaterials.BRONZE);
        var pipePos = new BlockPos(1, 1, 0);
        helper.pipe(pipePos, fluidPipe, pipe -> {
            pipe.addInConnection(Direction.EAST);
            pipe.addOutConnection(Direction.WEST);
            pipe.addInOutConnection(Direction.UP);
        });
        helper.startSequence()
                .thenIdle(1)
                .thenExecute(() -> {
                    helper.assertFluid(tankToFill, Fluids.WATER, 5000);
                    helper.assertNoFluid(extensionTank);
                })
                .thenExecute(() -> {
                    // Replace by a bronze tank (with 4000 of capacity).
                    helper.emptyTank(tankToFill, MIMaterials.BRONZE);
                })
                .thenIdle(1)
                .thenExecute(() -> {
                    helper.assertFluid(tankToFill, Fluids.WATER, 4000);
                    helper.assertFluid(extensionTank, Fluids.WATER, 800);
                    var node = (FluidNetworkNode) helper.getPipeNode(pipePos, fluidPipe);
                    if (node.getAmount() != 200) {
                        helper.fail("Expected 200 fluid in pipe, got " + node.getAmount(), pipePos);
                    }
                })
                .thenExecute(() -> {
                    helper.destroyBlock(tankToFill);
                    helper.emptyTank(tankToFill, MIMaterials.BRONZE);
                    // Replace by lava locked tank to make sure it gets ignored
                    helper.destroyBlock(extensionTank);
                    helper.emptyTank(extensionTank, MIMaterials.BRONZE);
                    var tank = (TankBlockEntity) helper.getBlockEntity(extensionTank);
                    try (var tx = Transaction.openOuter()) {
                        // hacky way to toggle lock :P
                        tank.insert(FluidVariant.of(Fluids.LAVA), 1000, tx);
                        tank.toggleLocked();
                    }
                })
                .thenIdle(1)
                .thenExecute(() -> {
                    helper.assertFluid(tankToFill, Fluids.WATER, 1000);
                    helper.assertNoFluid(extensionTank);
                })
                .thenSucceed();
    }
}

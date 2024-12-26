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
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.test.framework.MIGameTest;
import aztech.modern_industrialization.test.framework.MIGameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;

public class FluidPipeTests {
    private final PipeNetworkType fluidPipe = PipeNetworkType.get(MI.id("fluid_pipe"));

    @MIGameTest
    public void testSinglePipeThroughput(MIGameTestHelper helper) {
        helper.creativeTank(new BlockPos(0, 1, 0), Fluids.WATER);
        helper.emptyTank(new BlockPos(2, 1, 0));
        helper.pipe(new BlockPos(1, 1, 0), fluidPipe, pipe -> {
            pipe.addConnection(Direction.EAST);
            pipe.addConnection(Direction.WEST);
            // Switch WEST to OUT
            pipe.removeConnection(Direction.WEST);
            pipe.removeConnection(Direction.WEST);
        });
        helper.startSequence()
                .thenIdle(1)
                .thenExecute(() -> {
                    helper.assertFluid(new BlockPos(2, 1, 0), Fluids.WATER, 1000);
                })
                .thenSucceed();
    }

    @MIGameTest
    public void testDoublePipeThroughput(MIGameTestHelper helper) {
        helper.creativeTank(new BlockPos(0, 1, 0), Fluids.WATER);
        helper.emptyTank(new BlockPos(2, 1, 0));
        helper.pipe(new BlockPos(1, 1, 0), fluidPipe, pipe -> {
            pipe.addConnection(Direction.EAST);
            pipe.addConnection(Direction.WEST);
            // Switch WEST to OUT
            pipe.removeConnection(Direction.WEST);
            pipe.removeConnection(Direction.WEST);
        });
        helper.pipe(new BlockPos(1, 2, 0), fluidPipe, pipe -> {
        });
        helper.startSequence()
                .thenIdle(1)
                .thenExecute(() -> {
                    helper.assertFluid(new BlockPos(2, 1, 0), Fluids.WATER, 2000);
                })
                .thenSucceed();
    }
}

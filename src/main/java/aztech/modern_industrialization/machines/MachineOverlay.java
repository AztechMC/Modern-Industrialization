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

package aztech.modern_industrialization.machines;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MachineOverlay {
    private static final double SIDE = 0.25;
    public static final double[] ZONES = new double[] { 0, SIDE, 1 - SIDE, 1 };

    public static final List<VoxelShape> OVERLAY_SHAPES = new ArrayList<>();
    public static final List<List<Direction>> TOUCHING_DIRECTIONS = new ArrayList<>();

    static {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                for (int k = 0; k < 3; ++k) {
                    List<Direction> directions = new ArrayList<>();
                    OVERLAY_SHAPES.add(Shapes.box(ZONES[i], ZONES[j], ZONES[k], ZONES[i + 1], ZONES[j + 1], ZONES[k + 1]));
                    if (i == 0)
                        directions.add(Direction.WEST);
                    if (i == 2)
                        directions.add(Direction.EAST);
                    if (j == 0)
                        directions.add(Direction.DOWN);
                    if (j == 2)
                        directions.add(Direction.UP);
                    if (k == 0)
                        directions.add(Direction.NORTH);
                    if (k == 2)
                        directions.add(Direction.SOUTH);
                    TOUCHING_DIRECTIONS.add(directions);
                }
            }
        }
    }

    public static Vec3 getPosInBlock(BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        return blockHitResult.getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static int findHitIndex(Vec3 posInBlock) {
        for (int i = 0; i < MachineOverlay.OVERLAY_SHAPES.size(); i++) {
            AABB box = MachineOverlay.OVERLAY_SHAPES.get(i).bounds();
            // move slightly towards box center
            Vec3 dir = box.getCenter().subtract(posInBlock).normalize().scale(1e-4);
            if (box.contains(posInBlock.add(dir))) {
                return i;
            }
        }
        // hit from really weird direction - default to center of machine
        return 1 * 3 * 3 + 1 * 3 + 1;
    }

    public static Direction findHitSide(Vec3 posInBlock, Direction hitFace) {
        int i = findHitIndex(posInBlock);
        List<Direction> shapeDirections = MachineOverlay.TOUCHING_DIRECTIONS.get(i);
        if (shapeDirections.size() == 1) {
            return hitFace; // center
        } else if (shapeDirections.size() == 3) {
            return hitFace.getOpposite(); // corner
        } else {
            // one of the sides. There are 2 directions, so it must be the other one
            for (Direction direction : shapeDirections) {
                if (direction != hitFace) {
                    return direction;
                }
            }
        }
        // hit from inside of machine - default to center
        return hitFace;
    }

    public static Direction findHitSide(BlockHitResult bhr) {
        return findHitSide(getPosInBlock(bhr), bhr.getDirection());
    }
}

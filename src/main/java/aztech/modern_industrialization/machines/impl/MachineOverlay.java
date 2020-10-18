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
package aztech.modern_industrialization.machines.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

class MachineOverlay {
    private static final double SIDE = 0.25;

    static final List<VoxelShape> OVERLAY_SHAPES = new ArrayList<>();
    static final List<List<Direction>> TOUCHING_DIRECTIONS = new ArrayList<>();
    static {
        double[] p = new double[] { 0, SIDE, 1 - SIDE, 1 };
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                for (int k = 0; k < 3; ++k) {
                    List<Direction> directions = new ArrayList<>();
                    OVERLAY_SHAPES.add(VoxelShapes.cuboid(p[i], p[j], p[k], p[i + 1], p[j + 1], p[k + 1]));
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
}

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

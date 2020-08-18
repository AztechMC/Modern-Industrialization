package aztech.modern_industrialization.pipes.impl;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

/**
 * Reusing the PipePartBuilder to generate shapes for the pipe parts.
 */
public class PipeShapeBuilder extends PipePartBuilder {
    private VoxelShape shape;

    PipeShapeBuilder(int slotPos, Direction direction) {
        super(null, slotPos, direction, null);
        shape = VoxelShapes.empty();
    }

    @Override
    void noConnection() {
        // don't draw the single face, it would crash.
    }

    @Override
    protected void drawPipe(float length) {
        Vec3d up = up();
        addShape(pos.add(up.multiply(SIDE / 2)).add(right.multiply(SIDE / 2)), pos.subtract(up.multiply(SIDE / 2)).subtract(right.multiply(SIDE / 2)).add(facing.multiply(length)));
    }

    /**
     * Add a shape to the current shape using two corners.
     */
    private void addShape(Vec3d c1, Vec3d c2) {
        double x = Math.min(c1.x, c2.x), y = Math.min(c1.y, c2.y), z = Math.min(c1.z, c2.z);
        double X = Math.max(c1.x, c2.x), Y = Math.max(c1.y, c2.y), Z = Math.max(c1.z, c2.z);
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(x, y, z, X, Y, Z));
    }

    /**
     * Retrieve the built shape.
     */
    VoxelShape getShape() {
        return shape;
    }

    /**
     * Draw the center connector (starting from whatever direction).
     */
    public void centerConnector() {
        moveForward(-SIDE);
        drawPipe(SIDE);
    }
}

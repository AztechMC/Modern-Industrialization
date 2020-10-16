package aztech.modern_industrialization.pipes.impl;

import static net.minecraft.util.math.Direction.*;

import aztech.modern_industrialization.pipes.api.PipeConnectionType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * A class that can build pipe model parts using a simple interface.
 */
abstract class PipePartBuilder {
    /**
     * The width of a pipe.
     */
    static final float SIDE = 2.0f / 16;
    /**
     * The spacing between two pipes.
     */
    protected static final float SPACING = 1.0f / 16;
    /**
     * The distance between the side of the block and the first of the five pipe
     * slots.
     */
    protected static final float FIRST_POS = (1.0f - 5 * SIDE - 4 * SPACING) / 2;
    Vec3d pos;
    Vec3d facing;
    Vec3d right;

    PipePartBuilder(int slotPos, Direction direction) {
        this.facing = Vec3d.of(direction.getVector());
        // initial position + half pipe + slotPos * width
        float position = (1.0f - 3 * SIDE - 2 * SPACING) / 2.0f + SIDE / 2.0f + slotPos * (SIDE + SPACING);
        this.pos = new Vec3d(position, position, position);
        // Find a suitable right direction (both right and up must face inside of the
        // block).
        for (Direction d : Direction.values()) {
            this.right = Vec3d.of(d.getVector());
            if (isTowardsInside(this.right) && isTowardsInside(up()))
                break;
        }
        // Move out of the center cube.
        moveForward(SIDE / 2);
    }

    /**
     * Find out whether the axis direction is far from the sides of the block.
     */
    protected boolean isTowardsInside(Vec3d direction) {
        return distanceToSide(direction) > 0.5f - 1e-6;
    }

    /**
     * Get the distance along some axis direction to the nearest block side.
     */
    protected float distanceToSide(Vec3d direction) {
        float p = (float) direction.dotProduct(pos);
        if (p > 0) {
            return 1 - p;
        } else {
            return -p;
        }
    }

    abstract void drawPipe(float length);

    /**
     * Draw a 5-sided pipe.
     */
    protected void drawPipeWithEnd(float length) {
        drawPipe(length);
        moveForward(length);
        noConnection();
        moveForward(-length);
    }

    /**
     * Move forward.
     */
    void moveForward(float amount) {
        this.pos = this.pos.add(this.facing.multiply(amount));
    }

    /**
     * Get up vector.
     */
    Vec3d up() {
        return right.crossProduct(facing);
    }

    /**
     * Rotate clockwise around the facing axis.
     */
    protected void rotateCw() {
        right = up().multiply(-1);
    }

    /**
     * Turn 90° up.
     */
    protected void turnUp() {
        facing = up();
    }

    /**
     * Draw a single face.
     */
    abstract void noConnection();

    /**
     * Draw a straight line.
     */
    void straightLine() {
        drawPipeWithEnd(distanceToSide(facing));
    }

    /**
     * Draw a short bend.
     */
    void shortBend() {
        // horizontal
        float dist = FIRST_POS + 2 * SIDE + SPACING;
        float advDist = distanceToSide(facing) - dist;
        drawPipeWithEnd(advDist + SIDE);
        moveForward(advDist + SIDE / 2);
        turnUp();
        // vertical
        moveForward(SIDE / 2);
        drawPipeWithEnd(SPACING + SIDE);
        moveForward(SPACING + SIDE / 2);
        rotateCw();
        turnUp();
        // perpendicular
        moveForward(SIDE / 2);
        drawPipeWithEnd(SPACING + SIDE);
        moveForward(SPACING + SIDE / 2);
        rotateCw();
        turnUp();
        // again vertical
        moveForward(SIDE / 2);
        drawPipeWithEnd(distanceToSide(facing));
    }

    /**
     * Draw a short bend, on the extra slot.
     */
    void farShortBend() {
        // horizontal
        float dist = FIRST_POS + SIDE;
        float advDist = distanceToSide(facing) - dist;
        drawPipeWithEnd(advDist + SIDE);
        moveForward(advDist + SIDE / 2);
        turnUp();
        // vertical
        moveForward(SIDE / 2);
        drawPipeWithEnd(SPACING + SIDE);
        moveForward(SPACING + SIDE / 2);
        rotateCw();
        turnUp();
        // perpendicular
        moveForward(SIDE / 2);
        drawPipeWithEnd(SPACING + SIDE);
        moveForward(SPACING + SIDE / 2);
        rotateCw();
        turnUp();
        // again vertical
        moveForward(SIDE / 2);
        drawPipeWithEnd(distanceToSide(facing));
    }

    /**
     * Draw a long bend.
     */
    void longBend() {
        // horizontal
        float dist = FIRST_POS + SIDE;
        float advDist = distanceToSide(facing) - dist;
        drawPipeWithEnd(advDist + SIDE);
        moveForward(advDist + SIDE / 2);
        turnUp();
        // vertical
        moveForward(SIDE / 2);
        drawPipeWithEnd(2 * SPACING + 2 * SIDE);
        moveForward(2 * SPACING + 1.5f * SIDE);
        rotateCw();
        turnUp();
        // perpendicular
        moveForward(SIDE / 2);
        drawPipeWithEnd(2 * SPACING + 2 * SIDE);
        moveForward(2 * SPACING + 1.5f * SIDE);
        rotateCw();
        turnUp();
        // again vertical
        moveForward(SIDE / 2);
        drawPipeWithEnd(distanceToSide(facing));
    }

    public static int getSlotPos(int slot) {
        return slot == 0 ? 1 : slot == 1 ? 0 : 2;
    }

    /**
     * Get the type of a connection.
     */
    static int getRenderType(int slot, Direction direction, PipeConnectionType[][] connections) {
        if (connections[slot][direction.getId()] == null) {
            return 0;
        } else {
            int connSlot = 0;
            for (int i = 0; i < slot; i++) {
                if (connections[i][direction.getId()] != null) {
                    connSlot++;
                }
            }
            if (slot == 1) {
                // short bend
                if (connSlot == 0) {
                    return 2;
                }
            } else if (slot == 2) {
                if (connSlot == 0) {
                    // short bend, but far if the direction is west to avoid collisions in some
                    // cases.
                    return direction == WEST ? 3 : 2;
                } else if (connSlot == 1) {
                    // long bend
                    return 4;
                }
            }
            // default to straight line
            return 1;
        }
    }
}

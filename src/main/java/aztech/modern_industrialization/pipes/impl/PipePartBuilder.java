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

package aztech.modern_industrialization.pipes.impl;

import static net.minecraft.core.Direction.*;

import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

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
    Vec3 pos;
    Vec3 facing;
    Vec3 right;

    PipePartBuilder(int slotPos, Direction direction) {
        this.facing = Vec3.atLowerCornerOf(direction.getNormal());
        // initial position + half pipe + slotPos * width
        float position = (1.0f - 3 * SIDE - 2 * SPACING) / 2.0f + SIDE / 2.0f + slotPos * (SIDE + SPACING);
        this.pos = new Vec3(position, position, position);
        // Find a suitable right direction (both right and up must face inside of the
        // block).
        for (Direction d : Direction.values()) {
            this.right = Vec3.atLowerCornerOf(d.getNormal());
            if (isTowardsInside(this.right) && isTowardsInside(up()))
                break;
        }
        // Move out of the center cube.
        moveForward(SIDE / 2);
    }

    /**
     * Find out whether the axis direction is far from the sides of the block.
     */
    protected boolean isTowardsInside(Vec3 direction) {
        return distanceToSide(direction) > 0.5f - 1e-6;
    }

    /**
     * Get the distance along some axis direction to the nearest block side.
     */
    protected float distanceToSide(Vec3 direction) {
        float p = (float) direction.dot(pos);
        if (p > 0) {
            return 1 - p;
        } else {
            return -p;
        }
    }

    /**
     * Draw a 5-sided pipe.
     */
    protected final void drawPipe(float length, Intent intent) {
        drawPipe(length, intent, true);
    }

    /**
     * Draw a pipe.
     */
    abstract void drawPipe(float length, Intent intent, boolean end);

    /**
     * Move forward.
     */
    void moveForward(float amount) {
        this.pos = this.pos.add(this.facing.scale(amount));
    }

    /**
     * Get up vector.
     */
    Vec3 up() {
        return right.cross(facing);
    }

    /**
     * Rotate clockwise around the facing axis.
     */
    protected void rotateCw() {
        right = up().scale(-1);
    }

    /**
     * Turn 90° up.
     */
    protected void turnUp() {
        facing = up();
    }

    /**
     * Draw a straight line.
     */
    void straightLine(boolean reduced, boolean end) {
        if (reduced)
            moveForward(SIDE + SPACING);
        drawPipe(distanceToSide(facing), Intent.STRAIGHT, end);
    }

    /**
     * Draw a short bend.
     */
    void shortBend(boolean reduced, boolean end) {
        if (reduced)
            moveForward(SIDE + SPACING);
        // horizontal
        float dist = FIRST_POS + 2 * SIDE + SPACING;
        float advDist = distanceToSide(facing) - dist;
        boolean bendConflicting = advDist + SIDE < 0;
        drawPipe(advDist + SIDE, Intent.BEND);
        moveForward(advDist + SIDE / 2);
        turnUp();
        rotateCw();
        // vertical
        moveForward(SIDE / 2);
        drawPipe(SPACING + SIDE, bendConflicting ? Intent.BEND_CONFLICTING : Intent.BEND, !bendConflicting);
        moveForward(SPACING + SIDE / 2);
        turnUp();
        rotateCw();
        // perpendicular
        moveForward(SIDE / 2);
        drawPipe(SPACING + SIDE, Intent.BEND);
        moveForward(SPACING + SIDE / 2);
        turnUp();
        // again vertical
        moveForward(SIDE / 2);
        drawPipe(distanceToSide(facing), Intent.STRAIGHT, end);
    }

    /**
     * Draw a short bend, on the extra slot.
     */
    void farShortBend(boolean reduced, boolean end) {
        if (reduced)
            moveForward(SIDE + SPACING);
        // horizontal
        float dist = FIRST_POS + SIDE;
        float advDist = distanceToSide(facing) - dist;
        drawPipe(advDist + SIDE, Intent.BEND);
        moveForward(advDist + SIDE / 2);
        turnUp();
        rotateCw();
        // vertical
        moveForward(SIDE / 2);
        drawPipe(SPACING + SIDE, Intent.BEND);
        moveForward(SPACING + SIDE / 2);
        turnUp();
        rotateCw();
        // perpendicular
        moveForward(SIDE / 2);
        drawPipe(SPACING + SIDE, Intent.BEND);
        moveForward(SPACING + SIDE / 2);
        turnUp();
        // again vertical
        moveForward(SIDE / 2);
        drawPipe(distanceToSide(facing), Intent.STRAIGHT, end);
    }

    /**
     * Draw a long bend.
     */
    void longBend(boolean reduced, boolean end) {
        if (reduced)
            moveForward(SIDE + SPACING);
        // horizontal
        float dist = FIRST_POS + SIDE;
        float advDist = distanceToSide(facing) - dist;
        drawPipe(advDist + SIDE, Intent.BEND);
        moveForward(advDist + SIDE / 2);
        turnUp();
        rotateCw();
        // vertical
        moveForward(SIDE / 2);
        drawPipe(2 * SPACING + 2 * SIDE, Intent.BEND);
        moveForward(2 * SPACING + 1.5f * SIDE);
        turnUp();
        rotateCw();
        // perpendicular
        moveForward(SIDE / 2);
        drawPipe(2 * SPACING + 2 * SIDE, Intent.BEND);
        moveForward(2 * SPACING + 1.5f * SIDE);
        turnUp();
        // again vertical
        moveForward(SIDE / 2);
        drawPipe(distanceToSide(facing), Intent.STRAIGHT, end);
    }

    public static int getSlotPos(int slot) {
        return slot == 0 ? 1 : slot == 1 ? 0 : 2;
    }

    /**
     * Get the type of a connection.
     */
    static int getRenderType(int logicalSlot, Direction direction, PipeEndpointType[][] connections) {
        if (connections[logicalSlot][direction.get3DDataValue()] == null) {
            // no connection
            return 0;
        } else if (connections[logicalSlot][direction.get3DDataValue()] != PipeEndpointType.PIPE) {
            // straight line when connecting to a block
            return 1;
        } else {
            int connSlot = 0;
            for (int i = 0; i < logicalSlot; i++) {
                if (connections[i][direction.get3DDataValue()] != null) {
                    connSlot++;
                }
            }
            if (logicalSlot == 1) {
                // short bend
                if (connSlot == 0) {
                    return 2;
                }
            } else if (logicalSlot == 2) {
                if (connSlot == 0) {
                    // short bend, but far if the direction is negative to avoid collisions in some cases.
                    return direction.getAxisDirection() == AxisDirection.NEGATIVE ? 3 : 2;
                } else if (connSlot == 1) {
                    // long bend
                    return 4;
                }
            }
            // default to straight line
            return 1;
        }
    }

    /**
     * Get the initial direction of a connection.
     */
    static Direction getInitialDirection(int logicalSlot, Direction connectionDirection, int renderType) {
        if (renderType == 2) { // only for short bend
            if (logicalSlot == 1) {
                if (connectionDirection == NORTH)
                    return UP;
                if (connectionDirection == WEST)
                    return SOUTH;
                if (connectionDirection == DOWN)
                    return EAST;
            } else if (logicalSlot == 2) {
                if (connectionDirection == UP)
                    return NORTH;
                if (connectionDirection == SOUTH)
                    return WEST;
                if (connectionDirection == EAST)
                    return DOWN;
            }
        }
        return connectionDirection;
    }

    /**
     * Indicates why a particular pipe kind is being used
     */
    public enum Intent {
        STRAIGHT,
        BEND,
        BEND_CONFLICTING,
    }
}

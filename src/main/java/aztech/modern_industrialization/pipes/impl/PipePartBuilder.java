package aztech.modern_industrialization.pipes.impl;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.util.math.Direction.*;

/**
 * A class that can build pipe model parts using a simple interface.
 */
class PipePartBuilder {
    /**
     * The width of a pipe.
     */
    private static final float SIDE = 2.0f / 16;
    /**
     * The spacing between two pipes.
     */
    private static final float SPACING = 1.0f / 16;
    /**
     * The distance between the side of the block and the first of the five pipe slots.
     */
    private static final float FIRST_POS = (1.0f - 5 * SIDE - 4 * SPACING) / 2;
    private QuadEmitter emitter;
    private Vec3d pos;
    private Vec3d facing;
    private Vec3d right;
    private Sprite sprite;
    private int color;

    PipePartBuilder(QuadEmitter emitter, int slotPos, Direction direction, Sprite sprite, int color) {
        this.emitter = emitter;
        this.facing = Vec3d.of(direction.getVector());
        this.sprite = sprite;
        this.color = color;
        // initial position + half pipe + slotPos * width
        float position = (1.0f - 3 * SIDE - 2 * SPACING) / 2.0f + SIDE / 2.0f + slotPos * (SIDE + SPACING);
        this.pos = new Vec3d(position, position, position);
        // Find a suitable right direction (both right and up must face inside of the block).
        for (Direction d : Direction.values()) {
            this.right = Vec3d.of(d.getVector());
            if (isTowardsInside(this.right) && isTowardsInside(up())) break;
        }
        // Move out of the center cube.
        moveForward(SIDE / 2);
    }

    /**
     * Find out whether the axis direction is far from the sides of the block.
     */
    private boolean isTowardsInside(Vec3d direction) {
        return distanceToSide(direction) > 0.5f - 1e-6;
    }

    /**
     * Get the distance along some axis direction to the nearest block side.
     */
    private float distanceToSide(Vec3d direction) {
        float p = (float) direction.dotProduct(pos);
        if (p > 0) {
            return 1 - p;
        } else {
            return -p;
        }
    }

    /**
     * Add a quad.
     */
    private void quad(Direction direction, float left, float bottom, float right, float top, float depth) {
        square(direction, left, bottom, right, top, depth);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, color, color, color, color);
        emitter.cullFace(null);
        emitter.emit();
    }

    /**
     * Add a quad with double arguments.
     */
    private void quad(Direction direction, double left, double bottom, double right, double top, double depth) {
        quad(direction, (float) left, (float) bottom, (float) right, (float) top, (float) depth);
    }

    // TODO: fix this in fabric api

    /**
     * Add a square. This is a fixed (?) version of the QuadEmitter#square function.
     */
    private void square(Direction nominalFace, float left, float bottom, float right, float top, float depth) {
        emitter.nominalFace(nominalFace);
        switch (nominalFace) {
            case UP:
                depth = 1 - depth;
                top = 1 - top;
                bottom = 1 - bottom;
                emitter.pos(0, left, depth, top);
                emitter.pos(1, left, depth, bottom);
                emitter.pos(2, right, depth, bottom);
                emitter.pos(3, right, depth, top);
                break;

            case DOWN:
                right = 1 - right;
                left = 1 - left;
                emitter.pos(3, left, depth, top);
                emitter.pos(2, left, depth, bottom);
                emitter.pos(1, right, depth, bottom);
                emitter.pos(0, right, depth, top);
                break;

            case EAST:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case WEST:
                emitter.pos(0, depth, top, left);
                emitter.pos(1, depth, bottom, left);
                emitter.pos(2, depth, bottom, right);
                emitter.pos(3, depth, top, right);
                break;

            case SOUTH:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case NORTH:
                left = 1 - left;
                right = 1 - right;
                emitter.pos(3, right, top, depth);
                emitter.pos(2, right, bottom, depth);
                emitter.pos(1, left, bottom, depth);
                emitter.pos(0, left, top, depth);
                break;
        }
    }

    /**
     * Add a quad with two corners and the facing direction.
     */
    private void quad(Vec3d facing, Vec3d c1, Vec3d c2) {
        Direction direction = Direction.getFacing(facing.x, facing.y, facing.z);
        double x = Math.min(c1.x, c2.x), y = Math.min(c1.y, c2.y), z = Math.min(c1.z, c2.z);
        double X = Math.max(c1.x, c2.x), Y = Math.max(c1.y, c2.y), Z = Math.max(c1.z, c2.z);
        if (direction == UP) quad(UP, x, 1 - Z, X, 1 - z, 1 - Y);
        else if (direction == DOWN) quad(DOWN, 1 - X, z, 1 - x, Z, y);
        else if (direction == NORTH) quad(NORTH, 1 - X, y, 1 - x, Y, z);
        else if (direction == EAST) quad(EAST, 1 - Z, y, 1 - z, Y, 1 - X);
        else if (direction == SOUTH) quad(SOUTH, x, y, X, Y, 1 - Z);
        else quad(WEST, z, y, Z, Y, x);
    }

    /**
     * Draw a 4-sided pipe.
     */
    private void drawPipe(float length) {
        for (int i = 0; i < 4; ++i) {
            Vec3d up = up();
            quad(up, pos.add(up.multiply(SIDE / 2)).add(right.multiply(SIDE / 2)), pos.add(up.multiply(SIDE / 2)).subtract(right.multiply(SIDE / 2)).add(facing.multiply(length)));
            rotateCw();
        }
    }

    /**
     * Draw a 5-sided pipe.
     */
    private void drawPipeWithEnd(float length) {
        drawPipe(length);
        moveForward(length);
        noConnection();
        moveForward(-length);
    }

    /**
     * Move forward.
     */
    private void moveForward(float amount) {
        this.pos = this.pos.add(this.facing.multiply(amount));
    }

    /**
     * Get up vector.
     */
    private Vec3d up() {
        return right.crossProduct(facing);
    }

    /**
     * Rotate clockwise around the facing axis.
     */
    private void rotateCw() {
        right = up().negate();
    }

    /**
     * Turn 90° up.
     */
    private void turnUp() {
        facing = up();
    }

    /**
     * Draw a single face.
     */
    void noConnection() {
        Vec3d up = up();
        quad(facing, pos.add(up.multiply(SIDE / 2)).add(right.multiply(SIDE / 2)), pos.subtract(up.multiply(SIDE / 2)).subtract(right.multiply(SIDE / 2)));
    }

    /**
     * Draw a straight line.
     */
    void straightLine() {
        drawPipe(distanceToSide(facing));
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
        drawPipe(distanceToSide(facing));
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
        drawPipe(distanceToSide(facing));
    }
}

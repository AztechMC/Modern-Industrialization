package aztech.modern_industrialization.pipes.impl;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.util.math.Direction.*;

public class PipeMeshBuilder extends PipePartBuilder {
    private QuadEmitter emitter;
    private Sprite sprite;

    PipeMeshBuilder(QuadEmitter emitter, int slotPos, Direction direction, Sprite sprite) {
        super(slotPos, direction);
        this.emitter = emitter;
        this.sprite = sprite;
    }

    /**
     * Add a quad.
     */
    private void quad(Direction direction, float left, float bottom, float right, float top, float depth) {
        square(direction, left, bottom, right, top, depth);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
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
    @Override
    void drawPipe(float length) {
        for (int i = 0; i < 4; ++i) {
            Vec3d up = up();
            quad(up, pos.add(up.multiply(SIDE / 2)).add(right.multiply(SIDE / 2)), pos.add(up.multiply(SIDE / 2)).subtract(right.multiply(SIDE / 2)).add(facing.multiply(length)));
            rotateCw();
        }
    }


    @Override
    void noConnection() {
        Vec3d up = up();
        quad(facing, pos.add(up.multiply(SIDE / 2)).add(right.multiply(SIDE / 2)), pos.subtract(up.multiply(SIDE / 2)).subtract(right.multiply(SIDE / 2)));
    }
}

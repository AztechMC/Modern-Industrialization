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

import static net.minecraft.util.math.Direction.*;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PipeMeshBuilder extends PipePartBuilder {
    protected final QuadEmitter emitter;
    private final Sprite sprite;
    private final float spriteSizeU;
    private final float spriteSizeV;

    PipeMeshBuilder(QuadEmitter emitter, int slotPos, Direction direction, Sprite sprite) {
        super(slotPos, direction);
        this.emitter = emitter;
        this.sprite = sprite;
        this.spriteSizeU = sprite.getMaxU() - sprite.getMinU();
        this.spriteSizeV = sprite.getMaxV() - sprite.getMinV();
    }

    /**
     * Add a quad, BUT DON'T EMIT.
     */
    protected void quad(Direction direction, float left, float bottom, float right, float top, float depth) {
        square(direction, left, bottom, right, top, depth);
        emitter.cullFace(null);
    }

    /**
     * Add a quad with double arguments, BUT DON'T EMIT.
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

        case DOWN:
            emitter.pos(0, left, depth, top);
            emitter.pos(1, left, depth, bottom);
            emitter.pos(2, right, depth, bottom);
            emitter.pos(3, right, depth, top);
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
            emitter.pos(0, 1 - left, top, depth);
            emitter.pos(1, 1 - left, bottom, depth);
            emitter.pos(2, 1 - right, bottom, depth);
            emitter.pos(3, 1 - right, top, depth);
            break;
        }
    }

    /**
     * Add a quad with four corners and the facing direction. It is important that 1
     * and 4 be opposite corners! UVs are actually (u, v, whatever)
     */
    private void quad(Vec3d facing, Vec3d[] corners, Vec3d[] uvs) {
        if (corners.length != 4 || uvs.length != 4)
            throw new RuntimeException("This is a bug, please report!");
        Vec3d c1 = corners[0];
        Vec3d c4 = corners[3];
        Direction direction = Direction.getFacing(facing.x, facing.y, facing.z);
        double x = Math.min(c1.x, c4.x), y = Math.min(c1.y, c4.y), z = Math.min(c1.z, c4.z);
        double X = Math.max(c1.x, c4.x), Y = Math.max(c1.y, c4.y), Z = Math.max(c1.z, c4.z);
        if (direction == UP)
            quad(UP, x, 1 - Z, X, 1 - z, 1 - Y);
        else if (direction == DOWN)
            quad(DOWN, x, z, X, Z, y);
        else if (direction == NORTH)
            quad(NORTH, 1 - X, y, 1 - x, Y, z);
        else if (direction == EAST)
            quad(EAST, 1 - Z, y, 1 - z, Y, 1 - X);
        else if (direction == SOUTH)
            quad(SOUTH, x, y, X, Y, 1 - Z);
        else
            quad(WEST, z, y, Z, Y, x);

        // Map the uvs onto the quad
        for (int i = 0; i < 4; ++i) {
            Vec3d vertexPos = new Vec3d(emitter.copyPos(i, null));
            for (int j = 0; j < 4; ++j) {
                if (vertexPos.subtract(corners[j]).lengthSquared() < 1e-6) {
                    float realU = sprite.getMinU() + spriteSizeU * (float) uvs[j].getX();
                    float realV = sprite.getMinV() + spriteSizeV * (float) uvs[j].getY();
                    emitter.sprite(i, 0, realU, realV);
                }
            }
        }

        emitter.emit();
    }

    private static final double COL_WIDTH = 1 / 8.0;
    private static final double[] BEND_COL = new double[] { 0, 1 / 8.0, 0, 2 / 8.0 };
    private static final double[] BEND_CONFLICTING_COL = new double[] { 0, 4 / 8.0, 0, 5 / 8.0 };
    private static final double[] STRAIGHT_COL = new double[] { 6 / 8.0, 6 / 8.0, 6 / 8.0, 6 / 8.0 };

    /**
     * Draw a 4-sided pipe.
     */
    @Override
    void drawPipe(float length, Intent intent, boolean end) {
        if (length < 0)
            return;
        // Four sides
        double[] cols = intent == Intent.STRAIGHT ? STRAIGHT_COL : intent == Intent.BEND ? BEND_COL : BEND_CONFLICTING_COL;
        for (int i = 0; i < 4; ++i) {
            if (intent != Intent.STRAIGHT && i == 0)
                length -= SIDE;
            double u = cols[i];
            Vec3d up = up();
            Vec3d base = pos.add(up.multiply(SIDE / 2));
            quad(up, new Vec3d[] { base.add(right.multiply(SIDE / 2)), base.subtract(right.multiply(SIDE / 2)),
                    base.add(right.multiply(SIDE / 2)).add(facing.multiply(length)),
                    base.subtract(right.multiply(SIDE / 2)).add(facing.multiply(length)), },
                    new Vec3d[] { new Vec3d(u + COL_WIDTH, length, 0), new Vec3d(u, length, 0), new Vec3d(u + COL_WIDTH, 0, 0),
                            new Vec3d(u, 0, 0), });
            rotateCw();
            if (intent != Intent.STRAIGHT && i == 0)
                length += SIDE;
        }
        // End
        if (end) {
            Vec3d up = up();
            Vec3d base = pos.add(facing.multiply(length));
            quad(facing,
                    new Vec3d[] { base.subtract(up.multiply(SIDE / 2)).subtract(right.multiply(SIDE / 2)),
                            base.subtract(up.multiply(SIDE / 2)).add(right.multiply(SIDE / 2)),
                            base.add(up.multiply(SIDE / 2)).subtract(right.multiply(SIDE / 2)),
                            base.add(up.multiply(SIDE / 2)).add(right.multiply(SIDE / 2)), },
                    intent == Intent.STRAIGHT
                            ? new Vec3d[] { new Vec3d(4 * COL_WIDTH, 0, 0), new Vec3d(3 * COL_WIDTH, 0, 0), new Vec3d(4 * COL_WIDTH, COL_WIDTH, 0),
                                    new Vec3d(3 * COL_WIDTH, COL_WIDTH, 0), }
                            : new Vec3d[] { new Vec3d(COL_WIDTH, 1, 0), new Vec3d(0, 1, 0), new Vec3d(COL_WIDTH, 1 - COL_WIDTH, 0),
                                    new Vec3d(0, 1 - COL_WIDTH, 0), });
        }
    }

    private static final int[][] CENTER_PATTERNS = new int[][] { new int[] { 1, 0, 1, 0 }, new int[] { 0, 1, 1, 0 }, new int[] { 0, 0, 0, 0 },
            new int[] { 1, 1, 1, 1 }, new int[] { 1, 0, 1, 1 }, new int[] { 0, 0, 0, 1 }, };
    private static final double[][] CENTER_UVS = new double[][] { new double[] { 0, 0 }, new double[] { COL_WIDTH, 0 },
            new double[] { 3 * COL_WIDTH, 0 }, new double[] { 3 * COL_WIDTH, COL_WIDTH }, new double[] { 3 * COL_WIDTH, 2 * COL_WIDTH },
            new double[] { 3 * COL_WIDTH, 3 * COL_WIDTH }, };

    /**
     * Draw a single connection face.
     * 
     * @param directions: a bitset with the directions
     */
    void noConnection(int directions) {
        if ((directions & (1 << Direction.getFacing(facing.x, facing.y, facing.z).getId())) > 0) {
            return; // don't render when there is already a connection in this direction
        }
        // Get the 4 connections as '0's and '1's
        int[] sidesDirections = new int[4];
        for (int i = 0; i < 4; ++i) {
            Vec3d up = up();
            Direction sideDir = Direction.getFacing(up.x, up.y, up.z);
            sidesDirections[i] = (directions >> sideDir.getId()) & 1;
            rotateCw();
        }
        // Try to match every pattern
        for (int i = 0; i < CENTER_PATTERNS.length; ++i) {
            // With every possible rotation
            rotations: for (int j = 0; j < 4; ++j) {
                for (int k = 0; k < 4; ++k) {
                    if (CENTER_PATTERNS[i][k] != sidesDirections[(j + k) % 4]) {
                        continue rotations;
                    }
                }
                // Render the connection
                Vec3d up = up();
                Vec3d[] vertices = new Vec3d[] { pos.add(right.multiply(SIDE / 2)).subtract(up.multiply(SIDE / 2)),
                        pos.add(right.multiply(SIDE / 2)).add(up.multiply(SIDE / 2)),
                        pos.subtract(right.multiply(SIDE / 2)).subtract(up.multiply(SIDE / 2)),
                        pos.subtract(right.multiply(SIDE / 2)).add(up.multiply(SIDE / 2)), };
                double u = CENTER_UVS[i][0];
                double v = CENTER_UVS[i][1];
                Vec3d[] uvs = new Vec3d[] { new Vec3d(u, v + COL_WIDTH, 0), new Vec3d(u, v, 0), new Vec3d(u + COL_WIDTH, v + COL_WIDTH, 0),
                        new Vec3d(u + COL_WIDTH, v, 0), };
                for (int k = 0; k < j; ++k) {
                    rotate(vertices);
                }
                quad(facing, vertices, uvs);
                return;
            }
        }
    }

    private void rotate(Vec3d[] arr) {
        Vec3d tmp = arr[0];
        arr[0] = arr[2];
        arr[2] = arr[3];
        arr[3] = arr[1];
        arr[1] = tmp;
    }

    public static class InnerQuads extends PipeMeshBuilder {
        InnerQuads(QuadEmitter emitter, int slotPos, Direction direction, Sprite sprite) {
            super(emitter, slotPos, direction, sprite);
        }

        @Override
        protected void quad(Direction direction, float left, float bottom, float right, float top, float depth) {
            // create the inner qud
            super.quad(direction, left, bottom, right, top, depth + 0.001f);
            // set the tag
            emitter.tag(1);
            // emit the quad
            emitter.emit();
            // create the actual quad
            super.quad(direction, left, bottom, right, top, depth);
        }
    }
}

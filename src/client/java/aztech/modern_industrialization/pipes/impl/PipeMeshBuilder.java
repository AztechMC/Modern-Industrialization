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

import aztech.modern_industrialization.thirdparty.fabricrendering.QuadEmitter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class PipeMeshBuilder extends PipePartBuilder {
    protected final QuadEmitter emitter;
    private final TextureAtlasSprite sprite;
    private final float spriteSizeU;
    private final float spriteSizeV;

    PipeMeshBuilder(QuadEmitter emitter, int slotPos, Direction direction, TextureAtlasSprite sprite) {
        super(slotPos, direction);
        this.emitter = emitter;
        this.sprite = sprite;
        this.spriteSizeU = sprite.getU1() - sprite.getU0();
        this.spriteSizeV = sprite.getV1() - sprite.getV0();
    }

    /**
     * Add a quad, BUT DON'T EMIT.
     */
    protected void quad(Direction direction, float left, float bottom, float right, float top, float depth) {
        emitter.square(direction, left, bottom, right, top, depth);
        emitter.cullFace(null);
    }

    /**
     * Add a quad with double arguments, BUT DON'T EMIT.
     */
    private void quad(Direction direction, double left, double bottom, double right, double top, double depth) {
        quad(direction, (float) left, (float) bottom, (float) right, (float) top, (float) depth);
    }

    /**
     * Add a quad with four corners and the facing direction. It is important that 1
     * and 4 be opposite corners! UVs are actually (u, v, whatever)
     */
    private void quad(Vec3 facing, Vec3[] corners, Vec3[] uvs) {
        if (corners.length != 4 || uvs.length != 4)
            throw new RuntimeException("This is a bug, please report!");
        Vec3 c1 = corners[0];
        Vec3 c4 = corners[3];
        Direction direction = Direction.getNearest(facing.x, facing.y, facing.z);
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
            Vec3 vertexPos = new Vec3(emitter.copyPos(i, null));
            for (int j = 0; j < 4; ++j) {
                if (vertexPos.subtract(corners[j]).lengthSqr() < 1e-6) {
                    float realU = sprite.getU0() + spriteSizeU * (float) uvs[j].x();
                    float realV = sprite.getV0() + spriteSizeV * (float) uvs[j].y();
                    emitter.uv(i, realU, realV);
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
        if (length <= 1e-9)
            return;
        // Four sides
        double[] cols = intent == Intent.STRAIGHT ? STRAIGHT_COL : intent == Intent.BEND ? BEND_COL : BEND_CONFLICTING_COL;
        for (int i = 0; i < 4; ++i) {
            if (intent != Intent.STRAIGHT && i == 0)
                length -= SIDE;
            double u = cols[i];
            Vec3 up = up();
            Vec3 base = pos.add(up.scale(SIDE / 2));
            quad(up, new Vec3[] { base.add(right.scale(SIDE / 2)), base.subtract(right.scale(SIDE / 2)),
                    base.add(right.scale(SIDE / 2)).add(facing.scale(length)),
                    base.subtract(right.scale(SIDE / 2)).add(facing.scale(length)), },
                    new Vec3[] { new Vec3(u + COL_WIDTH, length, 0), new Vec3(u, length, 0), new Vec3(u + COL_WIDTH, 0, 0),
                            new Vec3(u, 0, 0), });
            rotateCw();
            if (intent != Intent.STRAIGHT && i == 0)
                length += SIDE;
        }
        // End
        if (end) {
            Vec3 up = up();
            Vec3 base = pos.add(facing.scale(length));
            quad(facing,
                    new Vec3[] { base.subtract(up.scale(SIDE / 2)).subtract(right.scale(SIDE / 2)),
                            base.subtract(up.scale(SIDE / 2)).add(right.scale(SIDE / 2)),
                            base.add(up.scale(SIDE / 2)).subtract(right.scale(SIDE / 2)),
                            base.add(up.scale(SIDE / 2)).add(right.scale(SIDE / 2)), },
                    intent == Intent.STRAIGHT
                            ? new Vec3[] { new Vec3(4 * COL_WIDTH, 0, 0), new Vec3(3 * COL_WIDTH, 0, 0), new Vec3(4 * COL_WIDTH, COL_WIDTH, 0),
                                    new Vec3(3 * COL_WIDTH, COL_WIDTH, 0), }
                            : new Vec3[] { new Vec3(COL_WIDTH, 1, 0), new Vec3(0, 1, 0), new Vec3(COL_WIDTH, 1 - COL_WIDTH, 0),
                                    new Vec3(0, 1 - COL_WIDTH, 0), });
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
        if ((directions & (1 << Direction.getNearest(facing.x, facing.y, facing.z).get3DDataValue())) > 0) {
            return; // don't render when there is already a connection in this direction
        }
        // Get the 4 connections as '0's and '1's
        int[] sidesDirections = new int[4];
        for (int i = 0; i < 4; ++i) {
            Vec3 up = up();
            Direction sideDir = Direction.getNearest(up.x, up.y, up.z);
            sidesDirections[i] = (directions >> sideDir.get3DDataValue()) & 1;
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
                Vec3 up = up();
                Vec3[] vertices = new Vec3[] { pos.add(right.scale(SIDE / 2)).subtract(up.scale(SIDE / 2)),
                        pos.add(right.scale(SIDE / 2)).add(up.scale(SIDE / 2)),
                        pos.subtract(right.scale(SIDE / 2)).subtract(up.scale(SIDE / 2)),
                        pos.subtract(right.scale(SIDE / 2)).add(up.scale(SIDE / 2)), };
                double u = CENTER_UVS[i][0];
                double v = CENTER_UVS[i][1];
                Vec3[] uvs = new Vec3[] { new Vec3(u, v + COL_WIDTH, 0), new Vec3(u, v, 0), new Vec3(u + COL_WIDTH, v + COL_WIDTH, 0),
                        new Vec3(u + COL_WIDTH, v, 0), };
                for (int k = 0; k < j; ++k) {
                    rotate(vertices);
                }
                quad(facing, vertices, uvs);
                return;
            }
        }
    }

    private void rotate(Vec3[] arr) {
        Vec3 tmp = arr[0];
        arr[0] = arr[2];
        arr[2] = arr[3];
        arr[3] = arr[1];
        arr[1] = tmp;
    }

    public static class InnerQuads extends PipeMeshBuilder {
        InnerQuads(QuadEmitter emitter, int slotPos, Direction direction, TextureAtlasSprite sprite) {
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

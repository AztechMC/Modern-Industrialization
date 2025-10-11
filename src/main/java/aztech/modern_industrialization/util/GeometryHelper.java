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

package aztech.modern_industrialization.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class GeometryHelper {
    private GeometryHelper() {
    }

    /**
     * Vectors to the right of the face, i.e. the X axis in the XY plane of the
     * face.
     */
    public static Vec3[] FACE_RIGHT = new Vec3[] { new Vec3(1, 0, 0), new Vec3(1, 0, 0), new Vec3(-1, 0, 0), new Vec3(1, 0, 0),
            new Vec3(0, 0, 1), new Vec3(0, 0, -1), };
    /**
     * Vectors to the up of the face, i.e. the Y axis in the XY plane of the face.
     */
    public static Vec3[] FACE_UP = new Vec3[] { new Vec3(0, 0, 1), new Vec3(0, 0, -1), new Vec3(0, 1, 0), new Vec3(0, 1, 0), new Vec3(0, 1, 0),
            new Vec3(0, 1, 0), };
    /**
     * Origins of the XY planes of the faces.
     */
    public static Vec3[] FACE_ORIGIN = new Vec3[] { new Vec3(0, 0, 0), new Vec3(0, 1, 1), new Vec3(1, 0, 0), new Vec3(0, 0, 1),
            new Vec3(0, 0, 0), new Vec3(1, 0, 1), };

    /**
     * Project onto a face.
     *
     * @return (x coordinate, y coordinate, 0) in the XY plane of the face.
     */
    public static Vec3 toFaceCoords(Vec3 posInBlock, Direction face) {
        posInBlock = posInBlock.subtract(FACE_ORIGIN[face.get3DDataValue()]);
        return new Vec3(posInBlock.dot(FACE_RIGHT[face.get3DDataValue()]), posInBlock.dot(FACE_UP[face.get3DDataValue()]), 0);
    }

    public static Vec3 toWorldCoords(Vec3 faceCoords, Direction face) {
        return FACE_ORIGIN[face.get3DDataValue()].add(FACE_RIGHT[face.get3DDataValue()].scale(faceCoords.x))
                .add(FACE_UP[face.get3DDataValue()].scale(faceCoords.y));
    }
}

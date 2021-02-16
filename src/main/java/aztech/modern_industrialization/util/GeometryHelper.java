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

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class GeometryHelper {
    private GeometryHelper() {
    }

    /**
     * Vectors to the right of the face, i.e. the X axis in the XY plane of the
     * face.
     */
    public static Vec3d[] FACE_RIGHT = new Vec3d[] { new Vec3d(1, 0, 0), new Vec3d(1, 0, 0), new Vec3d(-1, 0, 0), new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1), new Vec3d(0, 0, -1), };
    /**
     * Vectors to the up of the face, i.e. the Y axis in the XY plane of the face.
     */
    public static Vec3d[] FACE_UP = new Vec3d[] { new Vec3d(0, 0, 1), new Vec3d(0, 0, -1), new Vec3d(0, 1, 0), new Vec3d(0, 1, 0), new Vec3d(0, 1, 0),
            new Vec3d(0, 1, 0), };
    /**
     * Origins of the XY planes of the faces.
     */
    public static Vec3d[] FACE_ORIGIN = new Vec3d[] { new Vec3d(0, 0, 0), new Vec3d(0, 1, 1), new Vec3d(1, 0, 0), new Vec3d(0, 0, 1),
            new Vec3d(0, 0, 0), new Vec3d(1, 0, 1), };

    /**
     * Project onto a face.
     * 
     * @return (x coordinate, y coordinate, 0) in the XY plane of the face.
     */
    public static Vec3d toFaceCoords(Vec3d posInBlock, Direction face) {
        posInBlock = posInBlock.subtract(FACE_ORIGIN[face.getId()]);
        return new Vec3d(posInBlock.dotProduct(FACE_RIGHT[face.getId()]), posInBlock.dotProduct(FACE_UP[face.getId()]), 0);
    }

    public static Vec3d toWorldCoords(Vec3d faceCoords, Direction face) {
        return FACE_ORIGIN[face.getId()].add(FACE_RIGHT[face.getId()].multiply(faceCoords.x)).add(FACE_UP[face.getId()].multiply(faceCoords.y));
    }
}

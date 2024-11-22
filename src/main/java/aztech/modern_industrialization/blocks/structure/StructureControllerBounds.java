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
package aztech.modern_industrialization.blocks.structure;

import com.mojang.serialization.Codec;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public record StructureControllerBounds(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {

    public static final Codec<StructureControllerBounds> CODEC = Codec.INT_STREAM.comapFlatMap(
            (stream) -> Util.fixedSize(stream, 6).map((array) -> new StructureControllerBounds(
                    array[0], array[1], array[2],
                    array[3], array[4], array[5])),
            (bounds) -> IntStream.of(
                    bounds.x(), bounds.y(), bounds.z(),
                    bounds.sizeX(), bounds.sizeY(), bounds.sizeZ()));

    public static final StructureControllerBounds EMPTY = new StructureControllerBounds(0, 0, 0, 0, 0, 0);

    public boolean isEmpty() {
        return sizeX == 0 || sizeY == 0 || sizeZ == 0;
    }

    @Nullable
    public BoundingBox boundingBox() {
        if (this.isEmpty()) {
            return null;
        }
        int x1 = x;
        int y1 = y;
        int z1 = z;
        int x2 = x + sizeX + Integer.compare(0, sizeX);
        int y2 = y + sizeY + Integer.compare(0, sizeY);
        int z2 = z + sizeZ + Integer.compare(0, sizeZ);
        return new BoundingBox(
                Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    @Nullable
    public AABB aabb() {
        BoundingBox boundingBox = this.boundingBox();
        return boundingBox == null ? null : AABB.of(boundingBox);
    }
}

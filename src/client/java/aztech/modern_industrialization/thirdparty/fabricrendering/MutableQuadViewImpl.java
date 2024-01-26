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
package aztech.modern_industrialization.thirdparty.fabricrendering;
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static aztech.modern_industrialization.thirdparty.fabricrendering.EncodingFormat.*;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Almost-concrete implementation of a mutable quad. The only missing part is {@link #emit()}, because that depends on
 * where/how it is used. (Mesh encoding vs. render-time transformation).
 */
public abstract class MutableQuadViewImpl extends QuadViewImpl implements QuadEmitter {
    public final void begin(int[] data, int baseIndex) {
        this.data = data;
        this.baseIndex = baseIndex;
        clear();
    }

    public void clear() {
        System.arraycopy(EMPTY, 0, data, baseIndex, EncodingFormat.TOTAL_STRIDE);
        isGeometryInvalid = true;
        nominalFace = null;
        normalFlags(0);
        tag(0);
        colorIndex(-1);
        cullFace(null);
    }

    @Override
    public MutableQuadViewImpl pos(int vertexIndex, float x, float y, float z) {
        final int index = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_X;
        data[index] = Float.floatToRawIntBits(x);
        data[index + 1] = Float.floatToRawIntBits(y);
        data[index + 2] = Float.floatToRawIntBits(z);
        isGeometryInvalid = true;
        return this;
    }

    @Override
    public MutableQuadViewImpl color(int vertexIndex, int color) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_COLOR] = color;
        return this;
    }

    @Override
    public MutableQuadViewImpl uv(int vertexIndex, float u, float v) {
        final int i = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_U;
        data[i] = Float.floatToRawIntBits(u);
        data[i + 1] = Float.floatToRawIntBits(v);
        return this;
    }

    @Override
    public MutableQuadViewImpl spriteBake(TextureAtlasSprite sprite, int bakeFlags) {
        TextureHelper.bakeSprite(this, sprite, bakeFlags);
        return this;
    }

    @Override
    public MutableQuadViewImpl lightmap(int vertexIndex, int lightmap) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_LIGHTMAP] = lightmap;
        return this;
    }

    protected void normalFlags(int flags) {
        data[baseIndex + HEADER_BITS] = EncodingFormat.normalFlags(data[baseIndex + HEADER_BITS], flags);
    }

    @Override
    public MutableQuadViewImpl normal(int vertexIndex, float x, float y, float z) {
        normalFlags(normalFlags() | (1 << vertexIndex));
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_NORMAL] = NormalHelper.packNormal(x, y, z, 0);
        return this;
    }

    /**
     * Internal helper method. Copies face normals to vertex normals lacking one.
     */
    public final void populateMissingNormals() {
        final int normalFlags = this.normalFlags();

        if (normalFlags == 0b1111)
            return;

        final int packedFaceNormal = NormalHelper.packNormal(faceNormal(), 0);

        for (int v = 0; v < 4; v++) {
            if ((normalFlags & (1 << v)) == 0) {
                data[baseIndex + v * VERTEX_STRIDE + VERTEX_NORMAL] = packedFaceNormal;
            }
        }

        normalFlags(0b1111);
    }

    @Override
    public final MutableQuadViewImpl cullFace(@Nullable Direction face) {
        data[baseIndex + HEADER_BITS] = EncodingFormat.cullFace(data[baseIndex + HEADER_BITS], face);
        nominalFace(face);
        return this;
    }

    @Override
    public final MutableQuadViewImpl nominalFace(@Nullable Direction face) {
        nominalFace = face;
        return this;
    }

    @Override
    public final MutableQuadViewImpl colorIndex(int colorIndex) {
        data[baseIndex + HEADER_COLOR_INDEX] = colorIndex;
        return this;
    }

    @Override
    public final MutableQuadViewImpl tag(int tag) {
        data[baseIndex + HEADER_TAG] = tag;
        return this;
    }

    @Override
    public final MutableQuadViewImpl fromVanilla(int[] quadData, int startIndex) {
        System.arraycopy(quadData, startIndex, data, baseIndex + HEADER_STRIDE, VANILLA_QUAD_STRIDE);
        isGeometryInvalid = true;

        int colorIndex = baseIndex + VERTEX_COLOR;

        for (int i = 0; i < 4; i++) {
            data[colorIndex] = ColorHelper.fromVanillaColor(data[colorIndex]);
            colorIndex += VERTEX_STRIDE;
        }

        return this;
    }

    @Override
    public final MutableQuadViewImpl fromVanilla(BakedQuad quad, @Nullable Direction cullFace) {
        fromVanilla(quad.getVertices(), 0);
        data[baseIndex + HEADER_BITS] = EncodingFormat.cullFace(0, cullFace);
        nominalFace(quad.getDirection());
        colorIndex(quad.getTintIndex());

        tag(0);
        return this;
    }

    /**
     * Emit the quad without clearing the underlying data.
     * Geometry is not guaranteed to be valid when called, but can be computed by calling {@link #computeGeometry()}.
     */
    public abstract void emitDirectly();

    @Override
    public final MutableQuadViewImpl emit() {
        emitDirectly();
        clear();
        return this;
    }
}

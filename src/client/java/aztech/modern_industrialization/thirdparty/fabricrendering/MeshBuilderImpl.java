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

/**
 * Our implementation of {@link MeshBuilder}, used for static mesh creation and baking. Not much to it - mainly it just
 * needs to grow the int[] array as quads are appended and maintain/provide a properly-configured
 * {@link MutableQuadView} instance. All the encoding and other work is handled in the quad base classes. The one
 * interesting bit is in {@link Maker#emit()}.
 */
public class MeshBuilderImpl implements MeshBuilder {
    int[] data = new int[256];
    private final Maker maker = new Maker();
    int index = 0;
    int limit = data.length;

    protected void ensureCapacity(int stride) {
        if (stride > limit - index) {
            limit *= 2;
            final int[] bigger = new int[limit];
            System.arraycopy(data, 0, bigger, 0, index);
            data = bigger;
            maker.data = bigger;
        }
    }

    @Override
    public Mesh build() {
        final int[] packed = new int[index];
        System.arraycopy(data, 0, packed, 0, index);
        index = 0;
        maker.begin(data, index);
        return new MeshImpl(packed);
    }

    @Override
    public QuadEmitter getEmitter() {
        ensureCapacity(EncodingFormat.TOTAL_STRIDE);
        maker.begin(data, index);
        return maker;
    }

    /**
     * Our base classes are used differently so we define final encoding steps in subtypes. This will be a static mesh
     * used at render time so we want to capture all geometry now and apply non-location-dependent lighting.
     */
    private class Maker extends MutableQuadViewImpl {
        @Override
        public void emitDirectly() {
            computeGeometry();
            index += EncodingFormat.TOTAL_STRIDE;
            ensureCapacity(EncodingFormat.TOTAL_STRIDE);
            baseIndex = index;
        }
    }
}

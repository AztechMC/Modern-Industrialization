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

import java.nio.ByteOrder;

/**
 * Static routines of general utility for renderer implementations. Renderers are not required to use these helpers, but
 * they were designed to be usable without the default renderer.
 */
public abstract class ColorHelper {
    private ColorHelper() {
    }

    private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    /*
     * Renderer color format: ARGB (0xAARRGGBB) Vanilla color format (little endian): ABGR (0xAABBGGRR) Vanilla color
     * format (big endian): RGBA (0xRRGGBBAA)
     * 
     * Why does the vanilla color format change based on endianness? See VertexConsumer#quad. Quad data is loaded as
     * integers into a native byte order buffer. Color is read directly from bytes 12, 13, 14 of each vertex. A
     * different byte order will yield different results.
     * 
     * The renderer always uses ARGB because the API color methods always consume and return ARGB. Vanilla block and
     * item colors also use ARGB.
     */

    /**
     * Converts from ARGB color to ABGR color if little endian or RGBA color if big endian.
     */
    public static int toVanillaColor(int color) {
        if (color == -1) {
            return -1;
        }

        if (BIG_ENDIAN) {
            // ARGB to RGBA
            return ((color & 0x00FFFFFF) << 8) | ((color & 0xFF000000) >>> 24);
        } else {
            // ARGB to ABGR
            return (color & 0xFF00FF00) | ((color & 0x00FF0000) >>> 16) | ((color & 0x000000FF) << 16);
        }
    }

    /**
     * Converts to ARGB color from ABGR color if little endian or RGBA color if big endian.
     */
    public static int fromVanillaColor(int color) {
        if (color == -1) {
            return -1;
        }

        if (BIG_ENDIAN) {
            // RGBA to ARGB
            return ((color & 0xFFFFFF00) >>> 8) | ((color & 0x000000FF) << 24);
        } else {
            // ABGR to ARGB
            return (color & 0xFF00FF00) | ((color & 0x00FF0000) >>> 16) | ((color & 0x000000FF) << 16);
        }
    }
}

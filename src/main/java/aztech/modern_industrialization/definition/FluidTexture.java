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

package aztech.modern_industrialization.definition;

import aztech.modern_industrialization.textures.Animation;
import aztech.modern_industrialization.textures.AnimationWithFrames;
import aztech.modern_industrialization.textures.MCMetaInfo;
import java.util.List;

public enum FluidTexture {

    LAVA_LIKE("lava_like", new MCMetaInfo(new AnimationWithFrames(2, List.of(0,
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)))),

    PLASMA_LIKE("plasma_like", new MCMetaInfo(new Animation(1))),
    STEAM_LIKE("steam_like", new MCMetaInfo(new Animation(2))),
    WATER_LIKE("water_like", new MCMetaInfo(new Animation(2)));

    public final String path;
    public final MCMetaInfo mcMetaInfo;

    FluidTexture(String path, MCMetaInfo mcMetaInfo) {
        this.path = path;
        this.mcMetaInfo = mcMetaInfo;
    }
}

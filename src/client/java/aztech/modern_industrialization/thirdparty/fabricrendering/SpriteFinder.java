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

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Indexes a texture atlas to allow fast lookup of Sprites from baked vertex coordinates. Main use is for
 * {@link Mesh}-based models to generate vanilla quads on demand without tracking and retaining the sprites that were
 * baked into the mesh. In other words, this class supplies the sprite parameter for
 * {@link QuadView#toBakedQuad(int, TextureAtlasSprite, boolean)}.
 */
public interface SpriteFinder {
    /**
     * Finds the atlas sprite containing the vertex centroid of the quad. Vertex centroid is essentially the mean u,v
     * coordinate - the intent being to find a point that is unambiguously inside the sprite (vs on an edge.)
     *
     * <p>
     * Should be reliable for any convex quad or triangle. May fail for non-convex quads. Note that all the above refers
     * to u,v coordinates. Geometric vertex does not matter, except to the extent it was used to determine u,v.
     */
    TextureAtlasSprite find(QuadView quad);

    /**
     * Alternative to {@link #find(QuadView, int)} when vertex centroid is already known or unsuitable. Expects
     * normalized (0-1) coordinates on the atlas texture, which should already be the case for u,v values in vanilla
     * baked quads and in {@link QuadView} after calling
     * {@link MutableQuadView#spriteBake(int, TextureAtlasSprite, int)}.
     *
     * <p>
     * Coordinates must be in the sprite interior for reliable results. Generally will be easier to use
     * {@link #find(QuadView, int)} unless you know the vertex centroid will somehow not be in the quad interior. This
     * method will be slightly faster if you already have the centroid or another appropriate value.
     */
    TextureAtlasSprite find(float u, float v);
}

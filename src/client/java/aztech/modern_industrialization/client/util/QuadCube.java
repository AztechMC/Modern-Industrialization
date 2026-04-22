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

package aztech.modern_industrialization.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class QuadCube {
    private final SpriteId spriteLocation;
    private BakedQuad @Nullable [] quads;

    public QuadCube(Identifier spriteLocation) {
        // TODO: is this the right atlas id?
        this.spriteLocation = new SpriteId(AtlasIds.BLOCKS, spriteLocation);
    }

    public BakedQuad[] getQuads() {
        var sprite = Minecraft.getInstance().getAtlasManager().get(spriteLocation);
        // Rebuild quads if the sprite changed
        if (quads == null || quads[0].materialInfo().sprite() != sprite) {
            quads = buildQuads(sprite);
        }
        return quads;
    }

    private static BakedQuad[] buildQuads(TextureAtlasSprite sprite) {
        var quads = new BakedQuad[6];
        for (Direction direction : Direction.values()) {
            quads[direction.get3DDataValue()] = ModelHelper.bakeSprite(direction, sprite, 0);
        }
        return quads;
    }
}

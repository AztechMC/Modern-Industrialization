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

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class ModelHelper {
    public static final @Nullable Direction[] DIRECTIONS_WITH_NULL = Arrays.copyOf(Direction.values(), 7);
    static {
        DIRECTIONS_WITH_NULL[6] = null;
    }

    public static int nullableDirectionIndex(@Nullable Direction side) {
        return side == null ? 6 : side.get3DDataValue();
    }

    // TODO: PR to NeoForge
    public static void addQuad(QuadCollection.Builder quads, @Nullable Direction side, BakedQuad quad) {
        if (side == null) {
            quads.addUnculledFace(quad);
        } else {
            quads.addCulledFace(side, quad);
        }
    }

    private static final float CULL_FACE_EPSILON = 0.00001f;

    public static BakedQuad bakeSprite(Direction d, TextureAtlasSprite sprite, float depth) {
        Vector3f[] pos = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
        ModelHelper.square(pos, d, 0, 0, 1, 1, depth);

        long[] uv = ModelHelper.bakeUvs(pos, sprite, d);

        var material = new Material.Baked(sprite, false);
        var materialInfo = BakedQuad.MaterialInfo.of(material, sprite.transparency(), -1, true, 0, true);
        return new BakedQuad(
                pos[0], pos[1], pos[2], pos[3],
                uv[0], uv[1], uv[2], uv[3],
                d, materialInfo,
                BakedNormals.of(BakedNormals.pack(d.getUnitVec3f())), BakedColors.DEFAULT);
    }

    public static void square(Vector3f[] out, Direction nominalFace, float left, float bottom, float right, float top, float depth) {
        if (Math.abs(depth) < CULL_FACE_EPSILON) {
            depth = 0; // avoid any inconsistency for face quads
        }

        switch (nominalFace) {
            case UP:
                depth = 1 - depth;
                top = 1 - top;
                bottom = 1 - bottom;

            case DOWN:
                out[0].set(left, depth, top);
                out[1].set(left, depth, bottom);
                out[2].set(right, depth, bottom);
                out[3].set(right, depth, top);
                break;

            case EAST:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case WEST:
                out[0].set(depth, top, left);
                out[1].set(depth, bottom, left);
                out[2].set(depth, bottom, right);
                out[3].set(depth, top, right);
                break;

            case SOUTH:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case NORTH:
                out[0].set(1 - left, top, depth);
                out[1].set(1 - left, bottom, depth);
                out[2].set(1 - right, bottom, depth);
                out[3].set(1 - right, top, depth);
                break;
        }
    }

    public static long[] bakeUvs(Vector3fc[] pos, TextureAtlasSprite sprite, Direction face) {
        Vector2f[] uvs = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            uvs[i] = lockUvs(pos[i], face);
        }
        interpolate(uvs, sprite);
        return new long[] { packUV(uvs[0]), packUV(uvs[1]), packUV(uvs[2]), packUV(uvs[3]) };
    }

    private static Vector2f lockUvs(Vector3fc pos, Direction face) {
        return switch (face) {
            case EAST -> new Vector2f(1 - pos.z(), 1 - pos.y());
            case WEST -> new Vector2f(pos.z(), 1 - pos.y());
            case NORTH -> new Vector2f(1 - pos.x(), 1 - pos.y());
            case SOUTH -> new Vector2f(pos.x(), 1 - pos.y());
            case DOWN -> new Vector2f(pos.x(), 1 - pos.z());
            case UP -> new Vector2f(pos.x(), pos.z());
        };
    }

    /**
     * Faster than sprite method. Sprite computes span and normalizes inputs each call,
     * so we'd have to denormalize before we called, only to have the sprite renormalize immediately.
     */
    private static void interpolate(Vector2f[] uvs, TextureAtlasSprite sprite) {
        final float uMin = sprite.getU0();
        final float uSpan = sprite.getU1() - uMin;
        final float vMin = sprite.getV0();
        final float vSpan = sprite.getV1() - vMin;

        for (int i = 0; i < 4; i++) {
            uvs[i].set(uMin + uvs[i].x * uSpan, vMin + uvs[i].y * vSpan);
        }
    }

    private static long packUV(Vector2fc uv) {
        return UVPair.pack(uv.x(), uv.y());
    }
}

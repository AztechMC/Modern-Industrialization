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

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ModelHelper {
    public static final @Nullable Direction[] DIRECTIONS_WITH_NULL = Arrays.copyOf(Direction.values(), 7);
    static {
        DIRECTIONS_WITH_NULL[6] = null;
    }

    public static int nullableDirectionIndex(@Nullable Direction side) {
        return side == null ? 6 : side.get3DDataValue();
    }

    public static void emitSprite(QuadBakingVertexConsumer vc, Direction d, TextureAtlasSprite sprite, float depth) {
        vc.setTintIndex(-1);
        vc.setDirection(d);
        vc.setSprite(sprite);
        vc.setShade(true);
        vc.setHasAmbientOcclusion(true);

        Vector3f[] pos = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
        ModelHelper.square(pos, d, 0, 0, 1, 1, depth);

        Vector2f[] uv = ModelHelper.bakeUvs(pos, sprite, d);

        for (int i = 0; i < 4; ++i) {
            vc.vertex(pos[i].x, pos[i].y, pos[i].z);
            vc.color(255, 255, 255, 255);
            vc.uv(uv[i].x, uv[i].y);
            vc.uv2(0);
            var normal = d.getNormal();
            vc.normal(normal.getX(), normal.getY(), normal.getZ());
            vc.endVertex();
        }
    }

    public static void square(Vector3f[] out, Direction nominalFace, float left, float bottom, float right, float top, float depth) {
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

    public static Vector2f[] bakeUvs(Vector3f[] pos, TextureAtlasSprite sprite, Direction face) {
        Vector2f[] uvs = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            uvs[i] = lockUvs(pos[i], face);
        }
        interpolate(uvs, sprite);
        return uvs;
    }

    private static Vector2f lockUvs(Vector3f pos, Direction face) {
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

    /**
     * The vanilla model transformation logic is closely coupled with model deserialization.
     * That does little good for modded model loaders and procedurally generated models.
     * This convenient construction method applies the same scaling factors used for vanilla models.
     * This means you can use values from a vanilla JSON file as inputs to this method.
     */
    private static ItemTransform makeTransform(float rotationX, float rotationY, float rotationZ, float translationX, float translationY,
            float translationZ, float scaleX, float scaleY, float scaleZ) {
        Vector3f translation = new Vector3f(translationX, translationY, translationZ);
        translation.mul(0.0625f);
        translation.set(Mth.clamp(translation.x, -5.0F, 5.0F), Mth.clamp(translation.y, -5.0F, 5.0F), Mth.clamp(translation.z, -5.0F, 5.0F));
        return new ItemTransform(new Vector3f(rotationX, rotationY, rotationZ), translation, new Vector3f(scaleX, scaleY, scaleZ));
    }

    public static final ItemTransform TRANSFORM_BLOCK_GUI = makeTransform(30, 225, 0, 0, 0, 0, 0.625f, 0.625f, 0.625f);
    public static final ItemTransform TRANSFORM_BLOCK_GROUND = makeTransform(0, 0, 0, 0, 3, 0, 0.25f, 0.25f, 0.25f);
    public static final ItemTransform TRANSFORM_BLOCK_FIXED = makeTransform(0, 0, 0, 0, 0, 0, 0.5f, 0.5f, 0.5f);
    public static final ItemTransform TRANSFORM_BLOCK_3RD_PERSON_RIGHT = makeTransform(75, 45, 0, 0, 2.5f, 0, 0.375f, 0.375f, 0.375f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_RIGHT = makeTransform(0, 45, 0, 0, 0, 0, 0.4f, 0.4f, 0.4f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_LEFT = makeTransform(0, 225, 0, 0, 0, 0, 0.4f, 0.4f, 0.4f);

    /**
     * Mimics the vanilla model transformation used for most vanilla blocks,
     * and should be suitable for most custom block-like models.
     */
    public static final ItemTransforms MODEL_TRANSFORM_BLOCK = new ItemTransforms(TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_3RD_PERSON_RIGHT,
            TRANSFORM_BLOCK_1ST_PERSON_LEFT, TRANSFORM_BLOCK_1ST_PERSON_RIGHT, ItemTransform.NO_TRANSFORM, TRANSFORM_BLOCK_GUI,
            TRANSFORM_BLOCK_GROUND, TRANSFORM_BLOCK_FIXED, ImmutableMap.of());
}

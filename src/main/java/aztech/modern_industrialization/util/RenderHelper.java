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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lwjgl.system.MemoryStack;

public class RenderHelper {
    private static final BakedQuad[] OVERLAY_QUADS;
    private static final float W = 0.05f;

    public static void drawOverlay(PoseStack ms, MultiBufferSource vcp, float r, float g, float b, int light, int overlay) {
        VertexConsumer vc = vcp.getBuffer(RenderType.solid());
        for (BakedQuad overlayQuad : OVERLAY_QUADS) {
            vc.putBulkData(ms.last(), overlayQuad, r, g, b, light, overlay);
        }
    }

    static {
        OVERLAY_QUADS = new BakedQuad[24];
        Renderer r = RendererAccess.INSTANCE.getRenderer();
        RenderMaterial material = r.materialFinder().blendMode(0, BlendMode.SOLID).find();
        for (Direction direction : Direction.values()) {
            QuadEmitter emitter;
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, 0, 1, W, 0);
            emitter.material(material);
            OVERLAY_QUADS[direction.get3DDataValue() * 4] = emitter.toBakedQuad(0, null, false);
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, 1 - W, 1, 1, 0);
            emitter.material(material);
            OVERLAY_QUADS[direction.get3DDataValue() * 4 + 1] = emitter.toBakedQuad(0, null, false);
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, W, W, 1 - W, 0);
            emitter.material(material);
            OVERLAY_QUADS[direction.get3DDataValue() * 4 + 2] = emitter.toBakedQuad(0, null, false);
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 1 - W, W, 1, 1 - W, 0);
            emitter.material(material);
            OVERLAY_QUADS[direction.get3DDataValue() * 4 + 3] = emitter.toBakedQuad(0, null, false);
        }
    }

    private static final BakedQuad[] CUBE_QUADS;

    public static void drawCube(PoseStack ms, MultiBufferSource vcp, float r, float g, float b, int light, int overlay) {
        VertexConsumer vc = vcp.getBuffer(RenderType.solid());
        for (BakedQuad cubeQuad : CUBE_QUADS) {
            vc.putBulkData(ms.last(), cubeQuad, r, g, b, light, overlay);
        }
    }

    static {
        CUBE_QUADS = new BakedQuad[6];
        Renderer r = RendererAccess.INSTANCE.getRenderer();
        for (Direction direction : Direction.values()) {
            QuadEmitter emitter;
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, 0, 1, 1, 0);
            CUBE_QUADS[direction.get3DDataValue()] = emitter.toBakedQuad(0, null, false);
        }
    }

    private static final float TANK_W = 0.02f;
    public static final int FULL_LIGHT = 0x00F0_00F0;

    public static void drawFluidInTank(BlockEntity be, PoseStack ms, MultiBufferSource vcp, FluidVariant fluid, float fill) {
        drawFluidInTank(be.getLevel(), be.getBlockPos(), ms, vcp, fluid, fill);
    }

    public static void drawFluidInTank(Level world, BlockPos pos, PoseStack ms, MultiBufferSource vcp, FluidVariant fluid, float fill) {
        VertexConsumer vc = vcp.getBuffer(RenderType.translucent());
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        int color = FluidVariantRendering.getColor(fluid, world, pos);
        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;

        // Make sure fill is within [TANK_W, 1 - TANK_W]
        fill = Math.min(fill, 1 - TANK_W);
        fill = Math.max(fill, TANK_W);
        // Top and bottom positions of the fluid inside the tank
        float topHeight = fill;
        float bottomHeight = TANK_W;
        // Render gas from top to bottom
        if (FluidVariantAttributes.isLighterThanAir(fluid)) {
            topHeight = 1 - TANK_W;
            bottomHeight = 1 - fill;
        }

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        for (Direction direction : Direction.values()) {
            QuadEmitter emitter = renderer.meshBuilder().getEmitter();

            if (direction.getAxis().isVertical()) {
                emitter.square(direction, TANK_W, TANK_W, 1 - TANK_W, 1 - TANK_W, direction == Direction.UP ? 1 - topHeight : bottomHeight);
            } else {
                emitter.square(direction, TANK_W, bottomHeight, 1 - TANK_W, topHeight, TANK_W);
            }

            emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            vc.putBulkData(ms.last(), emitter.toBakedQuad(0, sprite, false), r, g, b, FULL_LIGHT, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void drawFluidInGui(PoseStack ms, FluidVariant fluid, int i, int j) {
        drawFluidInGui(ms, fluid, i, j, 16, 1);
        RenderSystem.enableDepthTest();
    }

    public static void drawFluidInGui(PoseStack ms, FluidVariant fluid, float i, float j, int scale, float fractionUp) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        int color = FluidVariantRendering.getColor(fluid);

        if (sprite == null)
            return;

        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;
        RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        float x0 = i;
        float y0 = j;
        float x1 = x0 + scale;
        float y1 = y0 + scale * fractionUp;
        float z = 0.5f;
        float u0 = sprite.getU0();
        float v1 = sprite.getV1();
        float v0 = v1 + (sprite.getV0() - v1) * fractionUp;
        float u1 = sprite.getU1();

        Matrix4f model = ms.last().pose();
        bufferBuilder.vertex(model, x0, y1, z).color(r, g, b, 1).uv(u0, v1).endVertex();
        bufferBuilder.vertex(model, x1, y1, z).color(r, g, b, 1).uv(u1, v1).endVertex();
        bufferBuilder.vertex(model, x1, y0, z).color(r, g, b, 1).uv(u1, v0).endVertex();
        bufferBuilder.vertex(model, x0, y0, z).color(r, g, b, 1).uv(u0, v0).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);

        RenderSystem.enableDepthTest();
    }

    /**
     * Return whether the point is within the passed rectangle.
     */
    public static boolean isPointWithinRectangle(int xStart, int yStart, int width, int height, double pointX, double pointY) {
        return pointX >= (double) (xStart - 1) && pointX < (double) (xStart + width + 1) && pointY >= (double) (yStart - 1)
                && pointY < (double) (yStart + height + 1);
    }

    /**
     * Force chunk remesh.
     */
    public static void forceChunkRemesh(Level world, BlockPos pos) {
        world.sendBlockUpdated(pos, null, null, 0);
    }

    private static final float[] DEFAULT_BRIGHTNESSES = new float[] { 1, 1, 1, 1 };

    /**
     * {@link VertexConsumer#putBulkData} copy pasted from vanilla and adapted with support
     * for alpha and less useless allocations.
     */
    public static void quadWithAlpha(VertexConsumer consumer, PoseStack.Pose matrixEntry, BakedQuad quad, float red, float green, float blue,
            float alpha, int light, int overlay) {
        boolean useQuadColorData = false;
        float[] fs = DEFAULT_BRIGHTNESSES;
        int[] js = quad.getVertices();
        Vec3i vec3i = quad.getDirection().getNormal();
        Vector3f vec3f = new Vector3f((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
        Matrix4f matrix4f = matrixEntry.pose();
        vec3f.transform(matrixEntry.normal());
        int j = js.length / 8;
        MemoryStack memoryStack = MemoryStack.stackPush();

        try {
            ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();

            for (int k = 0; k < j; ++k) {
                intBuffer.clear();
                intBuffer.put(js, k * 8, 8);
                float f = byteBuffer.getFloat(0);
                float g = byteBuffer.getFloat(4);
                float h = byteBuffer.getFloat(8);
                float r;
                float s;
                float t;
                float v;
                float w;
                if (useQuadColorData) {
                    float l = (float) (byteBuffer.get(12) & 255) / 255.0F;
                    v = (float) (byteBuffer.get(13) & 255) / 255.0F;
                    w = (float) (byteBuffer.get(14) & 255) / 255.0F;
                    r = l * fs[k] * red;
                    s = v * fs[k] * green;
                    t = w * fs[k] * blue;
                } else {
                    r = fs[k] * red;
                    s = fs[k] * green;
                    t = fs[k] * blue;
                }

                v = byteBuffer.getFloat(16);
                w = byteBuffer.getFloat(20);
                Vector4f vector4f = new Vector4f(f, g, h, 1.0F);
                vector4f.transform(matrix4f);
                consumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), r, s, t, alpha, v, w, overlay, light, vec3f.x(), vec3f.y(),
                        vec3f.z());
            }
        } catch (Throwable var33) {
            if (memoryStack != null) {
                try {
                    memoryStack.close();
                } catch (Throwable var32) {
                    var33.addSuppressed(var32);
                }
            }

            throw var33;
        }

        if (memoryStack != null) {
            memoryStack.close();
        }

    }

    public static void fill(PoseStack matrices, int x1, int y1, int x2, int y2, int color) {
        fill(matrices.last().pose(), x1, y1, x2, y2, color);
    }

    private static void fill(Matrix4f matrix, int x1, int y1, int x2, int y2, int color) {
        int j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

}

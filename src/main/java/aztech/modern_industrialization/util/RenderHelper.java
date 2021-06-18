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

import aztech.modern_industrialization.fluid.CraftingFluid;
import aztech.modern_industrialization.mixin_client.ClientWorldAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidKeyRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;

public class RenderHelper {
    private static final BakedQuad[] OVERLAY_QUADS;
    private static final float W = 0.05f;

    public static void drawOverlay(MatrixStack ms, VertexConsumerProvider vcp, float r, float g, float b, int light, int overlay) {
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getSolid());
        for (BakedQuad overlayQuad : OVERLAY_QUADS) {
            vc.quad(ms.peek(), overlayQuad, r, g, b, light, overlay);
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
            OVERLAY_QUADS[direction.getId() * 4] = emitter.toBakedQuad(0, null, false);
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, 1 - W, 1, 1, 0);
            emitter.material(material);
            OVERLAY_QUADS[direction.getId() * 4 + 1] = emitter.toBakedQuad(0, null, false);
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, W, W, 1 - W, 0);
            emitter.material(material);
            OVERLAY_QUADS[direction.getId() * 4 + 2] = emitter.toBakedQuad(0, null, false);
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 1 - W, W, 1, 1 - W, 0);
            emitter.material(material);
            OVERLAY_QUADS[direction.getId() * 4 + 3] = emitter.toBakedQuad(0, null, false);
        }
    }

    private static final BakedQuad[] CUBE_QUADS;

    public static void drawCube(MatrixStack ms, VertexConsumerProvider vcp, float r, float g, float b, int light, int overlay) {
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getSolid());
        for (BakedQuad cubeQuad : CUBE_QUADS) {
            vc.quad(ms.peek(), cubeQuad, r, g, b, light, overlay);
        }
    }

    static {
        CUBE_QUADS = new BakedQuad[6];
        Renderer r = RendererAccess.INSTANCE.getRenderer();
        for (Direction direction : Direction.values()) {
            QuadEmitter emitter;
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, 0, 1, 1, 0);
            CUBE_QUADS[direction.getId()] = emitter.toBakedQuad(0, null, false);
        }
    }

    private static final float TANK_W = 0.02f;
    public static final int FULL_LIGHT = 0x00F0_00F0;

    public static void drawFluidInTank(MatrixStack ms, VertexConsumerProvider vcp, FluidKey fluid, float fill) {
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getTranslucent());
        Sprite sprite = FluidKeyRendering.getSprite(fluid);
        int color = FluidKeyRendering.getColor(fluid);
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
        if (fluid instanceof CraftingFluid && ((CraftingFluid) fluid).isGas) {
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
            vc.quad(ms.peek(), emitter.toBakedQuad(0, sprite, false), r, g, b, FULL_LIGHT, OverlayTexture.DEFAULT_UV);
        }
    }

    public static void drawFluidInGui(MatrixStack ms, FluidKey fluid, int i, int j) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        Sprite sprite = FluidKeyRendering.getSprite(fluid);
        int color = FluidKeyRendering.getColor(fluid);

        if (sprite == null)
            return;

        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;
        RenderSystem.disableDepthTest();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        float x0 = (float) i;
        float y0 = (float) j;
        float x1 = x0 + 16;
        float y1 = y0 + 16;
        float z = 0.5f;
        float u0 = sprite.getMinU();
        float v0 = sprite.getMinV();
        float u1 = sprite.getMaxU();
        float v1 = sprite.getMaxV();
        Matrix4f model = ms.peek().getModel();
        bufferBuilder.vertex(model, x0, y1, z).color(r, g, b, 1).texture(u0, v1).next();
        bufferBuilder.vertex(model, x1, y1, z).color(r, g, b, 1).texture(u1, v1).next();
        bufferBuilder.vertex(model, x1, y0, z).color(r, g, b, 1).texture(u1, v0).next();
        bufferBuilder.vertex(model, x0, y0, z).color(r, g, b, 1).texture(u0, v0).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

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
    public static void forceChunkRemesh(ClientWorld world, BlockPos pos) {
        ((ClientWorldAccessor) world).getWorldRenderer().updateBlock(null, pos, null, null, 0);
    }
}

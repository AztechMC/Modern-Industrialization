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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.client.MIRenderTypes;
import aztech.modern_industrialization.compat.sodium.SodiumCompat;
import aztech.modern_industrialization.thirdparty.fabricrendering.MutableQuadView;
import aztech.modern_industrialization.thirdparty.fabricrendering.QuadBuffer;
import aztech.modern_industrialization.thirdparty.fabricrendering.QuadEmitter;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.client.fluid.FluidVariantRendering;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

public class RenderHelper {
    private static final Supplier<BakedQuad[]> OVERLAY_QUADS;
    private static final float W = 0.05f;
    private static final MIIdentifier LOCKED_TEXTURE_LOCATION = new MIIdentifier("block/locked");

    public static void drawOverlay(PoseStack ms, MultiBufferSource vcp, float r, float g, float b, int light, int overlay) {
        VertexConsumer vc = vcp.getBuffer(MIRenderTypes.solidHighlight());
        for (BakedQuad overlayQuad : OVERLAY_QUADS.get()) {
            putBulkData(vc, ms.last(), overlayQuad, r, g, b, light, overlay);
        }
    }

    static {
        OVERLAY_QUADS = Suppliers.memoize(() -> {
            var overlayQuads = new BakedQuad[24];
            QuadEmitter emitter = new QuadBuffer();
            for (Direction direction : Direction.values()) {
                emitter.emit();
                emitter.square(direction, 0, 0, 1, W, 0);
                overlayQuads[direction.get3DDataValue() * 4] = emitter.toBakedQuad(null);
                emitter.square(direction, 0, 1 - W, 1, 1, 0);
                overlayQuads[direction.get3DDataValue() * 4 + 1] = emitter.toBakedQuad(null);
                emitter.square(direction, 0, W, W, 1 - W, 0);
                overlayQuads[direction.get3DDataValue() * 4 + 2] = emitter.toBakedQuad(null);
                emitter.square(direction, 1 - W, W, 1, 1 - W, 0);
                overlayQuads[direction.get3DDataValue() * 4 + 3] = emitter.toBakedQuad(null);
            }
            return overlayQuads;
        });
    }

    private static final Supplier<BakedQuad[]> CUBE_QUADS;

    public static void drawCube(PoseStack ms, MultiBufferSource vcp, float r, float g, float b, int light, int overlay) {
        VertexConsumer vc = vcp.getBuffer(MIRenderTypes.solidHighlight());
        for (BakedQuad cubeQuad : CUBE_QUADS.get()) {
            putBulkData(vc, ms.last(), cubeQuad, r, g, b, light, overlay);
        }
    }

    static {
        CUBE_QUADS = Suppliers.memoize(() -> {
            var cubeQuads = new BakedQuad[6];
            for (Direction direction : Direction.values()) {
                QuadEmitter emitter = new QuadBuffer();
                emitter.square(direction, 0, 0, 1, 1, 0);
                cubeQuads[direction.get3DDataValue()] = emitter.toBakedQuad(null);
            }
            return cubeQuads;
        });
    }

    private static final float TANK_W = 1 / 16f + 0.001f;
    public static final int FULL_LIGHT = 0x00F0_00F0;

    public static void drawFluidInTank(BlockEntity be, PoseStack ms, MultiBufferSource vcp, FluidVariant fluid, float fill) {
        drawFluidInTank(be.getLevel(), be.getBlockPos(), ms, vcp, fluid, fill);
    }

    public static void drawFluidInTank(@Nullable Level world, BlockPos pos, PoseStack ms, MultiBufferSource vcp, FluidVariant fluid, float fill) {
        VertexConsumer vc = vcp.getBuffer(Sheets.translucentCullBlockSheet());
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        int color = FluidVariantRendering.getColor(fluid, world, pos);
        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;

        SodiumCompat.markSpriteActive(sprite);

        // Make sure fill is within [TANK_W, 1 - TANK_W]
        fill = TANK_W + (1 - 2 * TANK_W) * Math.min(1, Math.max(fill, 0));
        // Top and bottom positions of the fluid inside the tank
        float topHeight = fill;
        float bottomHeight = TANK_W;
        // Render gas from top to bottom
        if (fluid.getFluid().getFluidType().isLighterThanAir()) {
            topHeight = 1 - TANK_W;
            bottomHeight = 1 - fill;
        }

        var emitter = new QuadBuffer();
        for (Direction direction : Direction.values()) {
            emitter.emit();

            if (direction.getAxis().isVertical()) {
                emitter.square(direction, TANK_W, TANK_W, 1 - TANK_W, 1 - TANK_W, direction == Direction.UP ? 1 - topHeight : bottomHeight);
            } else {
                emitter.square(direction, TANK_W, bottomHeight, 1 - TANK_W, topHeight, TANK_W);
            }

            emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.color(-1, -1, -1, -1);
            vc.putBulkData(ms.last(), emitter.toBakedQuad(sprite), r, g, b, FULL_LIGHT, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void drawFluidInGui(GuiGraphics guiGraphics, FluidVariant fluid, int i, int j) {
        drawFluidInGui(guiGraphics, fluid, i, j, 16, 1);
        RenderSystem.enableDepthTest();
    }

    public static void drawFluidInGui(GuiGraphics guiGraphics, FluidVariant fluid, float i, float j, int scale, float fractionUp) {
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

        Matrix4f model = guiGraphics.pose().last().pose();
        bufferBuilder.vertex(model, x0, y1, z).color(r, g, b, 1).uv(u0, v1).endVertex();
        bufferBuilder.vertex(model, x1, y1, z).color(r, g, b, 1).uv(u1, v1).endVertex();
        bufferBuilder.vertex(model, x1, y0, z).color(r, g, b, 1).uv(u1, v0).endVertex();
        bufferBuilder.vertex(model, x0, y0, z).color(r, g, b, 1).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.enableDepthTest();

        SodiumCompat.markSpriteActive(sprite);
    }

    /**
     * Return whether the point is within the passed rectangle.
     */
    public static boolean isPointWithinRectangle(int xStart, int yStart, int width, int height, double pointX, double pointY) {
        return pointX >= (double) (xStart - 1) && pointX < (double) (xStart + width + 1) && pointY >= (double) (yStart - 1)
                && pointY < (double) (yStart + height + 1);
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
        vec3f.mul(matrixEntry.normal());
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
                vector4f.mul(matrix4f);
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

    // Literally the exact same as VertexConsumer#putBulkData but copy/pasted, to avoid the null sprite tripping up Sodium in its mixin to mark
    // sprites as active...
    // Remove once fixed in Sodium
    private static void putBulkData(VertexConsumer vc, PoseStack.Pose poseEntry, BakedQuad quad, float red, float green, float blue,
            int combinedLight, int combinedOverlay) {
        RenderHelper.putBulkData(vc, poseEntry, quad, new float[] { 1.0F, 1.0F, 1.0F, 1.0F }, red, green, blue,
                new int[] { combinedLight, combinedLight, combinedLight, combinedLight }, combinedOverlay, false);
    }

    private static void putBulkData(VertexConsumer vc, PoseStack.Pose poseEntry, BakedQuad quad, float[] colorMuls, float red, float green,
            float blue, int[] combinedLights, int combinedOverlay, boolean mulColor) {
        float[] fs = new float[] { colorMuls[0], colorMuls[1], colorMuls[2], colorMuls[3] };
        int[] is = new int[] { combinedLights[0], combinedLights[1], combinedLights[2], combinedLights[3] };
        int[] js = quad.getVertices();
        Vec3i vec3i = quad.getDirection().getNormal();
        Matrix4f matrix4f = poseEntry.pose();
        Vector3f vector3f = poseEntry.normal().transform(new Vector3f((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ()));
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
                float o;
                float p;
                float q;
                float m;
                float n;
                if (mulColor) {
                    float l = (float) (byteBuffer.get(12) & 255) / 255.0F;
                    m = (float) (byteBuffer.get(13) & 255) / 255.0F;
                    n = (float) (byteBuffer.get(14) & 255) / 255.0F;
                    o = l * fs[k] * red;
                    p = m * fs[k] * green;
                    q = n * fs[k] * blue;
                } else {
                    o = fs[k] * red;
                    p = fs[k] * green;
                    q = fs[k] * blue;
                }

                int r = is[k];
                m = byteBuffer.getFloat(16);
                n = byteBuffer.getFloat(20);
                Vector4f vector4f = matrix4f.transform(new Vector4f(f, g, h, 1.0F));
                vc.vertex(vector4f.x(), vector4f.y(), vector4f.z(), o, p, q, 1.0F, m, n, combinedOverlay, r, vector3f.x(), vector3f.y(),
                        vector3f.z());
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
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(g, h, k, f).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawLockedTexture(BlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, int colorRgb) {
        VertexConsumer vc = vertexConsumers.getBuffer(Sheets.cutoutBlockSheet());
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(LOCKED_TEXTURE_LOCATION);
        // draw the sprite on each face

        var pos = entity.getBlockPos();
        var state = entity.getBlockState();
        float r = (colorRgb >> 16 & 255) / 255.0F;
        float g = (colorRgb >> 8 & 255) / 255.0F;
        float b = (colorRgb & 255) / 255.0F;

        var emitter = new QuadBuffer();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isVertical() ||
            // Note: level can be null from builtin item renderer
                    entity.getLevel() != null && !Block.shouldRenderFace(state, entity.getLevel(), pos,
                            direction.getOpposite(), pos.relative(direction.getOpposite()))) {
                continue;
            }

            emitter.emit();
            emitter.square(direction, 1, 0, 0, 1, 1.015f);
            emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);

            vc.putBulkData(matrices.last(),
                    emitter.toBakedQuad(sprite),
                    r, g, b, RenderHelper.FULL_LIGHT, OverlayTexture.NO_OVERLAY);
        }
    }

    public static final BlockEntityWithoutLevelRenderer BLOCK_AND_ENTITY_RENDERER = new BlockEntityWithoutLevelRenderer(null, null) {
        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack matrices, MultiBufferSource vertexConsumers, int light,
                int overlay) {
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                throw new IllegalArgumentException("Stack must be a block item!");
            }
            if (!(blockItem.getBlock() instanceof EntityBlock entityBlock)) {
                throw new IllegalArgumentException("Block must be an entity block!");
            }

            var fakeBlockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, blockItem.getBlock().defaultBlockState());
            var tag = Objects.requireNonNullElseGet(stack.getTagElement("BlockEntityTag"), CompoundTag::new);
            Objects.requireNonNull(fakeBlockEntity).load(tag);

            // Render the base block first
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(fakeBlockEntity.getBlockState(), matrices, vertexConsumers, light, overlay);
            // Render additional data using the block entity renderer
            var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(fakeBlockEntity);
            Objects.requireNonNull(renderer).render(fakeBlockEntity, Minecraft.getInstance().getFrameTime(), matrices, vertexConsumers, light,
                    overlay);
        }
    };

    public static void renderVoxelShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red,
            float green, float blue, float alpha) {
        for (AABB aabb : shape.toAabbs()) {
            LevelRenderer.renderShape(poseStack, consumer, Shapes.create(aabb), x, y, z, red, green, blue, alpha);
        }
    }

    public static void renderAndDecorateItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        renderAndDecorateItem(guiGraphics, Minecraft.getInstance().font, stack, x, y);
    }

    public static void renderAndDecorateItem(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y) {
        renderAndDecorateItem(guiGraphics, font, stack, x, y, null);
    }

    public static void renderAndDecorateItem(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y, @Nullable String text) {
        guiGraphics.renderItem(stack, x, y);
        guiGraphics.renderItemDecorations(font, stack, x, y, text);
    }
}

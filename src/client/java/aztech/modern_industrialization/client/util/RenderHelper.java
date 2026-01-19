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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.client.MIRenderTypes;
import aztech.modern_industrialization.client.compat.sodium.SodiumCompat;
import aztech.modern_industrialization.client.thirdparty.fabrictransfer.FluidVariantRendering;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
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
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class RenderHelper {
    private static final QuadCube overlayQuads = new QuadCube(MI.id("block/hatch_placement_overlay"));

    // TODO 26.1
//    public static void drawOverlay(PoseStack ms, MultiBufferSource vcp, int overlay) {
//        VertexConsumer vc = vcp.getBuffer(MIRenderTypes.cutoutHighlight());
//        for (BakedQuad overlayQuad : overlayQuads.getQuads()) {
//            vc.putBulkData(ms.last(), overlayQuad, 1.0f, 1.0f, 1.0f, 1.0f, LightTexture.FULL_BRIGHT, /* not used by shader */ overlay);
//        }
//    }

    private static final QuadCube whiteQuads = new QuadCube(Identifier.fromNamespaceAndPath("neoforge", "white"));

    // TODO 26.1
//    public static void drawCube(PoseStack ms, MultiBufferSource vcp, float r, float g, float b, int light, int overlay) {
//        VertexConsumer vc = vcp.getBuffer(MIRenderTypes.cutoutHighlight());
//        for (BakedQuad cubeQuad : whiteQuads.getQuads()) {
//            vc.putBulkData(ms.last(), cubeQuad, r, g, b, 1.0f, light, overlay);
//        }
//    }

    private static final float TANK_W = 1 / 16f + 0.001f;
    public static final int FULL_LIGHT = 0x00F0_00F0;

    public static void drawFluidInTank(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, FluidVariant fluid, float fillLevel, int color) {
        submitNodeCollector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(), (pose, vc) -> {
            TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
            if (sprite == null) {
                return;
            }
            float r = ((color >> 16) & 255) / 256f;
            float g = ((color >> 8) & 255) / 256f;
            float b = (color & 255) / 256f;

            SodiumCompat.markSpriteActive(sprite);

            // Make sure fill is within [TANK_W, 1 - TANK_W]
            float fill = fillLevel;
            fill = TANK_W + (1 - 2 * TANK_W) * Math.min(1, Math.max(fill, 0));
            // Top and bottom positions of the fluid inside the tank
            float topHeight = fill;
            float bottomHeight = TANK_W;
            // Render gas from top to bottom
            if (fluid.getFluid().getFluidType().isLighterThanAir()) {
                topHeight = 1 - TANK_W;
                bottomHeight = 1 - fill;
            }

            for (Direction direction : Direction.values()) {
                Vector3f[] pos = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                if (direction.getAxis().isVertical()) {
                    ModelHelper.square(pos, direction, TANK_W, TANK_W, 1 - TANK_W, 1 - TANK_W, direction == Direction.UP ? 1 - topHeight : bottomHeight);
                } else {
                    ModelHelper.square(pos, direction, TANK_W, bottomHeight, 1 - TANK_W, topHeight, TANK_W);
                }

                long[] uv = ModelHelper.bakeUvs(pos, sprite, direction);

                var quad = new BakedQuad(
                        pos[0], pos[1], pos[2], pos[3],
                        uv[0], uv[1], uv[2], uv[3],
                        -1, direction, sprite, true, 0,
                        BakedNormals.of(BakedNormals.pack(direction.getUnitVec3f())), BakedColors.DEFAULT, true);

                vc.putBulkData(pose, quad, r, g, b, 1, FULL_LIGHT, OverlayTexture.NO_OVERLAY);
            }
        });
    }

    public static void drawFluidInGui(GuiGraphics guiGraphics, FluidVariant fluid, int x0, int y0) {
        drawFluidInGui(guiGraphics, fluid, x0, y0, 16, 1);
    }

    public static void drawFluidInGui(GuiGraphics guiGraphics, FluidVariant fluid, int x0, int y0, int scale, float fractionUp) {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        int color = FluidVariantRendering.getColor(fluid);

        if (sprite == null)
            return;

        int x1 = x0 + scale;
        int y1 = y0 + Math.round(scale * fractionUp);
        float u0 = sprite.getU0();
        float v1 = sprite.getV1();
        float v0 = v1 + (sprite.getV0() - v1) * fractionUp;
        float u1 = sprite.getU1();

        guiGraphics.innerBlit(RenderPipelines.GUI_TEXTURED, TextureAtlas.LOCATION_BLOCKS, x0, x1, y0, y1, u0, u1, v0, v1, color | 0xFF000000);

        SodiumCompat.markSpriteActive(sprite);
    }

    /**
     * Return whether the point is within the passed rectangle.
     */
    public static boolean isPointWithinRectangle(int xStart, int yStart, int width, int height, double pointX, double pointY) {
        return pointX >= (double) (xStart - 1) && pointX < (double) (xStart + width + 1) && pointY >= (double) (yStart - 1)
                && pointY < (double) (yStart + height + 1);
    }

    public static void quadWithAlpha(VertexConsumer consumer, PoseStack.Pose matrixEntry, BakedQuad quad, float red, float green, float blue,
            float alpha, int light, int overlay) {
        consumer.putBulkData(matrixEntry, quad, red, green, blue, alpha, light, overlay);
    }

    private static final Material LOCKED_TEXTURE_LOCATION = ClientHooks.getBlockMaterial(MI.id("block/locked"));

    public static void drawLockedTexture(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int colorRgb) {
        submitNodeCollector.submitCustomGeometry(poseStack, Sheets.cutoutBlockSheet(), (pose, vc) -> {
            var sprite = Minecraft.getInstance().getAtlasManager().get(LOCKED_TEXTURE_LOCATION);
            // draw the sprite on each face

            float r = (colorRgb >> 16 & 255) / 255.0F;
            float g = (colorRgb >> 8 & 255) / 255.0F;
            float b = (colorRgb & 255) / 255.0F;

            for (Direction direction : Direction.values()) {
                if (direction.getAxis().isVertical()) {
                    // TODO 26.1: it would be nice to restore this?
//            // Note: level can be null from builtin item renderer
//                    entity.getLevel() != null && !Block.shouldRenderFace(state, entity.getLevel(), pos,
//                            direction.getOpposite(), pos.relative(direction.getOpposite()))) {
                    continue;
                }

                Vector3f[] pos = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                ModelHelper.square(pos, direction, 1, 0, 0, 1, 1.015f);

                long[] uv = ModelHelper.bakeUvs(pos, sprite, direction);

                var quad = new BakedQuad(
                        pos[0], pos[1], pos[2], pos[3],
                        uv[0], uv[1], uv[2], uv[3],
                        -1, direction, sprite, true, 0,
                        BakedNormals.of(BakedNormals.pack(direction.getUnitVec3f())), BakedColors.DEFAULT, true);

                vc.putBulkData(pose,
                        quad,
                        r, g, b, 1, RenderHelper.FULL_LIGHT, OverlayTexture.NO_OVERLAY);
            }
        });
    }

    public static void renderVoxelShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red,
            float green, float blue, float alpha) {
        for (AABB aabb : shape.toAabbs()) {
            ShapeRenderer.renderShape(poseStack, consumer, Shapes.create(aabb), x, y, z, ARGB.colorFromFloat(alpha, red, green, blue), Minecraft.getInstance().getWindow().getAppropriateLineWidth());
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

    public static List<FormattedCharSequence> splitTooltip(List<Component> components) {
        List<FormattedCharSequence> charSequences = new ArrayList<>();
        for (var component : components) {
            charSequences.addAll(Tooltip.splitTooltip(Minecraft.getInstance(), component));
        }
        return charSequences;
    }
}

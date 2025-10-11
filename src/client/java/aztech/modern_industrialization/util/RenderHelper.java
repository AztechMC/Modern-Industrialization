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

import aztech.modern_industrialization.MI;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.client.RenderTypeHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class RenderHelper {
    private static final ResourceLocation HATCH_PLACEMENT_OVERLAY_LOCATION = MI.id("block/hatch_placement_overlay");
    @Nullable
    private static OverlayQuads overlayQuads;

    private record OverlayQuads(BakedQuad[] quads, TextureAtlasSprite sprite) {
    }

    public static void drawOverlay(PoseStack ms, MultiBufferSource vcp, int overlay) {
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(HATCH_PLACEMENT_OVERLAY_LOCATION);
        if (overlayQuads == null || overlayQuads.sprite() != sprite) {
            overlayQuads = new OverlayQuads(buildOverlayQuads(sprite), sprite);
        }
        VertexConsumer vc = vcp.getBuffer(MIRenderTypes.cutoutHighlight());
        for (BakedQuad overlayQuad : overlayQuads.quads) {
            vc.putBulkData(ms.last(), overlayQuad, 1.0f, 1.0f, 1.0f, 1.0f, LightTexture.FULL_BRIGHT, /* not used by shader */ overlay);
        }
    }

    private static BakedQuad[] buildOverlayQuads(TextureAtlasSprite sprite) {
        var overlayQuads = new BakedQuad[6];
        QuadEmitter emitter = new QuadBuffer();
        for (Direction direction : Direction.values()) {
            emitter.emit();
            emitter.square(direction, 0, 0, 1, 1, 0);
            emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
            overlayQuads[direction.get3DDataValue()] = emitter.toBakedQuad(sprite);
        }
        return overlayQuads;
    }

    private static final Supplier<BakedQuad[]> CUBE_QUADS;

    public static void drawCube(PoseStack ms, MultiBufferSource vcp, float r, float g, float b, int light, int overlay) {
        VertexConsumer vc = vcp.getBuffer(MIRenderTypes.cutoutHighlight());
        for (BakedQuad cubeQuad : CUBE_QUADS.get()) {
            vc.putBulkData(ms.last(), cubeQuad, r, g, b, 1.0f, light, overlay);
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
        VertexConsumer vc = vcp.getBuffer(RenderTypeHelper.getEntityRenderType(RenderType.translucent(), false));
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
            vc.putBulkData(ms.last(), emitter.toBakedQuad(sprite), r, g, b, 1, FULL_LIGHT, OverlayTexture.NO_OVERLAY);
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

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
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
        bufferBuilder.addVertex(model, x0, y1, z).setUv(u0, v1).setColor(r, g, b, 1);
        bufferBuilder.addVertex(model, x1, y1, z).setUv(u1, v1).setColor(r, g, b, 1);
        bufferBuilder.addVertex(model, x1, y0, z).setUv(u1, v0).setColor(r, g, b, 1);
        bufferBuilder.addVertex(model, x0, y0, z).setUv(u0, v0).setColor(r, g, b, 1);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

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

    public static void quadWithAlpha(VertexConsumer consumer, PoseStack.Pose matrixEntry, BakedQuad quad, float red, float green, float blue,
            float alpha, int light, int overlay) {
        consumer.putBulkData(matrixEntry, quad, red, green, blue, alpha, light, overlay);
    }

    private static final ResourceLocation LOCKED_TEXTURE_LOCATION = MI.id("block/locked");

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
                    r, g, b, 1, RenderHelper.FULL_LIGHT, OverlayTexture.NO_OVERLAY);
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
            Objects.requireNonNull(fakeBlockEntity);
            fakeBlockEntity.applyComponentsFromItemStack(stack);

            // Render the base block first
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(fakeBlockEntity.getBlockState(), matrices, vertexConsumers, light, overlay);
            // Render additional data using the block entity renderer
            var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(fakeBlockEntity);
            Objects.requireNonNull(renderer).render(fakeBlockEntity, 0.0f, matrices, vertexConsumers, light,
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

    public static List<FormattedCharSequence> splitTooltip(List<Component> components) {
        List<FormattedCharSequence> charSequences = new ArrayList<>();
        for (var component : components) {
            charSequences.addAll(Tooltip.splitTooltip(Minecraft.getInstance(), component));
        }
        return charSequences;
    }
}

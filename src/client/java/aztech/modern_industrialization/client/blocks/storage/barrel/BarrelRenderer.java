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

package aztech.modern_industrialization.client.blocks.storage.barrel;

import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlockEntity;
import aztech.modern_industrialization.client.util.RenderHelper;
import aztech.modern_industrialization.config.MIClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BarrelRenderer implements BlockEntityRenderer<BarrelBlockEntity, BarrelRenderState> {
    private static final ThreadLocal<Integer> barrelNesting = ThreadLocal.withInitial(() -> 0);

    private final ItemModelResolver itemModelResolver;
    private final int itemNameColor;

    public BarrelRenderer(ItemModelResolver itemModelResolver, int itemNameColor) {
        this.itemModelResolver = itemModelResolver;
        this.itemNameColor = itemNameColor;
    }

    @Override
    public BarrelRenderState createRenderState() {
        return new BarrelRenderState();
    }

    @Override
    public void extractRenderState(BarrelBlockEntity barrel, BarrelRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(barrel, state, partialTicks, cameraPosition, breakProgress);
        state.locked = barrel.isLocked();
        this.itemModelResolver.updateForTopItem(
                state.stackRenderState, barrel.getResource(0).toStack(), ItemDisplayContext.GUI, barrel.getLevel(), null, 0);
    }

    @Override
    public void submit(BarrelRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.locked) {
            RenderHelper.drawLockedTexture(poseStack, submitNodeCollector, itemNameColor);
        }

        if (!MIClientConfig.INSTANCE.barrelContentRendering.getAsBoolean()) {
            return;
        }
        // TODO: this check is likely misplaced
        int nesting = barrelNesting.get();
        if (nesting >= 4) {
            return;
        }

        barrelNesting.set(nesting + 1);

        try {
//            var state = entity.getBlockState();
//            var pos = entity.getBlockPos();


            if (!state.stackRenderState.isEmpty()) {
                int sideMask = 0;

                for (int i = 0; i < 4; i++) {
//                    var direction = Direction.from2DDataValue(i);
                    // TODO 26.1: it would be nice to restore this?
//                    // Note: level can be null from builtin item renderer
//                    if (entity.getLevel() != null
//                            && !Block.shouldRenderFace(state, entity.getLevel(), pos, direction, pos.relative(direction))) {
//                        continue;
//                    }

                    sideMask |= 1 << i;
                    // Thanks TechReborn for rendering code

                    poseStack.pushPose();
                    poseStack.translate(0.5, 0, 0.5);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-i * 90F));
                    poseStack.scale(0.5F, 0.5F, 0.5F);
                    poseStack.translate(0, 1.125, 1.01);

                    poseStack.last().pose().scale(1, 1, 0.01f);
                    poseStack.last().normal().rotate(Mth.HALF_PI / 2, -1, 0, 0);

                    state.stackRenderState.submit(
                            poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

                    poseStack.popPose();
                }

                // TODO 26.1
//                DeferredBarrelTextRenderer.enqueueBarrelForRendering(pos, sideMask, itemNameColor);
            }
        } finally {
            barrelNesting.set(nesting);
        }
    }
}

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

package aztech.modern_industrialization.client.machines.multiblocks;

import aztech.modern_industrialization.client.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

public class MultiblockHighlight {
    private static final Map<BlockPos, Highlight> highlightQueue = new HashMap<>();
    private static final MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(new ByteBufferBuilder(128));
    private static final VertexSorting reverseDistanceToOrigin = RenderHelper.reverseVertexSorting(VertexSorting.DISTANCE_TO_ORIGIN);

    public static void init() {
        NeoForge.EVENT_BUS.addListener(MultiblockHighlight::end);
    }

    public static void enqueueErrorHighlight(BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        highlightQueue.put(pos.immutable(), new ErrorHighlight(state != null ? Pair.of(state, blockEntity) : null));
    }

    public static void enqueueHatchHighlight(BlockPos pos) {
        highlightQueue.put(pos.immutable(), new HatchHighlight());
    }

    private static void end(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        if (!highlightQueue.isEmpty()) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            var poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.mulPose(event.getModelViewMatrix());
            for (var entry : highlightQueue.entrySet()) {
                poseStack.pushPose();
                Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                BlockPos pos = entry.getKey();
                double x = pos.getX() - cameraPos.x;
                double y = pos.getY() - cameraPos.y;
                double z = pos.getZ() - cameraPos.z;
                poseStack.translate(x, y, z);
                entry.getValue().render(poseStack);
                poseStack.popPose();
            }
            poseStack.popPose();
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(RenderSystem.getProjectionMatrix(), reverseDistanceToOrigin);
            immediate.endBatch();
            RenderSystem.restoreProjectionMatrix();
            highlightQueue.clear();
        }
    }

    private interface Highlight {
        void render(PoseStack poseStack);
    }

    private record ErrorHighlight(@Nullable Pair<BlockState, @Nullable BlockEntity> value) implements Highlight {
        @Override
        public void render(PoseStack poseStack) {
            poseStack.translate(0.25, 0.25, 0.25);
            poseStack.scale(0.5f, 0.5f, 0.5f);

            if (value == null) {
                RenderHelper.drawCube(poseStack, immediate, 1, 50f / 256, 50f / 256, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            } else {
                BlockState state = value.getFirst();
                BlockEntity blockEntity = value.getSecond();
                var berDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
                if (blockEntity != null && berDispatcher.getRenderer(blockEntity) != null) {
                    berDispatcher.render(blockEntity, 0, poseStack, immediate);
                } else {
                    ModelData modelData = blockEntity == null ? ModelData.EMPTY : blockEntity.getModelData();
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, immediate, 15728880, OverlayTexture.NO_OVERLAY, modelData, null);
                }
            }
        }
    }

    private record HatchHighlight() implements Highlight {
        @Override
        public void render(PoseStack poseStack) {
            poseStack.translate(-0.005, -0.005, -0.005);
            poseStack.scale(1.01f, 1.01f, 1.01f);
            RenderHelper.drawOverlay(poseStack, immediate, OverlayTexture.NO_OVERLAY);
        }
    }
}

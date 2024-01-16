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
package aztech.modern_industrialization.machines.multiblocks;

import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class MultiblockErrorHighlight {
    private static final Map<BlockPos, @Nullable BlockState> highlightQueue = new HashMap<>();
    private static final MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(new BufferBuilder(128));

    public static void init() {
        NeoForge.EVENT_BUS.addListener(MultiblockErrorHighlight::end);
    }

    public static void enqueueHighlight(BlockPos pos, @Nullable BlockState state) {
        highlightQueue.put(pos.immutable(), state);
    }

    private static void end(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        if (highlightQueue.size() > 0) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            var poseStack = event.getPoseStack();
            for (Map.Entry<BlockPos, @Nullable BlockState> entry : highlightQueue.entrySet()) {
                poseStack.pushPose();
                Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                BlockPos pos = entry.getKey();
                double x = pos.getX() - cameraPos.x;
                double y = pos.getY() - cameraPos.y;
                double z = pos.getZ() - cameraPos.z;
                poseStack.translate(x + 0.25, y + 0.25, z + 0.25);
                poseStack.scale(0.5f, 0.5f, 0.5f);

                BlockState state = entry.getValue();
                if (state == null) {
                    RenderHelper.drawCube(poseStack, immediate, 1, 50f / 256, 50f / 256, 15728880, OverlayTexture.NO_OVERLAY);
                } else {
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, immediate, 15728880,
                            OverlayTexture.NO_OVERLAY);
                }

                poseStack.popPose();
            }
            immediate.endBatch();
            highlightQueue.clear();
        }
    }
}

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
package aztech.modern_industrialization.machinesv2.multiblocks;

import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class MultiblockErrorHighlight {
    private static final Map<BlockPos, @Nullable BlockState> highlightQueue = new HashMap<>();
    private static final VertexConsumerProvider.Immediate immediate;

    public static void init() {
        WorldRenderEvents.END.register(MultiblockErrorHighlight::end);
    }

    public static void enqueueHighlight(BlockPos pos, @Nullable BlockState state) {
        highlightQueue.put(pos.toImmutable(), state);
    }

    private static void end(WorldRenderContext wrc) {
        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
        for (Map.Entry<BlockPos, @Nullable BlockState> entry : highlightQueue.entrySet()) {
            wrc.matrixStack().push();
            Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
            BlockPos pos = entry.getKey();
            double x = pos.getX() - cameraPos.x;
            double y = pos.getY() - cameraPos.y;
            double z = pos.getZ() - cameraPos.z;
            wrc.matrixStack().translate(x + 0.25, y + 0.25, z + 0.25);
            wrc.matrixStack().scale(0.5f, 0.5f, 0.5f);

            BlockState state = entry.getValue();
            if (state == null) {
                RenderHelper.drawCube(wrc.matrixStack(), immediate, 1, 50f / 256, 50f / 256, 15728880, OverlayTexture.DEFAULT_UV);
            } else {
                MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state, wrc.matrixStack(), immediate, 15728880,
                        OverlayTexture.DEFAULT_UV);
            }

            wrc.matrixStack().pop();
        }
        highlightQueue.clear();
        immediate.draw();
    }

    static {
        immediate = VertexConsumerProvider.immediate(new BufferBuilder(128));
    }
}

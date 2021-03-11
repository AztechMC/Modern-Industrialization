package aztech.modern_industrialization.machinesv2.multiblocks;

import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
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

import java.util.HashMap;
import java.util.Map;

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
            wrc.matrixStack().translate(x+0.25, y+0.25, z+0.25);
            wrc.matrixStack().scale(0.5f, 0.5f, 0.5f);

            BlockState state = entry.getValue();
            if (state == null) {
                RenderHelper.drawCube(wrc.matrixStack(), immediate, 1, 50f / 256, 50f / 256, 15728880, OverlayTexture.DEFAULT_UV);
            } else {
                MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state,
                        wrc.matrixStack(), immediate, 15728880, OverlayTexture.DEFAULT_UV);
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

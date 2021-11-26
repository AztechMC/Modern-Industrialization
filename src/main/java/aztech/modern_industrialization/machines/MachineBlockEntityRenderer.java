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
package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.machines.models.MachineBakedModel;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Renders an overlay if the machine is active.
 */
public class MachineBlockEntityRenderer<T extends MachineBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockModels blockModels;
    private BlockState lastBlockState = null;
    private MachineBakedModel model = null;
    private final BakedQuad[] cachedQuads = new BakedQuad[36];
    private final boolean[] isQuadCached = new boolean[36];

    public MachineBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.blockModels = ctx.getRenderManager().getModels();
    }

    @Nullable
    private BakedQuad getCachedQuad(MachineModelClientData data, Direction d) {
        int cachedQuadIndex = data.frontDirection.ordinal() * 6 + d.ordinal();

        if (!isQuadCached[cachedQuadIndex]) {
            Renderer renderer = RendererAccess.INSTANCE.getRenderer();
            QuadEmitter emitter = renderer.meshBuilder().getEmitter();

            Sprite sprite = model.getSprite(d, data.frontDirection, true);
            if (sprite != null) {
                emitter.material(model.cutoutMaterial);
                emitter.square(d, 0, 0, 1, 1, -2e-4f); // non-active face is -1e-6f, so we override it.
                emitter.cullFace(d);
                emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
                emitter.spriteColor(0, -1, -1, -1, -1);
                cachedQuads[cachedQuadIndex] = emitter.toBakedQuad(0, sprite, false);
            } else {
                cachedQuads[cachedQuadIndex] = null;
            }
            isQuadCached[cachedQuadIndex] = true;
        }

        return cachedQuads[cachedQuadIndex];
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay) {
        BlockState state = entity.getCachedState();
        if (lastBlockState == null) {
            lastBlockState = state;
            model = (MachineBakedModel) blockModels.getModel(state);
        } else if (lastBlockState != state) {
            // Sanity check.
            throw new IllegalStateException("Tried to use the same machine BER with two block states: " + state + " and " + lastBlockState);
        }

        MachineModelClientData data = entity.getModelData();
        if (data.isActive) {
            Direction facingDirection = data.frontDirection;

            VertexConsumer vc = vcp.getBuffer(RenderLayer.getCutout());

            for (Direction d : Direction.values()) {
                BakedQuad quad = getCachedQuad(data, d);
                if (quad != null) {
                    int faceLight = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getCachedState(), entity.getPos().offset(d));
                    vc.quad(matrices.peek(), quad, 1.0f, 1.0f, 1.0f, faceLight, OverlayTexture.DEFAULT_UV);
                }
            }
        }
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }
}

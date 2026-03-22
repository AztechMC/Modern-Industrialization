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

package aztech.modern_industrialization.client.blocks.storage.tank;

import aztech.modern_industrialization.blocks.storage.tank.AbstractTankBlockEntity;
import aztech.modern_industrialization.client.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TankRenderer implements BlockEntityRenderer<AbstractTankBlockEntity, TankRenderState> {
    private final int lockIconColor;

    public TankRenderer(int lockIconColor) {
        this.lockIconColor = lockIconColor;
    }

    @Override
    public TankRenderState createRenderState() {
        return new TankRenderState();
    }

    @Override
    public void extractRenderState(AbstractTankBlockEntity tank, TankRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tank, state, partialTicks, cameraPosition, breakProgress);
        state.resource = tank.getResource(0);
        state.fluidColor = RenderHelper.getFluidColor(state.resource, tank.getLevel(), tank.getBlockPos());;
        if (!tank.getResource(0).isEmpty()) {
            if (tank.behaviour.isCreative()) {
                state.fillLevel = 1;
            } else if (tank.getAmountAsLong(0) > 0) {
                state.fillLevel = (float) tank.getAmountAsLong(0) / tank.getCapacityAsLong(0, state.resource);
            } else if (tank.isLocked()) {
                state.fillLevel = 0.01f;
            } else {
                state.fillLevel = 0;
            }
        }
        state.locked = tank.isLocked();
    }

    @Override
    public void submit(TankRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.resource.isEmpty() && state.fillLevel > 0) {
            RenderHelper.drawFluidInTank(poseStack, submitNodeCollector, state.resource, state.fillLevel, state.fluidColor);
        }
        if (state.locked) {
            RenderHelper.drawLockedTexture(poseStack, submitNodeCollector, lockIconColor);
        }
    }
}

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

package aztech.modern_industrialization.client.machines;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.client.compat.sodium.SodiumCompat;
import aztech.modern_industrialization.client.machines.models.MachineBlockStateModel;
import aztech.modern_industrialization.client.util.ModelHelper;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.IdentityHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jspecify.annotations.Nullable;

/**
 * Renders an overlay if the machine is active.
 */
public class MachineBlockEntityRenderer<T extends MachineBlockEntity> implements BlockEntityRenderer<T, MachineRenderState> {
    private final BlockModelSet blockModels;
    @Nullable
    private BlockState lastBlockState = null;
    @Nullable
    private MachineBlockStateModel model = null;
    private final IdentityHashMap<MachineCasing, Object[]> quadCache = new IdentityHashMap<>();
    private static final Object NO_QUAD = new Object();

    @Nullable
    private static final MethodHandle UNWRAP_BAKED_MODEL;
    static {
        MethodHandle unwrapBakedModel = null;
        // TODO Continuity support
//        // Support for Continuity's model wrapping
//        if (ModList.get().isLoaded("fabric_renderer_api_v1")) {
//            try {
//                var wrapperBakedModel = Class.forName("net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel");
//                var unwrap = wrapperBakedModel.getMethod("unwrap", BakedModel.class);
//                unwrapBakedModel = MethodHandles.lookup().unreflect(unwrap);
//            } catch (ReflectiveOperationException e) {
//                LogUtils.getLogger().error("Failed to reflect WrapperBakedModel.unwrap method", e);
//            }
//        }
        UNWRAP_BAKED_MODEL = unwrapBakedModel;
    }

    public MachineBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.blockModels = Minecraft.getInstance().getModelManager().getBlockModelSet();
    }

    @Override
    public MachineRenderState createRenderState() {
        return new MachineRenderState();
    }

    @Override
    public void extractRenderState(T machine, MachineRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(machine, state, partialTicks, cameraPosition, breakProgress);

        BlockState blockState = machine.getBlockState();
        if (lastBlockState == null) {
            lastBlockState = blockState;
            model = getMachineModel(blockState);
        } else if (lastBlockState != blockState) {
            // Sanity check.
            throw new IllegalStateException("Tried to use the same machine BER with two block states: " + blockState + " and " + lastBlockState);
        }

        MachineModelClientData data = machine.getMachineModelData();
        if (data.isActive) {
            for (Direction d : Direction.values()) {
                BakedQuad quad = getCachedQuad(data, d);
                state.activeOverlays[d.get3DDataValue()].quad = quad;
                if (quad != null) {
                    state.activeOverlays[d.get3DDataValue()].packedLight = LightCoordsUtil.getLightCoords(LightCoordsUtil.BrightnessGetter.DEFAULT, machine.getLevel(), machine.getBlockState(), machine.getBlockPos().relative(d));
                }
            }

        } else {
            for (Direction d : Direction.values()) {
                state.activeOverlays[d.get3DDataValue()].quad = null;
            }
        }
    }

    @Override
    public void submit(MachineRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        for (Direction d : Direction.values()) {
            var activeOverlay = state.activeOverlays[d.get3DDataValue()];
            var quad = activeOverlay.quad;
            if (quad == null) {
                continue;
            }
            int packedLight = activeOverlay.packedLight;
            submitNodeCollector.submitCustomGeometry(poseStack, Sheets.cutoutBlockItemSheet(), (pose, vc) -> {
                var quadInstance = new QuadInstance();
                quadInstance.setLightCoords(packedLight);
                vc.putBakedQuad(pose, quad, quadInstance);
                // TODO 26.1
//                SodiumCompat.markSpriteActive(quad.sprite());
            });
        }
    }

    @Nullable
    private BakedQuad getCachedQuad(MachineModelClientData data, Direction d) {
        var facing = data.frontDirection;
        if (data.frontDirection == null) {
            return null;
        }
        int cachedQuadIndex = facing.ordinal() * 6 + d.ordinal();
        var casing = data.casing;
        var cachedQuads = quadCache.computeIfAbsent(casing, c -> new Object[36]);

        if (cachedQuads[cachedQuadIndex] == null) {
            var sprite = model == null ? null : MachineBlockStateModel.getSprite(model.getSprites(casing), d, facing, true);
            if (sprite != null) {
                cachedQuads[cachedQuadIndex] = ModelHelper.bakeSprite(d, sprite.sprite(), -2 * MachineBlockStateModel.Z_OFFSET);
            } else {
                cachedQuads[cachedQuadIndex] = NO_QUAD;
            }
        }

        var quad = cachedQuads[cachedQuadIndex];
        return quad == NO_QUAD ? null : (BakedQuad) quad;
    }

    @Nullable
    private MachineBlockStateModel getMachineModel(BlockState state) {
        var model = blockModels.get(state);

        if (UNWRAP_BAKED_MODEL != null) {
            // TODO Continuity support
//            try {
//                model = (BakedModel) UNWRAP_BAKED_MODEL.invokeExact((BakedModel) model);
//            } catch (Throwable throwable) {
//                throw new RuntimeException("Failed to unwrap machine model", throwable);
//            }
        }

        if (model instanceof MachineBlockStateModel mbm) {
            return mbm;
        } else {
            MI.LOGGER.warn("Model {} should have been a MachineBlockStateModel, but was {}", state, model.getClass());
            return null;
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}

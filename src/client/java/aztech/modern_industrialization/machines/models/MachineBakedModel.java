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
package aztech.modern_industrialization.machines.models;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MachineBakedModel implements BakedModel, FabricBakedModel {
    private static final Direction[] DIRECTIONS = Direction.values();

    public final RenderMaterial cutoutMaterial;
    private final MachineCasing baseCasing;
    private final TextureAtlasSprite[] defaultOverlays;
    private final Map<String, TextureAtlasSprite[]> tieredOverlays;

    MachineBakedModel(RenderMaterial cutoutMaterial, MachineCasing baseCasing,
            TextureAtlasSprite[] defaultOverlays,
            Map<String, TextureAtlasSprite[]> tieredOverlays) {
        this.cutoutMaterial = cutoutMaterial;
        this.baseCasing = baseCasing;
        this.defaultOverlays = defaultOverlays;
        this.tieredOverlays = tieredOverlays;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<RandomSource> supplier,
            RenderContext renderContext) {
        if (blockRenderView instanceof RenderAttachedBlockView bv) {
            Object attachment = bv.getBlockEntityRenderAttachment(blockPos);
            if (attachment instanceof MachineModelClientData clientData) {
                MachineCasing casing = clientData.casing == null ? baseCasing : clientData.casing;
                var sprites = renderBase(renderContext, casing, clientData.frontDirection);
                if (clientData.outputDirection != null) {
                    emitSprite(renderContext.getEmitter(), clientData.outputDirection, sprites[24], 3e-4f);
                    if (clientData.itemAutoExtract) {
                        emitSprite(renderContext.getEmitter(), clientData.outputDirection, sprites[25], 3e-4f);
                    }
                    if (clientData.fluidAutoExtract) {
                        emitSprite(renderContext.getEmitter(), clientData.outputDirection, sprites[26], 3e-4f);
                    }
                }
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<RandomSource> supplier, RenderContext renderContext) {
        renderBase(renderContext, baseCasing, Direction.NORTH);
    }

    private TextureAtlasSprite[] renderBase(RenderContext renderContext, MachineCasing casing, Direction facingDirection) {
        // Casing
        MachineCasingModel.get(casing).getMesh().outputTo(renderContext.getEmitter());
        // Machine overlays
        var sprites = getSprites(casing);
        QuadEmitter emitter = renderContext.getEmitter();
        for (Direction d : DIRECTIONS) {
            TextureAtlasSprite sprite = getSprite(sprites, d, facingDirection, false);
            if (sprite != null) {
                emitSprite(emitter, d, sprite, 1e-6f);
            }
        }
        return sprites;
    }

    public TextureAtlasSprite[] getSprites(@Nullable MachineCasing casing) {
        if (casing == null) {
            return defaultOverlays;
        }
        return tieredOverlays.getOrDefault(casing.name, defaultOverlays);
    }

    /**
     * Returns null if nothing should be rendered.
     */
    @Nullable
    public static TextureAtlasSprite getSprite(TextureAtlasSprite[] sprites, Direction side, Direction facingDirection, boolean isActive) {
        int spriteId;
        if (side.getAxis().isHorizontal()) {
            spriteId = (facingDirection.get2DDataValue() - side.get2DDataValue() + 4) % 4 * 2;
        } else {
            spriteId = (facingDirection.get2DDataValue() + 4) * 2;

            if (side == Direction.DOWN) {
                spriteId += 8;
            }
        }
        if (isActive) {
            spriteId++;
        }
        return sprites[spriteId];
    }

    private void emitSprite(QuadEmitter emitter, Direction d, TextureAtlasSprite sprite, float depth) {
        if (sprite != null) {
            emitter.material(cutoutMaterial);
            emitter.square(d, 0, 0, 1, 1, -depth);
            emitter.cullFace(d);
            emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.color(-1, -1, -1, -1);
            emitter.emit();
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return MachineCasingModel.get(baseCasing).getSideSprite();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}

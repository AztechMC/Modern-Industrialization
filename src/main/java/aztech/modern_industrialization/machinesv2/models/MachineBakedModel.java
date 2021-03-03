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
package aztech.modern_industrialization.machinesv2.models;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

class MachineBakedModel implements BakedModel, FabricBakedModel {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final ModelTransformation blockTransformation;
    private final RenderMaterial cutoutMaterial;
    /**
     * @see MachineUnbakedModel
     */
    private final Sprite[] sprites;
    private final MachineCasingModel defaultCasing;

    MachineBakedModel(ModelTransformation blockTransformation, RenderMaterial cutoutMaterial, Sprite[] sprites, MachineCasingModel defaultCasing) {
        this.blockTransformation = blockTransformation;
        this.cutoutMaterial = cutoutMaterial;
        this.sprites = sprites;
        this.defaultCasing = defaultCasing;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier,
            RenderContext renderContext) {
        if (blockRenderView instanceof RenderAttachedBlockView) {
            RenderAttachedBlockView bv = (RenderAttachedBlockView) blockRenderView;
            Object attachment = bv.getBlockEntityRenderAttachment(blockPos);
            if (attachment instanceof MachineModelClientData) {
                MachineModelClientData clientData = (MachineModelClientData) attachment;
                MachineCasingModel casing = clientData.casing == null ? defaultCasing : clientData.casing.mcm;
                renderBase(renderContext, casing, clientData.frontDirection, clientData.isActive);
                if (clientData.outputDirection != null) {
                    emitSprite(renderContext.getEmitter(), clientData.outputDirection, sprites[6], 2e-6f);
                    if (clientData.itemAutoExtract) {
                        emitSprite(renderContext.getEmitter(), clientData.outputDirection, sprites[7], 2e-6f);
                    }
                    if (clientData.fluidAutoExtract) {
                        emitSprite(renderContext.getEmitter(), clientData.outputDirection, sprites[8], 2e-6f);
                    }
                }
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {
        renderBase(renderContext, defaultCasing, Direction.NORTH, false);
    }

    private void renderBase(RenderContext renderContext, MachineCasingModel casing, Direction facingDirection, boolean isActive) {
        // Casing
        renderContext.meshConsumer().accept(casing.getMesh());
        // Machine overlays
        QuadEmitter emitter = renderContext.getEmitter();
        for (Direction d : DIRECTIONS) {
            int spriteId = -2;
            if (d == facingDirection) {
                spriteId = 0;
            } else if (d == Direction.UP) {
                spriteId = 2;
            } else if (d.getAxis().isHorizontal()) {
                spriteId = 4;
            }
            if (isActive) {
                spriteId++;
            }
            if (spriteId >= 0) {
                emitSprite(emitter, d, sprites[spriteId], 1e-6f);
            }
        }
    }

    private void emitSprite(QuadEmitter emitter, Direction d, @Nullable Sprite sprite, float depth) {
        if (sprite != null) {
            emitter.material(cutoutMaterial);
            emitter.square(d, 0, 0, 1, 1, -depth);
            emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return null;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return defaultCasing.getSideSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return blockTransformation;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}

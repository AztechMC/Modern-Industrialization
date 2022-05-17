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
package aztech.modern_industrialization.blocks.storage.tank;

import aztech.modern_industrialization.blocks.creativetank.CreativeTankItem;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Supplements the existing tank model for tank items to also render the tank's fill level.
 */
public class TankItemBakedModel implements FabricBakedModel, BakedModel {
    private final BakedModel blockModel;
    private final RenderMaterial translucentMaterial;

    public TankItemBakedModel(BakedModel blockModel, RenderMaterial translucentMaterial) {
        this.blockModel = blockModel;
        this.translucentMaterial = translucentMaterial;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.fallbackConsumer().accept(blockModel);

        Item it = stack.getItem();
        if (it instanceof TankItem item) {
            if (!item.isEmpty(stack)) {
                float fillFraction = (float) item.getAmount(stack) / item.capacity;
                drawFluid(context.getEmitter(), fillFraction, item.getFluid(stack));
            }
        } else if (it instanceof CreativeTankItem) {
            if (!CreativeTankItem.isEmpty(stack)) {
                drawFluid(context.getEmitter(), 1, CreativeTankItem.getFluid(stack));
            }
        }
    }

    private void drawFluid(QuadEmitter emitter, float fillFraction, FluidVariant fluid) {
        TextureAtlasSprite stillSprite = FluidVariantRendering.getSprite(fluid);
        int color = FluidVariantRendering.getColor(fluid) | 255 << 24;
        for (Direction direction : Direction.values()) {
            float topSpace, depth, bottomSpace;
            if (FluidVariantAttributes.isLighterThanAir(fluid)) {
                bottomSpace = direction.getAxis().isHorizontal() ? 1 - fillFraction + 0.01f : 0;
                depth = direction == Direction.DOWN ? fillFraction : 0;
                topSpace = 0;
            } else {
                bottomSpace = 0;
                topSpace = direction.getAxis().isHorizontal() ? 1 - fillFraction + 0.01f : 0;
                depth = direction == Direction.UP ? 1 - fillFraction : 0;
            }
            emitter.material(translucentMaterial);
            emitter.square(direction, 0, bottomSpace, 1, 1 - topSpace, depth + 0.01f);
            emitter.spriteBake(0, stillSprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, color, color, color, color);
            emitter.emit();
        }
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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
        return blockModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return blockModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}

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
package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The models of a pipe block. It can handle up to three different pipe types.
 * The block is divided in five slots of width SIDE, three for the main pipes
 * and two for connection handling.
 */
public class PipeModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final ResourceLocation DEFAULT_BLOCK_MODEL = new ResourceLocation("minecraft:block/block");
    private static final Material PARTICLE_SPRITE = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation("minecraft:block/iron_block"));
    private TextureAtlasSprite particleSprite;
    private Map<PipeRenderer.Factory, PipeRenderer> renderers = new Reference2ObjectOpenHashMap<>();
    private ItemTransforms modelTransformation;
    private RenderMaterial cutoutMaterial;

    public PipeModel(TextureAtlasSprite particleSprite, Map<PipeRenderer.Factory, PipeRenderer> renderers, ItemTransforms modelTransformation,
            RenderMaterial cutoutMaterial) {
        this.particleSprite = particleSprite;
        this.renderers = renderers;
        this.modelTransformation = modelTransformation;
        this.cutoutMaterial = cutoutMaterial;
    }

    public PipeModel() {
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockRenderView, BlockState state, BlockPos pos, Supplier<Random> supplier,
            RenderContext renderContext) {
        renderContext.pushTransform(quad -> {
            if (quad.tag() == 0) {
                quad.material(cutoutMaterial);
            }
            return true;
        });

        var maybeAttachment = ((RenderAttachedBlockView) blockRenderView).getBlockEntityRenderAttachment(pos);
        if (maybeAttachment instanceof PipeBlockEntity.RenderAttachment attachment) {
            int centerSlots = attachment.types.length;
            for (int slot = 0; slot < centerSlots; slot++) {
                // Set color
                int color = attachment.types[slot].getColor();
                renderContext.pushTransform(getColorTransform(color));

                renderers.get(attachment.types[slot].getRenderer()).draw(blockRenderView, pos, renderContext, slot, attachment.renderedConnections,
                        attachment.customData[slot]);

                renderContext.popTransform();
            }
        }
        renderContext.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {
        Item item = itemStack.getItem();
        if (item instanceof PipeItem) {
            // TODO: remove allocation if it becomes an issue
            PipeNetworkType type = ((PipeItem) item).type;
            int color = type.getColor();
            renderContext.pushTransform(getColorTransform(color));
            renderContext.pushTransform(ITEM_TRANSFORM);

            PipeEndpointType[][] connections = new PipeEndpointType[][] {
                    { null, null, null, null, PipeEndpointType.BLOCK, PipeEndpointType.BLOCK } };
            renderers.get(type.getRenderer()).draw(null, null, renderContext, 0, connections, new CompoundTag());

            renderContext.popTransform();
            renderContext.popTransform();
        }
    }

    private static RenderContext.QuadTransform getColorTransform(int color) {
        return quad -> {
            if (quad.tag() == 0) {
                quad.spriteColor(0, color, color, color, color);
            }
            return true;
        };
    }

    private static final RenderContext.QuadTransform ITEM_TRANSFORM = quad -> {
        for (int i = 0; i < 4; ++i) {
            Vector3f pos = quad.copyPos(i, null);
            quad.pos(i, pos.x(), pos.y() * 2 - 0.5f, pos.z() * 2 - 0.5f);
        }
        return true;
    };

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
        return particleSprite;
    }

    @Override
    public ItemTransforms getTransforms() {
        return modelTransformation;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Arrays.asList(DEFAULT_BLOCK_MODEL);
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        return PipeNetworkType.getTypes().values().stream().flatMap(r -> r.getRenderer().getSpriteDependencies().stream())
                .collect(Collectors.toList());
    }

    @Override
    public BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer,
            ResourceLocation modelId) {
        particleSprite = textureGetter.apply(PARTICLE_SPRITE);
        modelTransformation = ((BlockModel) loader.getModel(DEFAULT_BLOCK_MODEL)).getTransforms();

        renderers.clear();
        for (PipeRenderer.Factory rendererFactory : PipeNetworkType.getRenderers()) {
            renderers.put(rendererFactory, rendererFactory.create(textureGetter));
        }

        cutoutMaterial = RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(0, BlendMode.CUTOUT).find();

        return this;
    }

    public RenderMaterial getCutoutMaterial() {
        return cutoutMaterial;
    }

    public Map<PipeRenderer.Factory, PipeRenderer> getRenderers() {
        return renderers;
    }
}

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

import aztech.modern_industrialization.pipes.MIPipesClient;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
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
    private static final Material PARTICLE_SPRITE = new Material(InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("minecraft:block/iron_block"));
    private TextureAtlasSprite particleSprite;
    private final Map<PipeRenderer.Factory, PipeRenderer> renderers = new Reference2ObjectOpenHashMap<>();
    private ItemTransforms modelTransformation;

    private RenderMaterial translucentMaterial;

    public PipeModel() {
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockRenderView, BlockState state, BlockPos pos, Supplier<RandomSource> supplier,
            RenderContext renderContext) {
        var maybeAttachment = ((RenderAttachedBlockView) blockRenderView).getBlockEntityRenderAttachment(pos);
        if (maybeAttachment instanceof PipeBlockEntity.RenderAttachment attachment) {
            var camouflage = attachment.camouflage();

            if (camouflage == null || MIPipesClient.transparentCamouflage) {
                int centerSlots = attachment.types().length;
                for (int slot = 0; slot < centerSlots; slot++) {
                    // Set color
                    int color = attachment.types()[slot].getColor();
                    renderContext.pushTransform(getColorTransform(color));

                    renderers.get(PipeRenderer.get(attachment.types()[slot])).draw(blockRenderView, pos, renderContext, slot,
                            attachment.renderedConnections(), attachment.customData()[slot]);

                    renderContext.popTransform();
                }
            }

            if (camouflage != null) {
                renderContext.pushTransform(quad -> {
                    // Fix tinting
                    if (quad.colorIndex() != -1) {
                        var blockColorMap = Minecraft.getInstance().getBlockColors();

                        int color = 0xFF000000 | blockColorMap.getColor(camouflage, blockRenderView, pos, quad.colorIndex());
                        quad.colorIndex(-1);

                        for (int vertex = 0; vertex < 4; ++vertex) {
                            quad.spriteColor(vertex, 0, multiplyColor(color, quad.spriteColor(vertex, 0)));
                        }
                    }

                    if (MIPipesClient.transparentCamouflage) {
                        quad.material(translucentMaterial);

                        for (int vertex = 0; vertex < 4; ++vertex) {
                            int c = quad.spriteColor(vertex, 0);
                            int newAlpha = ((c >> 24) & 0xFF) * 3 / 4;
                            quad.spriteColor(vertex, 0, (c & 0x00FFFFFF) | (newAlpha << 24));
                        }
                    }

                    return true;
                });

                var camouflageModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(camouflage);
                ((FabricBakedModel) camouflageModel).emitBlockQuads(blockRenderView, state, pos, supplier, renderContext);

                renderContext.popTransform();
            }
        }
    }

    // Thank you Indigo!
    private static int multiplyColor(int color1, int color2) {
        if (color1 == -1) {
            return color2;
        } else if (color2 == -1) {
            return color1;
        }

        final int alpha = ((color1 >> 24) & 0xFF) * ((color2 >> 24) & 0xFF) / 0xFF;
        final int red = ((color1 >> 16) & 0xFF) * ((color2 >> 16) & 0xFF) / 0xFF;
        final int green = ((color1 >> 8) & 0xFF) * ((color2 >> 8) & 0xFF) / 0xFF;
        final int blue = (color1 & 0xFF) * (color2 & 0xFF) / 0xFF;

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<RandomSource> supplier, RenderContext renderContext) {
        Item item = itemStack.getItem();
        if (item instanceof PipeItem) {
            // TODO: remove allocation if it becomes an issue
            PipeNetworkType type = ((PipeItem) item).type;
            int color = type.getColor();
            renderContext.pushTransform(getColorTransform(color));
            renderContext.pushTransform(ITEM_TRANSFORM);

            PipeEndpointType[][] connections = new PipeEndpointType[][] {
                    { null, null, null, null, PipeEndpointType.BLOCK, PipeEndpointType.BLOCK } };
            renderers.get(PipeRenderer.get(type)).draw(null, null, renderContext, 0, connections, new CompoundTag());

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
        // Scale pipe to make items look better
        for (int i = 0; i < 4; ++i) {
            Vector3f pos = quad.copyPos(i, null);
            quad.pos(i, pos.x(), pos.y() * 2 - 0.5f, pos.z() * 2 - 0.5f);
        }
        // Remove fluid quads
        if (quad.tag() == 1) {
            return false;
        }
        return true;
    };

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, RandomSource random) {
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
        return PipeNetworkType.getTypes().values().stream().flatMap(r -> PipeRenderer.get(r).getSpriteDependencies().stream())
                .collect(Collectors.toList());
    }

    @Override
    public BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer,
            ResourceLocation modelId) {
        particleSprite = textureGetter.apply(PARTICLE_SPRITE);
        modelTransformation = ((BlockModel) loader.getModel(DEFAULT_BLOCK_MODEL)).getTransforms();

        renderers.clear();
        for (PipeRenderer.Factory rendererFactory : MIPipesClient.RENDERERS) {
            renderers.put(rendererFactory, rendererFactory.create(textureGetter));
        }

        translucentMaterial = RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(0, BlendMode.TRANSLUCENT).find();

        return this;
    }
}

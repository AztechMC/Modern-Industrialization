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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankItem;
import com.mojang.datafixers.util.Pair;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class TankModel implements UnbakedModel, FabricBakedModel, BakedModel {
    private static final ResourceLocation BASE_BLOCK_MODEL = new ResourceLocation("minecraft:block/block");
    ItemTransforms transformation;
    private final Material tankSpriteId;
    private TextureAtlasSprite tankSprite;
    private RenderMaterial translucentMaterial;
    private Mesh tankMesh;

    public TankModel(String tankType) {
        tankSpriteId = new Material(InventoryMenu.BLOCK_ATLAS, new MIIdentifier("blocks/tanks/" + tankType));
    }

    public TankModel(ItemTransforms transformation, Material tankSpriteId, TextureAtlasSprite tankSprite, RenderMaterial translucentMaterial,
            Mesh tankMesh) {
        this.transformation = transformation;
        this.tankSpriteId = tankSpriteId;
        this.tankSprite = tankSprite;
        this.translucentMaterial = translucentMaterial;
        this.tankMesh = tankMesh;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        // Base mesh
        context.meshConsumer().accept(tankMesh);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.meshConsumer().accept(tankMesh);

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
        return tankSprite;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transformation;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Arrays.asList(BASE_BLOCK_MODEL);
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        return Arrays.asList(tankSpriteId);
    }

    @Override
    public BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer,
            ResourceLocation modelId) {
        transformation = ((BlockModel) loader.getModel(BASE_BLOCK_MODEL)).getTransforms();
        tankSprite = textureGetter.apply(tankSpriteId);

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        RenderMaterial cutoutMaterial = renderer.materialFinder().blendMode(0, BlendMode.CUTOUT_MIPPED).find();
        translucentMaterial = renderer.materialFinder().blendMode(0, BlendMode.TRANSLUCENT).emissive(0, true).find();
        MeshBuilder builder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        QuadEmitter emitter = builder.getEmitter();
        for (Direction direction : Direction.values()) {
            emitter.material(cutoutMaterial);
            emitter.square(direction, 0, 0, 1, 1, 0.0f);
            emitter.cullFace(direction);
            emitter.spriteBake(0, tankSprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }
        tankMesh = builder.build();
        return this;
    }

    public Mesh getTankMesh() {
        return tankMesh;
    }
}

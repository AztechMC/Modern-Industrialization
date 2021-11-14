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
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public class TankModel implements UnbakedModel, FabricBakedModel, BakedModel {
    private static final Identifier BASE_BLOCK_MODEL = new Identifier("minecraft:block/block");
    ModelTransformation transformation;
    private final SpriteIdentifier tankSpriteId;
    private Sprite tankSprite;
    private RenderMaterial translucentMaterial;
    private Mesh tankMesh;

    public TankModel(String tankType) {
        tankSpriteId = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new MIIdentifier("blocks/tanks/" + tankType));
    }

    public TankModel(ModelTransformation transformation, SpriteIdentifier tankSpriteId, Sprite tankSprite, RenderMaterial translucentMaterial,
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
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
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
        Sprite stillSprite = FluidVariantRendering.getSprite(fluid);
        int color = FluidVariantRendering.getColor(fluid) | 255 << 24;
        for (Direction direction : Direction.values()) {
            float topSpace, depth, bottomSpace;
            if (FluidVariantRendering.fillsFromTop(fluid)) {
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
        return null;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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
    public Sprite getParticleSprite() {
        return tankSprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return transformation;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Arrays.asList(BASE_BLOCK_MODEL);
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        return Arrays.asList(tankSpriteId);
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer,
            Identifier modelId) {
        transformation = ((JsonUnbakedModel) loader.getOrLoadModel(BASE_BLOCK_MODEL)).getTransformations();
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

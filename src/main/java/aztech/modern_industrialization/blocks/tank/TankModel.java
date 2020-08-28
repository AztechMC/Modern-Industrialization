package aztech.modern_industrialization.blocks.tank;

import aztech.modern_industrialization.MIIdentifier;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
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
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class TankModel implements UnbakedModel, FabricBakedModel, BakedModel {
    private static final Identifier BASE_BLOCK_MODEL = new Identifier("minecraft:block/block");
    ModelTransformation transformation;
    private final SpriteIdentifier tankSpriteId;
    private Sprite tankSprite;
    private RenderMaterial cutoutMaterial;
    private RenderMaterial translucentMaterial;
    private Mesh tankMesh;

    public TankModel(String tankType) {
        tankSpriteId = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new MIIdentifier("blocks/tanks/" + tankType));
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        // Base mesh
        context.meshConsumer().accept(tankMesh);
        // Fluid inside
        RenderAttachedBlockView view = (RenderAttachedBlockView) blockView;
        TankBlockEntity.RenderAttachment attachment = (TankBlockEntity.RenderAttachment) view.getBlockEntityRenderAttachment(pos);
        drawFluid(context.getEmitter(), attachment.fillFraction, attachment.fluid);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.meshConsumer().accept(tankMesh);

        TankItem item = (TankItem) stack.getItem();
        if(!item.isEmpty(stack)) {
            float fillFraction = (float) item.getAmount(stack) / item.capacity;
            drawFluid(context.getEmitter(), fillFraction, item.getFluid(stack));
        }
    }

    private void drawFluid(QuadEmitter emitter, float fillFraction, Fluid fluid) {
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if(handler != null) {
            Sprite stillSprite = handler.getFluidSprites(null, null, null)[0];
            int color = 255 << 24 | handler.getFluidColor(null, null, null);
            for(Direction direction : Direction.values()) {
                float topSpace = direction.getAxis().isHorizontal() ? 1 - fillFraction : 0;
                float depth = direction == Direction.UP ? 1 - fillFraction : 0;
                emitter.material(translucentMaterial);
                emitter.square(direction, 0, 0, 1, 1 - topSpace, depth);
                emitter.spriteBake(0, stillSprite, MutableQuadView.BAKE_LOCK_UV);
                emitter.spriteColor(0, color, color, color, color);
                emitter.emit();
            }
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
    public Sprite getSprite() {
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
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Arrays.asList(tankSpriteId);
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        transformation = ((JsonUnbakedModel) loader.getOrLoadModel(BASE_BLOCK_MODEL)).getTransformations();
        tankSprite = textureGetter.apply(tankSpriteId);

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        cutoutMaterial = renderer.materialFinder().blendMode(0, BlendMode.CUTOUT_MIPPED).find();
        translucentMaterial = renderer.materialFinder().blendMode(0, BlendMode.TRANSLUCENT).find();
        MeshBuilder builder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        QuadEmitter emitter = builder.getEmitter();
        for(Direction direction : Direction.values()) {
            emitter.material(cutoutMaterial);
            emitter.square(direction, 0, 0, 1, 1, -0.0001f);
            emitter.cullFace(direction);
            emitter.spriteBake(0, tankSprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }
        tankMesh = builder.build();
        return this;
    }
}

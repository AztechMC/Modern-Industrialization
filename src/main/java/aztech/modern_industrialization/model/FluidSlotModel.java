package aztech.modern_industrialization.model;

import aztech.modern_industrialization.fluid.FluidStackItem;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class FluidSlotModel implements UnbakedModel, BakedModel, FabricBakedModel {

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {}

    @Override
    public void emitItemQuads(ItemStack fluidStack, Supplier<Random> supplier, RenderContext renderContext) {
        Fluid fluid = FluidStackItem.getFluid(fluidStack);
        if(fluid != Fluids.EMPTY) {
            FluidRenderHandler renderHandler = FluidRenderHandlerRegistryImpl.INSTANCE.get(fluid);
            if(renderHandler != null) {
                Sprite stillSprite = renderHandler.getFluidSprites(null, null, null)[0];
                int color = 255 << 24 | renderHandler.getFluidColor(null, null, null);
                QuadEmitter emitter = renderContext.getEmitter();
                for(Direction direction : Direction.values()) {
                    emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
                    emitter.spriteBake(0, stillSprite, MutableQuadView.BAKE_LOCK_UV);
                    emitter.spriteColor(0, color, color, color, color);
                    emitter.emit();
                }
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
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return null;
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelTransformation.NONE;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Collections.emptyList();
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        return this;
    }
}

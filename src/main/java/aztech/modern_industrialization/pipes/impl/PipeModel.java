package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.api.PipeBlockEntity;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import static net.minecraft.util.math.Direction.*;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The model of a pipe block. It can handle up to three different pipe types.
 * The block is divided in five slots of width SIDE, three for the main pipes and two for connection handling.
 */
public class PipeModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final SpriteIdentifier FLUID_SPRITE_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new MIIdentifier("blocks/pipes/fluid"));
    private Sprite fluidSprite;
    private Mesh[][][] meshCache;

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState state, BlockPos pos, Supplier<Random> supplier, RenderContext renderContext) {
        QuadEmitter emitter = renderContext.getEmitter();

        PipeBlockEntity.RenderAttachment attachment = (PipeBlockEntity.RenderAttachment)((RenderAttachedBlockView) blockRenderView).getBlockEntityRenderAttachment(pos);
        int centerSlots = attachment.types.length;
        for(int slot = 0; slot < centerSlots; slot++) {
            int color = attachment.types[slot].getColor();

            for(Direction direction : Direction.values()) {
                PipePartBuilder pmb = new PipePartBuilder(emitter, slot == 0 ? 1 : slot == 1 ? 0 : 2, direction, fluidSprite);
                if((attachment.renderedConnections[slot] & (1 << direction.getId())) == 0) {
                    pmb.noConnection();
                } else {
                    int connSlot = 0;
                    for(int i = 0; i < slot; i++) {
                        if((attachment.renderedConnections[i] & (1 << direction.getId())) != 0) {
                            connSlot++;
                        }
                    }
                    if(connSlot == slot) {
                        pmb.straightLine();
                    } else if(connSlot == 0) {
                        pmb.shortBend();
                    } else {
                        pmb.longBend();
                    }
                }
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {

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
        return fluidSprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return null;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return null;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Arrays.asList(FLUID_SPRITE_ID);
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        fluidSprite = textureGetter.apply(FLUID_SPRITE_ID);
        return this;
    }
}

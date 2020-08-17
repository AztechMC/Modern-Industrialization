package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.api.PipeBlockEntity;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.BlockRenderView;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class PipeModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final SpriteIdentifier TEMP_SPRITE_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier("minecraft:block/iron_block"));
    private Sprite tempSprite;

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState state, BlockPos pos, Supplier<Random> supplier, RenderContext renderContext) {
        PipeBlockEntity.RenderAttachment attachment = (PipeBlockEntity.RenderAttachment)((RenderAttachedBlockView) blockRenderView).getBlockEntityRenderAttachment(pos);
        if(attachment.renderedConnections.length == 0) return;
        byte connections = attachment.renderedConnections[0];

        float pipe_width = 1.5f / 16;
        float cl = 0.5f - pipe_width / 2; // center, a bit lower
        float ch = 0.5f + pipe_width / 2; // center, a bit higher

        int color = attachment.types[0].getColor();
        QuadEmitter emitter = renderContext.getEmitter();
        // center cube
        for(Direction direction : Direction.values()) {
            emitter.square(direction, cl, cl, ch, ch, cl);
            emitter.spriteBake(0, tempSprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, color, color, color, color);
            emitter.cullFace(null);
            emitter.emit();
        }

        QuadBuilder quad = (direction, left, bottom, right, top, depth) -> {
            addQuad(emitter, direction, left, bottom, right, top, depth, color);
        };

        Function<Direction, Boolean> hasConnection = (direction) -> (connections & (1 << direction.getId())) != 0;

        // side connections
        if(hasConnection.apply(NORTH)) {
            quad.build(NORTH, cl, cl, ch, ch, 0);
            quad.build(EAST, ch, cl, 1, ch, cl);
            quad.build(WEST, 0, cl, cl, ch, cl);
            quad.build(UP, cl, ch, ch, 1, cl);
            quad.build(DOWN, cl, 0, ch, cl, cl);
        }
        if(hasConnection.apply(SOUTH)) {
            quad.build(SOUTH, cl, cl, ch, ch, 0);
            quad.build(EAST, 0, cl, cl, ch, cl);
            quad.build(WEST, ch, cl, 1, ch, cl);
            quad.build(UP, cl, 0, ch, cl, cl);
            quad.build(DOWN, cl, ch, ch, 1, cl);
        }
        if(hasConnection.apply(EAST)) {
            quad.build(EAST, cl, cl, ch, ch, 0);
            quad.build(NORTH, ch, cl, 1, ch, cl);
            quad.build(SOUTH, 0, cl, cl, ch, cl);
            quad.build(UP, ch, cl, 1, ch, cl);
            quad.build(DOWN, ch, cl, 1, ch, cl);
        }
        if(hasConnection.apply(WEST)) {
            quad.build(WEST, cl, cl, ch, ch, 0);
            quad.build(NORTH, 0, cl, cl, ch, cl);
            quad.build(SOUTH, ch, cl, 1, ch, cl);
            quad.build(UP, 0, cl, ch, ch, cl);
            quad.build(DOWN, 0, cl, ch, ch, cl);
        }
        if(hasConnection.apply(DOWN)) {
            quad.build(DOWN, cl, cl, ch, ch, 0);
            quad.build(NORTH, cl, 0, ch, ch, cl);
            quad.build(EAST, cl, 0, ch, ch, cl);
            quad.build(SOUTH, cl, 0, ch, ch, cl);
            quad.build(WEST, cl, 0, ch, ch, cl);
        }
        if(hasConnection.apply(UP)) {
            quad.build(UP, cl, cl, ch, ch, 0);
            quad.build(NORTH, cl, ch, ch, 1, cl);
            quad.build(EAST, cl, ch, ch, 1, cl);
            quad.build(SOUTH, cl, ch, ch, 1, cl);
            quad.build(WEST, cl, ch, ch, 1, cl);
        }
    }

    @FunctionalInterface
    private interface QuadBuilder {
        void build(Direction direction, float left, float bottom, float right, float top, float depth);
    }

    private void addQuad(QuadEmitter emitter, Direction direction, float left, float bottom, float right, float top, float depth, int color) {
        emitter.square(direction, left, bottom, right, top, depth);
        emitter.spriteBake(0, tempSprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, color, color, color, color);
        emitter.cullFace(null);
        emitter.emit();
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
        return tempSprite;
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
        return Arrays.asList(TEMP_SPRITE_ID);
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        tempSprite = textureGetter.apply(TEMP_SPRITE_ID);
        return this;
    }
}

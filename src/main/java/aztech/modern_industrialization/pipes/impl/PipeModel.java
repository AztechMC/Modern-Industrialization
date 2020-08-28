package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.api.PipeConnectionType;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraft.util.math.Direction.*;

/**
 * The models of a pipe block. It can handle up to three different pipe types.
 * The block is divided in five slots of width SIDE, three for the main pipes and two for connection handling.
 */
public class PipeModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final List<SpriteIdentifier> SPRITE_IDS;

    static {
        List<String> names = Arrays.asList("fluid", "fluid_in", "fluid_in_out", "fluid_out", "item", "item_in", "item_in_out", "item_out");
        SPRITE_IDS = names.stream().map(n -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new MIIdentifier("blocks/pipes/" + n))).collect(Collectors.toList());
    }

    private Sprite[] sprites;
    // [connection type (fluid, fluid in, etc...)][slot][direction][render type (no connection, straight line, short bend, etc...)]
    private Mesh[][][][] meshCache;
    private Mesh itemMesh;
    private static final Identifier DEFAULT_BLOCK_MODEL = new Identifier("minecraft:block/block");
    private ModelTransformation modelTransformation;
    private boolean isBaked = false;

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState state, BlockPos pos, Supplier<Random> supplier, RenderContext renderContext) {

        PipeBlockEntity.RenderAttachment attachment = (PipeBlockEntity.RenderAttachment) ((RenderAttachedBlockView) blockRenderView).getBlockEntityRenderAttachment(pos);
        int centerSlots = attachment.types.length;
        for (int slot = 0; slot < centerSlots; slot++) {
            // Set color
            int color = attachment.types[slot].getColor();
            renderContext.pushTransform(getColorTransform(color));

            // Draw cached meshes
            for (Direction direction : Direction.values()) {
                PipeConnectionType type = attachment.renderedConnections[slot][direction.getId()];
                renderContext.meshConsumer().accept(meshCache
                        [
                        type == null ? attachment.types[slot].getDefaultConnectionType().getId() : type.getId()
                        ][
                        slot
                        ][
                        direction.getId()
                        ][
                        getRenderType(slot, direction, attachment.renderedConnections)
                        ]);
            }

            renderContext.popTransform();
        }
    }

    /**
     * Get the type of a connection.
     */
    static int getRenderType(int slot, Direction direction, PipeConnectionType[][] connections) {
        if (connections[slot][direction.getId()] == null) {
            return 0;
        } else {
            int connSlot = 0;
            for (int i = 0; i < slot; i++) {
                if (connections[i][direction.getId()] != null) {
                    connSlot++;
                }
            }
            if (slot == 1) {
                // short bend
                if (connSlot == 0) {
                    return 2;
                }
            } else if (slot == 2) {
                if (connSlot == 0) {
                    // short bend, but far if the direction is west to avoid collisions in some cases.
                    return direction == WEST ? 3 : 2;
                } else if (connSlot == 1) {
                    // long bend
                    return 4;
                }
            }
            // default to straight line
            return 1;
        }
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {
        Item item = itemStack.getItem();
        if (item instanceof PipeItem) {
            int color = ((PipeItem) item).type.getColor();
            renderContext.pushTransform(getColorTransform(color));

            renderContext.meshConsumer().accept(itemMesh);

            renderContext.popTransform();
        }
    }

    private static RenderContext.QuadTransform getColorTransform(int color) {
        return quad -> {
            quad.spriteColor(0, color, color, color, color);
            return true;
        };
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
        return sprites[0]; // TODO: fix color.
    }

    @Override
    public ModelTransformation getTransformation() {
        return modelTransformation;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Arrays.asList(DEFAULT_BLOCK_MODEL);
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return SPRITE_IDS;
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        if (isBaked) return this;
        isBaked = true;

        sprites = new Sprite[SPRITE_IDS.size()];
        for(int i = 0; i < SPRITE_IDS.size(); i++) {
            sprites[i] = textureGetter.apply(SPRITE_IDS.get(i));
        }
        modelTransformation = ((JsonUnbakedModel) loader.getOrLoadModel(DEFAULT_BLOCK_MODEL)).getTransformations();

        meshCache = new Mesh[SPRITE_IDS.size()][3][6][5];
        MeshBuilder builder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        for(int connectionType = 0; connectionType < SPRITE_IDS.size(); connectionType++) {
            for (int slot = 0; slot < 3; slot++) {
                for (Direction direction : Direction.values()) {
                    int connectionTypes = slot == 0 ? 2 : slot == 1 ? 3 : 5;
                    for (int renderType = 0; renderType < connectionTypes; renderType++) {
                        PipePartBuilder ppb = new PipePartBuilder(builder.getEmitter(), getSlotPos(slot), direction, sprites[connectionType]);
                        if (renderType == 0) ppb.noConnection();
                        else if (renderType == 1) ppb.straightLine();
                        else if (renderType == 2) ppb.shortBend();
                        else if (renderType == 3) ppb.farShortBend();
                        else ppb.longBend();
                        meshCache[connectionType][slot][direction.getId()][renderType] = builder.build();
                    }
                }
            }
        }

        QuadEmitter itemMeshEmitter = builder.getEmitter();
        for (Direction direction : Direction.values()) {
            PipePartBuilder ppb = new PipePartBuilder(itemMeshEmitter, getSlotPos(0), direction, sprites[0]);
            if (direction == NORTH || direction == SOUTH) {
                ppb.straightLineWithFace();
            } else {
                ppb.noConnection();
            }
        }
        itemMesh = builder.build();
        return this;
    }

    static int getSlotPos(int slot) {
        return slot == 0 ? 1 : slot == 1 ? 0 : 2;
    }
}

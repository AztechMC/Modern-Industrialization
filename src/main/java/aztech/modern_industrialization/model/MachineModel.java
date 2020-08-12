package aztech.modern_industrialization.model;

import aztech.modern_industrialization.blockentity.AbstractMachineBlockEntity;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A generic machine model. For now it supports having a top/side/bottom texture and also adding an overlay on the front face.
 */
public class MachineModel implements BaseModel {
    public final Identifier id;
    private SpriteIdentifier[] sprite_ids;
    private Sprite[] sprites;

    private RenderMaterial cutoutMaterial;
    private Mesh mesh;

    public MachineModel(Identifier id, Identifier topTexture, Identifier sideTexture, Identifier bottomTexture) {
        this.id = id;
        sprite_ids = new SpriteIdentifier[] {
                new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, topTexture),
                new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, sideTexture),
                new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, bottomTexture),
                null, // front overlay
        };
    }

    private static Identifier appendPath(Identifier base, String extension) {
        return new Identifier(base.getNamespace(), base.getPath() + extension);
    }

    public MachineModel(Identifier id, Identifier textureFolder) {
        this(id, appendPath(textureFolder, "top"), appendPath(textureFolder, "side"), appendPath(textureFolder, "bottom"));
    }

    public MachineModel withFrontOverlay(Identifier overlayTexture) {
        sprite_ids[3] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, overlayTexture);
        return this;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {
        // Render base mesh
        renderContext.meshConsumer().accept(mesh);
        // Render front overlay
        RenderAttachedBlockView view = (RenderAttachedBlockView) blockRenderView;
        AbstractMachineBlockEntity.AttachmentData attachmentData = (AbstractMachineBlockEntity.AttachmentData)view.getBlockEntityRenderAttachment(blockPos);
        QuadEmitter emitter = renderContext.getEmitter();
        if(this.sprites[3] != null) {
            emitter.material(cutoutMaterial);
            emitter.square(attachmentData.facingDirection, 0.0f, 0.0f, 1.0f, 1.0f, -0.000001f);
            emitter.spriteBake(0, sprites[3], MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {

    }

    @Override
    public Sprite getSprite() {
        return sprites[1];
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Arrays.asList(sprite_ids);
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        // Get sprites
        sprites = new Sprite[sprite_ids.length];
        for(int i = 0; i < 4; ++i) {
            if(sprite_ids[i] != null) {
                sprites[i] = textureGetter.apply(sprite_ids[i]);
            }
        }
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        // Get cutout material
        cutoutMaterial = renderer.materialFinder().blendMode(0, BlendMode.CUTOUT_MIPPED).find();
        // Build mesh
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();
        emitter.material(cutoutMaterial);
        for(Direction direction : Direction.values()) {
            int spriteIdx = direction == Direction.UP ? 0 : direction == Direction.DOWN ? 2 : 1 ;
            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
            emitter.spriteBake(0, sprites[spriteIdx], MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }
        mesh = builder.build();

        return this;
    }
}

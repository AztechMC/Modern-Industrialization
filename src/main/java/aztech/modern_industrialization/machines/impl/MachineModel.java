package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.model.block.CustomBlockModel;
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
import java.util.stream.Collectors;

/**
 * A generic machine_recipe models.
 */
public class MachineModel extends CustomBlockModel {
    public final String model_name;
    private SpriteIdentifier[] sprite_ids;
    private Sprite[] sprites;

    private RenderMaterial cutoutMaterial;
    private Mesh mesh;

    public MachineModel(String model_name, Identifier topTexture, Identifier sideTexture, Identifier bottomTexture) {
        this.model_name = model_name;
        sprite_ids = new SpriteIdentifier[] {
                new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, topTexture),
                new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, sideTexture),
                new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, bottomTexture),
                null, // front overlay
                null, // active front overlay
                null, // side overlay
                null, // active side overlay
                null, // top overlay
                null, // active top overlay
                null, // output overlay
                null, // item output overlay
                null, // fluid output overlay
        };
    }

    private static Identifier appendPath(Identifier base, String extension) {
        return new Identifier(base.getNamespace(), base.getPath() + extension);
    }

    public MachineModel(String model_name, Identifier textureFolder) {
        this(model_name, appendPath(textureFolder, "top"), appendPath(textureFolder, "side"), appendPath(textureFolder, "bottom"));
    }

    public MachineModel withFrontOverlay(Identifier inactiveOverlay, Identifier activeOverlay) {
        sprite_ids[3] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, inactiveOverlay);
        sprite_ids[4] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, activeOverlay);
        return this;
    }

    public MachineModel withSideOverlay(Identifier inactiveOverlay, Identifier activeOverlay) {
        sprite_ids[5] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, inactiveOverlay);
        sprite_ids[6] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, activeOverlay);
        return this;
    }

    public MachineModel withTopOverlay(Identifier inactiveOverlay, Identifier activeOverlay) {
        sprite_ids[7] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, inactiveOverlay);
        sprite_ids[8] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, activeOverlay);
        return this;
    }

    public MachineModel withOutputOverlay(Identifier overlay, Identifier extractItems, Identifier extractFluids) {
        sprite_ids[ 9] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, overlay);
        sprite_ids[10] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, extractItems);
        sprite_ids[11] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, extractFluids);
        return this;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {
        RenderAttachedBlockView view = (RenderAttachedBlockView) blockRenderView;
        AbstractMachineBlockEntity.AttachmentData attachmentData = (AbstractMachineBlockEntity.AttachmentData)view.getBlockEntityRenderAttachment(blockPos);
        // Render base mesh
        renderContext.meshConsumer().accept(attachmentData.casingOverride == null ? mesh : attachmentData.casingOverride.mesh);
        // Render overlays
        QuadEmitter emitter = renderContext.getEmitter();
        for(Direction direction : Direction.values()) {
            int spriteId = -2;
            if(direction == Direction.UP) {
                spriteId = 7;
            } else if(direction == attachmentData.facingDirection) {
                spriteId = 3;
            } else if(direction.getAxis().isHorizontal()) {
                spriteId = 5;
            }
            if(attachmentData.isActive) ++spriteId;
            if(spriteId < 0 || sprites[spriteId] == null) continue;
            Sprite sprite = this.sprites[spriteId];
            emitter.material(cutoutMaterial);
            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, -0.000001f);
            emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }
        // Render output overlay
        if(this.sprites[9] != null && attachmentData.outputDirection != null) {
            for(int i = 0; i < 3; ++i) {
                if(i == 1 && !attachmentData.extractItems) continue;
                if(i == 2 && !attachmentData.extractFluids) continue;
                emitter.material(cutoutMaterial);
                emitter.square(attachmentData.outputDirection, 0.0f, 0.0f, 1.0f, 1.0f, -0.000002f);
                emitter.spriteBake(0, sprites[9+i], MutableQuadView.BAKE_LOCK_UV);
                emitter.spriteColor(0, -1, -1, -1, -1);
                emitter.emit();
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {
        // Base mesh
        renderContext.meshConsumer().accept(mesh);
        QuadEmitter emitter = renderContext.getEmitter();
        // Front overlay, always facing NORTH
        for(Direction direction : Direction.values()) {
            int spriteId = -2;
            if(direction == Direction.UP) {
                spriteId = 7;
            } else if(direction == Direction.NORTH) {
                spriteId = 3;
            } else if(direction.getAxis().isHorizontal()) {
                spriteId = 5;
            }
            if(spriteId < 0 || sprites[spriteId] == null) continue;
            Sprite sprite = this.sprites[spriteId];
            emitter.material(cutoutMaterial);
            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, -0.000001f);
            emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }
    }

    @Override
    public Sprite getSprite() {
        return sprites[1];
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Arrays.stream(sprite_ids).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        super.bake(loader, textureGetter, rotationContainer, modelId);

        // Get sprites
        sprites = new Sprite[sprite_ids.length];
        for(int i = 0; i < sprite_ids.length; ++i) {
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

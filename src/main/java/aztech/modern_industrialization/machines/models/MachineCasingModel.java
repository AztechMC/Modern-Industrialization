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
package aztech.modern_industrialization.machines.models;

import aztech.modern_industrialization.MIIdentifier;
import com.mojang.datafixers.util.Pair;
import java.util.*;
import java.util.function.Function;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Auto-registers itself when created!
 */
public class MachineCasingModel implements UnbakedModel, BakedModel {
    private Identifier id;
    /**
     * <ol>
     * <li>Top texture</li>
     * <li>Side texture</li>
     * <li>Bottom texture</li>
     * </ol>
     */
    private final SpriteIdentifier[] spriteIds = new SpriteIdentifier[3];
    private static final String[] SIDES = new String[] { "top", "side", "bottom" };

    private Mesh mesh;
    private Sprite sideSprite;

    public MachineCasingModel(String folder) {
        this.id = new MIIdentifier("machine_casing/" + folder);
        for (int i = 0; i < 3; ++i) {
            spriteIds[i] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
                    new MIIdentifier("blocks/casings/" + folder + "/" + SIDES[i]));
        }

        MachineModelProvider.register(id, this);
        MachineModelProvider.loadManually(id);
    }

    public MachineCasingModel(Identifier id, Mesh mesh, Sprite sideSprite) {
        this.id = id;
        this.mesh = mesh;
        this.sideSprite = sideSprite;
    }

    public Identifier getId() {
        return id;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Sprite getSideSprite() {
        return sideSprite;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        return Arrays.asList(spriteIds);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public @Nullable BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer,
            Identifier modelId) {
        Sprite[] sprites = new Sprite[3];
        for (int i = 0; i < 3; ++i) {
            sprites[i] = textureGetter.apply(spriteIds[i]);
        }
        this.sideSprite = sprites[1];
        MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();
        for (Direction direction : Direction.values()) {
            int spriteIdx = direction == Direction.UP ? 0 : direction == Direction.DOWN ? 2 : 1;
            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
            emitter.spriteBake(0, sprites[spriteIdx], MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }
        this.mesh = meshBuilder.build();
        return this;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public void setSideSprite(Sprite sideSprite) {
        this.sideSprite = sideSprite;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
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
    public Sprite getParticleSprite() {
        return null;
    }

    @Override
    public ModelTransformation getTransformation() {
        return null;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return null;
    }
}

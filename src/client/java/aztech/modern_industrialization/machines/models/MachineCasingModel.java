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
import java.util.*;
import java.util.function.Function;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MachineCasingModel implements UnbakedModel, BakedModel {
    public static MachineCasingModel get(MachineCasing casing) {
        return (MachineCasingModel) casing.model;
    }

    ResourceLocation id;
    /**
     * <ol>
     * <li>Top texture</li>
     * <li>Side texture</li>
     * <li>Bottom texture</li>
     * </ol>
     */
    private final Material[] spriteIds = new Material[3];
    private static final String[] SIDES = new String[] { "top", "side", "bottom" };

    private Mesh mesh;
    private TextureAtlasSprite sideSprite;

    public MachineCasingModel(String folder) {
        this.id = new MIIdentifier("machine_casing/" + folder);
        for (int i = 0; i < 3; ++i) {
            spriteIds[i] = new Material(InventoryMenu.BLOCK_ATLAS,
                    new MIIdentifier("block/casings/" + folder + "/" + SIDES[i]));
        }
    }

    public Mesh getMesh() {
        return mesh;
    }

    public TextureAtlasSprite getSideSprite() {
        return sideSprite;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ResourceLocation location) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[3];
        for (int i = 0; i < 3; ++i) {
            sprites[i] = spriteGetter.apply(spriteIds[i]);
        }
        this.sideSprite = sprites[1];
        MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();
        for (Direction direction : Direction.values()) {
            int spriteIdx = direction == Direction.UP ? 0 : direction == Direction.DOWN ? 2 : 1;
            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
            emitter.spriteBake(sprites[spriteIdx], MutableQuadView.BAKE_LOCK_UV);
            emitter.color(-1, -1, -1, -1);
            emitter.emit();
        }
        this.mesh = meshBuilder.build();
        return this;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
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
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return null;
    }

    @Override
    public ItemTransforms getTransforms() {
        return null;
    }

    @Override
    public ItemOverrides getOverrides() {
        return null;
    }
}

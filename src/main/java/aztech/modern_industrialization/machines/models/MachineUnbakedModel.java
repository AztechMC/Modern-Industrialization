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
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

/**
 * The sprites are, in order:
 * <ol>
 * <li>Inactive front overlay</li>
 * <li>Active front overlay</li>
 * <li>Inactive top overlay</li>
 * <li>Active top overlay</li>
 * <li>Inactive side overlay</li>
 * <li>Active side overlay</li>
 * <li>Output overlay</li>
 * <li>Item auto-export overlay</li>
 * <li>Fluid auto-export overlay</li>
 * </ol>
 */
public class MachineUnbakedModel implements UnbakedModel {
    private static final Identifier BASE_BLOCK_MODEL = new Identifier("minecraft:block/block");

    private final SpriteIdentifier[] spriteIds = new SpriteIdentifier[9];
    private final MachineCasingModel defaultCasing;

    public MachineUnbakedModel(String folder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay, boolean active,
            MachineCasingModel defaultCasing) {
        if (frontOverlay) {
            setOverlay(folder, "front", 0, active);
        }
        if (topOverlay) {
            setOverlay(folder, "top", 2, active);
        }
        if (sideOverlay) {
            setOverlay(folder, "side", 4, active);
        }
        this.defaultCasing = defaultCasing;
    }

    private void setOverlay(String folder, String side, int id, boolean active) {
        spriteIds[id] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
                new MIIdentifier("blocks/machines/" + folder + "/overlay_" + side));
        if (active) {
            spriteIds[id + 1] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
                    new MIIdentifier("blocks/machines/" + folder + "/overlay_" + side + "_active"));
        } else {
            spriteIds[id + 1] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
                    new MIIdentifier("blocks/machines/" + folder + "/overlay_" + side));
        }
    }

    public MachineUnbakedModel withStandardOverlays() {
        return withOutputOverlay("output").withItemAutoExportOverlay("extract_items").withFluidAutoExportOverlay("extract_fluids");
    }

    public MachineUnbakedModel withOutputOverlay(String suffix) {
        return withOverlay(suffix, 6);
    }

    public MachineUnbakedModel withItemAutoExportOverlay(String suffix) {
        return withOverlay(suffix, 7);
    }

    public MachineUnbakedModel withFluidAutoExportOverlay(String suffix) {
        return withOverlay(suffix, 8);
    }

    private MachineUnbakedModel withOverlay(String suffix, int id) {
        spriteIds[id] = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new MIIdentifier("blocks/overlays/" + suffix));
        return this;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.singletonList(BASE_BLOCK_MODEL);
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        return Arrays.stream(spriteIds).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer,
            Identifier modelId) {
        ModelTransformation blockTransformation = ((JsonUnbakedModel) loader.getOrLoadModel(BASE_BLOCK_MODEL)).getTransformations();
        RenderMaterial cutoutMaterial = RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(0, BlendMode.CUTOUT_MIPPED).find();
        Sprite[] sprites = new Sprite[spriteIds.length];
        for (int i = 0; i < spriteIds.length; ++i) {
            if (spriteIds[i] != null) {
                sprites[i] = textureGetter.apply(spriteIds[i]);
            }
        }
        return new MachineBakedModel(blockTransformation, cutoutMaterial, sprites, defaultCasing);
    }
}

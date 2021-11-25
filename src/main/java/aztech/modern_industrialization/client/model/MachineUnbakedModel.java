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
package aztech.modern_industrialization.client.model;

import aztech.modern_industrialization.machines.models.MachineCasing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.Resource;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class MachineUnbakedModel implements UnbakedModel {
    private static final Identifier BASE_BLOCK_MODEL = new Identifier("minecraft:block/block");
    private static final int OVERLAY_COUNT = 2 * 6 + 3; // active and inactive per side + output/item export/fluid export.
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Identifier.class, new Identifier.Serializer()).create();

    public static MachineUnbakedModel deserialize(MachineCasing baseCasing, Resource jsonModel) {
        var stream = new InputStreamReader(jsonModel.getInputStream(), StandardCharsets.UTF_8);
        var element = JsonParser.parseReader(stream);
        return new MachineUnbakedModel(baseCasing, element.getAsJsonObject());
    }

    private final MachineCasing baseCasing;
    private final SpriteIdentifier[] defaultOverlays;
    private final Map<String, SpriteIdentifier[]> tieredOverlays = new HashMap<>();

    private MachineUnbakedModel(MachineCasing baseCasing, JsonObject obj) {
        this.baseCasing = baseCasing;

        var defaultOverlaysJson = OverlaysJson.parse(JsonHelper.getObject(obj, "default_overlays"), null);
        this.defaultOverlays = defaultOverlaysJson.toSpriteIds();

        var tieredOverlays = JsonHelper.getObject(obj, "tiered_overlays", new JsonObject());
        for (var casingTier : tieredOverlays.keySet()) {
            var casingOverlaysJson = OverlaysJson.parse(JsonHelper.getObject(tieredOverlays, casingTier), defaultOverlaysJson);
            this.tieredOverlays.put(casingTier, casingOverlaysJson.toSpriteIds());
        }
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(BASE_BLOCK_MODEL);
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        var set = new HashSet<>(Arrays.asList(defaultOverlays));
        for (var tierSprites : tieredOverlays.values()) {
            set.addAll(Arrays.asList(tierSprites));
        }
        set.remove(null); // remove null (can happen if one of the textures is not specified)
        return set;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer,
            Identifier modelId) {
        var blockTransformation = ((JsonUnbakedModel) loader.getOrLoadModel(BASE_BLOCK_MODEL)).getTransformations();
        var cutoutMaterial = RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(0, BlendMode.CUTOUT_MIPPED).find();

        var defaultOverlays = loadSprites(textureGetter, this.defaultOverlays);
        var tieredOverlays = new HashMap<String, Sprite[]>();
        for (var entry : this.tieredOverlays.entrySet()) {
            tieredOverlays.put(entry.getKey(), loadSprites(textureGetter, entry.getValue()));
        }
        return new MachineBakedModel(blockTransformation, cutoutMaterial, baseCasing, defaultOverlays, tieredOverlays);
    }

    private static Sprite[] loadSprites(Function<SpriteIdentifier, Sprite> textureGetter, SpriteIdentifier[] ids) {
        var sprites = new Sprite[ids.length];
        for (int i = 0; i < ids.length; ++i) {
            if (ids[i] != null) {
                sprites[i] = textureGetter.apply(ids[i]);
            }
        }
        return sprites;
    }

    private static class OverlaysJson {
        // All fields are nullable.
        private Identifier top;
        private Identifier top_active;
        private Identifier side;
        private Identifier side_active;
        private Identifier bottom;
        private Identifier bottom_active;
        private Identifier front;
        private Identifier front_active;
        private Identifier left;
        private Identifier left_active;
        private Identifier right;
        private Identifier right_active;
        private Identifier back;
        private Identifier back_active;
        private Identifier output;
        private Identifier item_auto;
        private Identifier fluid_auto;

        private static OverlaysJson parse(JsonObject json, @Nullable OverlaysJson defaultOverlay) {
            var overlays = GSON.fromJson(json, OverlaysJson.class);

            if (defaultOverlay != null) {
                // Copy null fields from the default.
                try {
                    for (var field : OverlaysJson.class.getDeclaredFields()) {
                        if (field.get(overlays) == null) {
                            field.set(overlays, field.get(defaultOverlay));
                        }
                    }
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to copy fields from default overlay", ex);
                }
            }

            return overlays;
        }

        /**
         * Order is as follows:
         * Active and inactive: top, bottom, front, left, back, right.
         * output, item auto, fluid auto
         */
        private SpriteIdentifier[] toSpriteIds() {
            return new SpriteIdentifier[] {
                    select(top),
                    select(top_active, top),
                    select(bottom),
                    select(bottom_active, bottom),
                    select(front, side),
                    select(front_active, front, side_active, side),
                    select(left, side),
                    select(left_active, left, side_active, side),
                    select(back, side),
                    select(back_active, back, side_active, side),
                    select(right, side),
                    select(right_active, right, side_active, side),
                    select(output),
                    select(item_auto),
                    select(fluid_auto),
            };
        }

        /**
         * Select first non-null id, and convert it to a sprite id.
         */
        @Nullable
        private static SpriteIdentifier select(@Nullable Identifier... candidates) {
            for (var id : candidates) {
                if (id != null) {
                    return new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, id);
                }
            }
            return null;
        }
    }
}

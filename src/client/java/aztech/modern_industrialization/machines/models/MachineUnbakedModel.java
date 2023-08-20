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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;

public class MachineUnbakedModel implements UnbakedModel {
    private static final ResourceLocation BASE_BLOCK_MODEL = new ResourceLocation("minecraft:block/block");
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    public static MachineUnbakedModel deserialize(MachineCasing baseCasing, BufferedReader jsonModelReader) {
        var element = JsonParser.parseReader(jsonModelReader);
        return new MachineUnbakedModel(baseCasing, element.getAsJsonObject());
    }

    private final MachineCasing baseCasing;
    private final Material[] defaultOverlays;
    private final Map<String, Material[]> tieredOverlays = new HashMap<>();

    private MachineUnbakedModel(MachineCasing baseCasing, JsonObject obj) {
        this.baseCasing = baseCasing;

        var defaultOverlaysJson = OverlaysJson.parse(GsonHelper.getAsJsonObject(obj, "default_overlays"), null);
        this.defaultOverlays = defaultOverlaysJson.toSpriteIds();

        var tieredOverlays = GsonHelper.getAsJsonObject(obj, "tiered_overlays", new JsonObject());
        for (var casingTier : tieredOverlays.keySet()) {
            var casingOverlaysJson = OverlaysJson.parse(GsonHelper.getAsJsonObject(tieredOverlays, casingTier), defaultOverlaysJson);
            this.tieredOverlays.put(casingTier, casingOverlaysJson.toSpriteIds());
        }
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of(BASE_BLOCK_MODEL);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ResourceLocation location) {
        var cutoutMaterial = RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(BlendMode.CUTOUT_MIPPED).find();

        var defaultOverlays = loadSprites(spriteGetter, this.defaultOverlays);
        var tieredOverlays = new HashMap<String, TextureAtlasSprite[]>();
        for (var entry : this.tieredOverlays.entrySet()) {
            tieredOverlays.put(entry.getKey(), loadSprites(spriteGetter, entry.getValue()));
        }
        return new MachineBakedModel(cutoutMaterial, baseCasing, defaultOverlays, tieredOverlays);
    }

    private static TextureAtlasSprite[] loadSprites(Function<Material, TextureAtlasSprite> textureGetter, Material[] ids) {
        var sprites = new TextureAtlasSprite[ids.length];
        for (int i = 0; i < ids.length; ++i) {
            if (ids[i] != null) {
                sprites[i] = textureGetter.apply(ids[i]);
            }
        }
        return sprites;
    }

    private static class OverlaysJson {
        // All fields are nullable.
        private ResourceLocation top;
        private ResourceLocation top_active;
        private ResourceLocation side;
        private ResourceLocation side_active;
        private ResourceLocation bottom;
        private ResourceLocation bottom_active;
        private ResourceLocation front;
        private ResourceLocation front_active;
        private ResourceLocation left;
        private ResourceLocation left_active;
        private ResourceLocation right;
        private ResourceLocation right_active;
        private ResourceLocation back;
        private ResourceLocation back_active;
        private ResourceLocation top_s;
        private ResourceLocation top_s_active;
        private ResourceLocation top_w;
        private ResourceLocation top_w_active;
        private ResourceLocation top_n;
        private ResourceLocation top_n_active;
        private ResourceLocation top_e;
        private ResourceLocation top_e_active;
        private ResourceLocation bottom_s;
        private ResourceLocation bottom_s_active;
        private ResourceLocation bottom_w;
        private ResourceLocation bottom_w_active;
        private ResourceLocation bottom_n;
        private ResourceLocation bottom_n_active;
        private ResourceLocation bottom_e;
        private ResourceLocation bottom_e_active;
        private ResourceLocation output;
        private ResourceLocation item_auto;
        private ResourceLocation fluid_auto;

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
         * Active and inactive: front, left, back, right, top S/W/N/E, bottom S/W/N/E,
         * output, item auto, fluid auto
         */
        private Material[] toSpriteIds() {
            return new Material[] {
                    select(front, side),
                    select(front_active, front, side_active, side),
                    select(left, side),
                    select(left_active, left, side_active, side),
                    select(back, side),
                    select(back_active, back, side_active, side),
                    select(right, side),
                    select(right_active, right, side_active, side),
                    select(top_s, top),
                    select(top_s_active, top_s, top_active, top),
                    select(top_w, top),
                    select(top_w_active, top_w, top_active, top),
                    select(top_n, top),
                    select(top_n_active, top_n, top_active, top),
                    select(top_e, top),
                    select(top_e_active, top_e, top_active, top),
                    select(bottom_s, bottom),
                    select(bottom_s_active, bottom_s, bottom_active, bottom),
                    select(bottom_w, bottom),
                    select(bottom_w_active, bottom_w, bottom_active, bottom),
                    select(bottom_n, bottom),
                    select(bottom_n_active, bottom_n, bottom_active, bottom),
                    select(bottom_e, bottom),
                    select(bottom_e_active, bottom_e, bottom_active, bottom),
                    select(output),
                    select(item_auto),
                    select(fluid_auto),
            };
        }

        /**
         * Select first non-null id, and convert it to a sprite id.
         */
        @Nullable
        private static Material select(@Nullable ResourceLocation... candidates) {
            for (var id : candidates) {
                if (id != null) {
                    return new Material(InventoryMenu.BLOCK_ATLAS, id);
                }
            }
            return null;
        }
    }
}

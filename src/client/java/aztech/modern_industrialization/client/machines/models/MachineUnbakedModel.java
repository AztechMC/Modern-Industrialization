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

package aztech.modern_industrialization.client.machines.models;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jspecify.annotations.Nullable;

public class MachineUnbakedModel implements IUnbakedGeometry<MachineUnbakedModel> {
    public static final ResourceLocation LOADER_ID = MI.id("machine");
    public static final IGeometryLoader<MachineUnbakedModel> LOADER = (jsonObject, deserializationContext) -> {
        return new MachineUnbakedModel(jsonObject);
    };

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    private final MachineCasing baseCasing;
    private final @Nullable Material[] defaultOverlays;
    private final Map<MachineCasing, @Nullable Material[]> tieredOverlays = new HashMap<>();

    private MachineUnbakedModel(JsonObject obj) {
        this.baseCasing = MachineCasings.get(GsonHelper.getAsString(obj, "casing"));

        var defaultOverlaysJson = OverlaysJson.parse(GsonHelper.getAsJsonObject(obj, "default_overlays"), null);
        this.defaultOverlays = defaultOverlaysJson.toSpriteIds();

        var tieredOverlays = GsonHelper.getAsJsonObject(obj, "tiered_overlays", new JsonObject());
        for (var casingTier : tieredOverlays.keySet()) {
            var casingOverlaysJson = OverlaysJson.parse(GsonHelper.getAsJsonObject(tieredOverlays, casingTier), defaultOverlaysJson);
            this.tieredOverlays.put(MachineCasings.get(casingTier), casingOverlaysJson.toSpriteIds());
        }
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState, ItemOverrides overrides) {
        var defaultOverlays = loadSprites(spriteGetter, this.defaultOverlays);
        var tieredOverlays = new HashMap<MachineCasing, @Nullable TextureAtlasSprite[]>();
        for (var entry : this.tieredOverlays.entrySet()) {
            tieredOverlays.put(entry.getKey(), loadSprites(spriteGetter, entry.getValue()));
        }
        return new MachineBakedModel(baseCasing, defaultOverlays, tieredOverlays);
    }

    private static @Nullable TextureAtlasSprite[] loadSprites(Function<Material, TextureAtlasSprite> textureGetter, @Nullable Material[] ids) {
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
        private @Nullable ResourceLocation top;
        private @Nullable ResourceLocation top_active;
        private @Nullable ResourceLocation side;
        private @Nullable ResourceLocation side_active;
        private @Nullable ResourceLocation bottom;
        private @Nullable ResourceLocation bottom_active;
        private @Nullable ResourceLocation front;
        private @Nullable ResourceLocation front_active;
        private @Nullable ResourceLocation left;
        private @Nullable ResourceLocation left_active;
        private @Nullable ResourceLocation right;
        private @Nullable ResourceLocation right_active;
        private @Nullable ResourceLocation back;
        private @Nullable ResourceLocation back_active;
        private @Nullable ResourceLocation top_s;
        private @Nullable ResourceLocation top_s_active;
        private @Nullable ResourceLocation top_w;
        private @Nullable ResourceLocation top_w_active;
        private @Nullable ResourceLocation top_n;
        private @Nullable ResourceLocation top_n_active;
        private @Nullable ResourceLocation top_e;
        private @Nullable ResourceLocation top_e_active;
        private @Nullable ResourceLocation bottom_s;
        private @Nullable ResourceLocation bottom_s_active;
        private @Nullable ResourceLocation bottom_w;
        private @Nullable ResourceLocation bottom_w_active;
        private @Nullable ResourceLocation bottom_n;
        private @Nullable ResourceLocation bottom_n_active;
        private @Nullable ResourceLocation bottom_e;
        private @Nullable ResourceLocation bottom_e_active;
        private @Nullable ResourceLocation output;
        private @Nullable ResourceLocation item_auto;
        private @Nullable ResourceLocation fluid_auto;

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
        private @Nullable Material[] toSpriteIds() {
            return new @Nullable Material[] {
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

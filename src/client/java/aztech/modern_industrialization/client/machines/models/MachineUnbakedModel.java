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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.Nullable;

public class MachineUnbakedModel implements CustomUnbakedBlockStateModel {
    public static final Identifier LOADER_ID = MI.id("machine");

    // TODO: consider using a proper codec
    public static final MapCodec<MachineUnbakedModel> CODEC = MapCodec.assumeMapUnsafe(
            ExtraCodecs.converter(JsonOps.INSTANCE)
                    .flatXmap(json -> {
                        if (json instanceof JsonObject object) {
                            return DataResult.success(new MachineUnbakedModel(object));
                        } else {
                            return DataResult.error(() -> "Can only read json objects, received " + json);
                        }
                    }, model -> DataResult.error(() -> "Cannot write machine unbaked models.")));

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new JsonDeserializer<>() {
                @Override
                public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return Identifier.parse(GsonHelper.convertToString(json, "location"));
                }
            })
            .create();

    private final MachineCasing baseCasing;
    private final @Nullable Material[] defaultOverlays;
    private final Map<MachineCasing, @Nullable Material[]> tieredOverlays = new HashMap<>();
    private final boolean noOverlayOnOutputSide;

    private MachineUnbakedModel(JsonObject obj) {
        this.baseCasing = MachineCasings.get(GsonHelper.getAsString(obj, "casing"));

        var defaultOverlaysJson = OverlaysJson.parse(GsonHelper.getAsJsonObject(obj, "default_overlays"), null);
        this.defaultOverlays = defaultOverlaysJson.toSpriteIds();

        var tieredOverlays = GsonHelper.getAsJsonObject(obj, "tiered_overlays", new JsonObject());
        for (var casingTier : tieredOverlays.keySet()) {
            var casingOverlaysJson = OverlaysJson.parse(GsonHelper.getAsJsonObject(tieredOverlays, casingTier), defaultOverlaysJson);
            this.tieredOverlays.put(MachineCasings.get(casingTier), casingOverlaysJson.toSpriteIds());
        }

        this.noOverlayOnOutputSide = GsonHelper.getAsBoolean(obj, "no_overlay_on_output_side", false);
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public BlockStateModel bake(ModelBaker modelBakery) {
        var defaultOverlays = loadSprites(modelBakery.sprites(), this.defaultOverlays);
        var tieredOverlays = new HashMap<MachineCasing, @Nullable TextureAtlasSprite[]>();
        for (var entry : this.tieredOverlays.entrySet()) {
            tieredOverlays.put(entry.getKey(), loadSprites(modelBakery.sprites(), entry.getValue()));
        }
        return new MachineBlockStateModel(baseCasing, defaultOverlays, tieredOverlays, noOverlayOnOutputSide);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {}

    private static @Nullable TextureAtlasSprite[] loadSprites(SpriteGetter spriteGetter, @Nullable Material[] ids) {
        var sprites = new TextureAtlasSprite[ids.length];
        for (int i = 0; i < ids.length; ++i) {
            if (ids[i] != null) {
                sprites[i] = spriteGetter.get(ids[i], () -> "machine unbaked model");
            }
        }
        return sprites;
    }

    private static class OverlaysJson {
        private @Nullable Identifier top;
        private @Nullable Identifier top_active;
        private @Nullable Identifier side;
        private @Nullable Identifier side_active;
        private @Nullable Identifier bottom;
        private @Nullable Identifier bottom_active;
        private @Nullable Identifier front;
        private @Nullable Identifier front_active;
        private @Nullable Identifier left;
        private @Nullable Identifier left_active;
        private @Nullable Identifier right;
        private @Nullable Identifier right_active;
        private @Nullable Identifier back;
        private @Nullable Identifier back_active;
        private @Nullable Identifier top_s;
        private @Nullable Identifier top_s_active;
        private @Nullable Identifier top_w;
        private @Nullable Identifier top_w_active;
        private @Nullable Identifier top_n;
        private @Nullable Identifier top_n_active;
        private @Nullable Identifier top_e;
        private @Nullable Identifier top_e_active;
        private @Nullable Identifier bottom_s;
        private @Nullable Identifier bottom_s_active;
        private @Nullable Identifier bottom_w;
        private @Nullable Identifier bottom_w_active;
        private @Nullable Identifier bottom_n;
        private @Nullable Identifier bottom_n_active;
        private @Nullable Identifier bottom_e;
        private @Nullable Identifier bottom_e_active;
        private @Nullable Identifier output;
        private @Nullable Identifier item_auto;
        private @Nullable Identifier fluid_auto;

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
        private static Material select(@Nullable Identifier... candidates) {
            for (var id : candidates) {
                if (id != null) {
                    return new Material(AtlasIds.BLOCKS, id);
                }
            }
            return null;
        }
    }
}

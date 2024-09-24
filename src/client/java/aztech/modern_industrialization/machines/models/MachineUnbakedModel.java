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

import aztech.modern_industrialization.MI;
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
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public class MachineUnbakedModel<O extends MachineOverlaysJson> implements IUnbakedGeometry<MachineUnbakedModel<O>> {
    public static final ResourceLocation LOADER_ID = MI.id("machine");
    public static final IGeometryLoader<MachineUnbakedModel<MachineOverlaysJson>> LOADER = (jsonObject, deserializationContext) -> {
        return new MachineUnbakedModel(OverlaysJson.class, MachineBakedModel::new, jsonObject);
    };

    private final MachineModelBaker modelBaker;
    private final MachineCasing baseCasing;
    private final int[] outputOverlayIndexes;
    private final Material[] defaultOverlays;
    private final Map<String, Material[]> tieredOverlays = new HashMap<>();

    public MachineUnbakedModel(Class<O> overlayClass, MachineModelBaker modelBaker, JsonObject obj) {
        this.modelBaker = modelBaker;

        this.baseCasing = MachineCasings.get(GsonHelper.getAsString(obj, "casing"));

        var defaultOverlaysJson = MachineOverlaysJson.parse(overlayClass, GsonHelper.getAsJsonObject(obj, "default_overlays"), null);
        this.outputOverlayIndexes = defaultOverlaysJson.getOutputSpriteIndexes();
        this.defaultOverlays = defaultOverlaysJson.toSpriteIds();

        var tieredOverlays = GsonHelper.getAsJsonObject(obj, "tiered_overlays", new JsonObject());
        for (var casingTier : tieredOverlays.keySet()) {
            var casingOverlaysJson = MachineOverlaysJson.parse(overlayClass, GsonHelper.getAsJsonObject(tieredOverlays, casingTier),
                    defaultOverlaysJson);
            this.tieredOverlays.put(casingTier, casingOverlaysJson.toSpriteIds());
        }
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState, ItemOverrides overrides) {
        var defaultOverlays = loadSprites(spriteGetter, this.defaultOverlays);
        var tieredOverlays = new HashMap<String, TextureAtlasSprite[]>();
        for (var entry : this.tieredOverlays.entrySet()) {
            tieredOverlays.put(entry.getKey(), loadSprites(spriteGetter, entry.getValue()));
        }
        return modelBaker.bake(baseCasing, outputOverlayIndexes, defaultOverlays, tieredOverlays);
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

    private static class OverlaysJson implements MachineOverlaysJson {
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

        /**
         * Order is as follows:
         * Active and inactive: front, left, back, right, top S/W/N/E, bottom S/W/N/E,
         * output, item auto, fluid auto
         */
        @Override
        public Material[] toSpriteIds() {
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

        @Override
        public int[] getOutputSpriteIndexes() {
            return new int[] { 24, 25, 26 };
        }
    }
}

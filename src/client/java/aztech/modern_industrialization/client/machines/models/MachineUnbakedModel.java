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
import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.Nullable;

import static aztech.modern_industrialization.client.machines.models.OverlayName.*;

public record MachineUnbakedModel(
        MachineCasing baseCasing,
        Map<OverlayName, Identifier> defaultOverlays,
        Map<MachineCasing, Map<OverlayName, Identifier>> tieredOverlays,
        boolean noOverlayOnOutputSide) implements CustomUnbakedBlockStateModel {
    public static final Identifier LOADER_ID = MI.id("machine");

    private static final Codec<Map<OverlayName, Identifier>> OVERLAYS_CODEC = Codec.unboundedMap(OverlayName.CODEC, Identifier.CODEC);
    public static final MapCodec<MachineUnbakedModel> CODEC = RecordCodecBuilder.mapCodec(i ->
            i.group(
                    MachineCasing.CODEC.fieldOf("casing").forGetter(MachineUnbakedModel::baseCasing),
                    OVERLAYS_CODEC.fieldOf("default_overlays").forGetter(MachineUnbakedModel::defaultOverlays),
                    Codec.unboundedMap(MachineCasing.CODEC, OVERLAYS_CODEC).fieldOf("tiered_overlays").forGetter(MachineUnbakedModel::tieredOverlays),
                    Codec.BOOL.fieldOf("no_overlay_on_output_side").forGetter(MachineUnbakedModel::noOverlayOnOutputSide))
                    .apply(i, MachineUnbakedModel::new));

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

    private static @Nullable TextureAtlasSprite[] loadSprites(SpriteGetter spriteGetter, Map<OverlayName, Identifier> overlays) {
        var selectedMaterials = new OverlaySelector(overlays).toMaterials();
        var sprites = new TextureAtlasSprite[selectedMaterials.length];
        for (int i = 0; i < selectedMaterials.length; ++i) {
            if (selectedMaterials[i] != null) {
                sprites[i] = spriteGetter.get(selectedMaterials[i], () -> "machine unbaked model");
            }
        }
        return sprites;
    }

    private record OverlaySelector(Map<OverlayName, Identifier> overlays) {
        /**
         * Order is as follows:
         * Active and inactive: front, left, back, right, top S/W/N/E, bottom S/W/N/E,
         * output, item auto, fluid auto
         */
        private @Nullable Material[] toMaterials() {
            return new @Nullable Material[] {
                    select(FRONT, SIDE),
                    select(FRONT_ACTIVE, FRONT, SIDE_ACTIVE, SIDE),
                    select(LEFT, SIDE),
                    select(LEFT_ACTIVE, LEFT, SIDE_ACTIVE, SIDE),
                    select(BACK, SIDE),
                    select(BACK_ACTIVE, BACK, SIDE_ACTIVE, SIDE),
                    select(RIGHT, SIDE),
                    select(RIGHT_ACTIVE, RIGHT, SIDE_ACTIVE, SIDE),
                    select(TOP_S, TOP),
                    select(TOP_S_ACTIVE, TOP_S, TOP_ACTIVE, TOP),
                    select(TOP_W, TOP),
                    select(TOP_W_ACTIVE, TOP_W, TOP_ACTIVE, TOP),
                    select(TOP_N, TOP),
                    select(TOP_N_ACTIVE, TOP_N, TOP_ACTIVE, TOP),
                    select(TOP_E, TOP),
                    select(TOP_E_ACTIVE, TOP_E, TOP_ACTIVE, TOP),
                    select(BOTTOM_S, BOTTOM),
                    select(BOTTOM_S_ACTIVE, BOTTOM_S, BOTTOM_ACTIVE, BOTTOM),
                    select(BOTTOM_W, BOTTOM),
                    select(BOTTOM_W_ACTIVE, BOTTOM_W, BOTTOM_ACTIVE, BOTTOM),
                    select(BOTTOM_N, BOTTOM),
                    select(BOTTOM_N_ACTIVE, BOTTOM_N, BOTTOM_ACTIVE, BOTTOM),
                    select(BOTTOM_E, BOTTOM),
                    select(BOTTOM_E_ACTIVE, BOTTOM_E, BOTTOM_ACTIVE, BOTTOM),
                    select(OUTPUT),
                    select(ITEM_AUTO),
                    select(FLUID_AUTO),
            };
        }

        /**
         * select first non-null overlay, and convert it to a sprite material.
         */
        @Nullable
        private Material select(OverlayName... candidates) {
            for (var overlay : candidates) {
                var id = overlays.get(overlay);
                if (id != null) {
                    return ClientHooks.getBlockMaterial(id);
                }
            }
            return null;
        }
    }
}

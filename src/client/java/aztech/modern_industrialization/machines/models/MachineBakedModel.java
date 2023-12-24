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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import aztech.modern_industrialization.util.ModelHelper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineBakedModel implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet CUTOUT_MIPPED = ChunkRenderTypeSet.of(RenderType.cutoutMipped());

    private final MachineCasing baseCasing;
    private final TextureAtlasSprite[] defaultOverlays;
    private final Map<String, TextureAtlasSprite[]> tieredOverlays;
    private final MachineModelClientData defaultData;

    private final LoadingCache<MachineModelClientData, List<List<BakedQuad>>> quadsCache = CacheBuilder.newBuilder()
            .build(CacheLoader.from(this::buildQuads));

    MachineBakedModel(MachineCasing baseCasing,
            TextureAtlasSprite[] defaultOverlays,
            Map<String, TextureAtlasSprite[]> tieredOverlays) {
        this.baseCasing = baseCasing;
        this.defaultOverlays = defaultOverlays;
        this.tieredOverlays = tieredOverlays;
        this.defaultData = new MachineModelClientData(baseCasing, Direction.NORTH);
    }

    private List<List<BakedQuad>> buildQuads(MachineModelClientData clientData) {
        MachineCasing casing = clientData.casing == null ? baseCasing : clientData.casing;
        var sprites = getSprites(casing);

        List<List<BakedQuad>> quadLists = new ArrayList<>(7);
        for (int i = 0; i <= 6; ++i) {
            Direction d = ModelHelper.DIRECTIONS_WITH_NULL[i];
            List<BakedQuad> quads = new ArrayList<>();
            var vc = new QuadBakingVertexConsumer(quads::add);

            if (d != null) {
                // Casing
                quads.add(MachineCasingModel.get(casing).getQuad(d));
                // Machine overlays
                TextureAtlasSprite sprite = getSprite(sprites, d, clientData.frontDirection, false);
                if (sprite != null) {
                    ModelHelper.emitSprite(vc, d, sprite, -1e-6f);
                }
            }

            // Output overlays
            if (clientData.outputDirection != null && d == clientData.outputDirection) {
                ModelHelper.emitSprite(vc, clientData.outputDirection, sprites[24], -3e-4f);
                if (clientData.itemAutoExtract) {
                    ModelHelper.emitSprite(vc, clientData.outputDirection, sprites[25], -3e-4f);
                }
                if (clientData.fluidAutoExtract) {
                    ModelHelper.emitSprite(vc, clientData.outputDirection, sprites[26], -3e-4f);
                }
            }

            quadLists.add(List.copyOf(quads));
        }

        return quadLists;
    }

    public TextureAtlasSprite[] getSprites(@Nullable MachineCasing casing) {
        if (casing == null) {
            return defaultOverlays;
        }
        return tieredOverlays.getOrDefault(casing.name, defaultOverlays);
    }

    /**
     * Returns null if nothing should be rendered.
     */
    @Nullable
    public static TextureAtlasSprite getSprite(TextureAtlasSprite[] sprites, Direction side, Direction facingDirection, boolean isActive) {
        int spriteId;
        if (side.getAxis().isHorizontal()) {
            spriteId = (facingDirection.get2DDataValue() - side.get2DDataValue() + 4) % 4 * 2;
        } else {
            spriteId = (facingDirection.get2DDataValue() + 4) * 2;

            if (side == Direction.DOWN) {
                spriteId += 8;
            }
        }
        if (isActive) {
            spriteId++;
        }
        return sprites[spriteId];
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        var data = extraData.get(MachineModelClientData.KEY);
        if (data == null) {
            data = defaultData;
        }

        return quadsCache.getUnchecked(data).get(ModelHelper.nullableDirectionIndex(side));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return MachineCasingModel.get(baseCasing).getSideSprite();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return CUTOUT_MIPPED;
    }
}

package aztech.modern_industrialization.client.machines.models;

import aztech.modern_industrialization.client.util.ModelHelper;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MachineBlockStateModel implements DynamicBlockStateModel {
    public static float Z_OFFSET = 5e-4f; // Cannot be lower due to Embeddium compact vertex format

    private final MachineCasing baseCasing;
    private final @Nullable TextureAtlasSprite[] defaultOverlays;
    private final Map<MachineCasing, @Nullable TextureAtlasSprite[]> tieredOverlays;
    private final boolean noOverlayOnOutputSide;
    private final MachineModelClientData defaultData;

    MachineBlockStateModel(MachineCasing baseCasing,
            @Nullable TextureAtlasSprite[] defaultOverlays,
            Map<MachineCasing, @Nullable TextureAtlasSprite[]> tieredOverlays,
            boolean noOverlayOnOutputSide) {
        this.baseCasing = baseCasing;
        this.defaultOverlays = defaultOverlays;
        this.tieredOverlays = tieredOverlays;
        this.noOverlayOnOutputSide = noOverlayOnOutputSide;
        this.defaultData = new MachineModelClientData(baseCasing, Direction.NORTH);
    }

    public MachineCasing getBaseCasing() {
        return baseCasing;
    }

    public @Nullable TextureAtlasSprite[] getSprites(@Nullable MachineCasing casing) {
        if (casing == null) {
            return defaultOverlays;
        }
        return tieredOverlays.getOrDefault(casing, defaultOverlays);
    }

    /**
     * Returns null if nothing should be rendered.
     */
    @Nullable
    public static TextureAtlasSprite getSprite(@Nullable TextureAtlasSprite[] sprites, Direction side, Direction facingDirection, boolean isActive) {
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

    public QuadCollection assembleOverlayQuads(MachineModelClientData data) {
        MachineCasing casing = Objects.requireNonNullElse(data.casing, baseCasing);
        var sprites = getSprites(casing);

        QuadCollection.Builder quads = new QuadCollection.Builder();

        // Machine overlays
        for (var side : Direction.values()) {
            if (!noOverlayOnOutputSide || side != data.outputDirection) {
                // Draw the "front" texture on the north side if the machine has no facing
                var facingDirection = Objects.requireNonNullElse(data.frontDirection, Direction.NORTH);
                TextureAtlasSprite sprite = getSprite(sprites, side, facingDirection, false);
                if (sprite != null) {
                    quads.addCulledFace(side, ModelHelper.bakeSprite(side, sprite, -Z_OFFSET));
                }
            }
        }

        // Output overlays
        if (data.outputDirection != null) {
            if (sprites[24] != null) {
                quads.addCulledFace(data.outputDirection, ModelHelper.bakeSprite(data.outputDirection, sprites[24], -3 * Z_OFFSET));
            }
            if (data.itemAutoExtract) {
                quads.addCulledFace(data.outputDirection, ModelHelper.bakeSprite(data.outputDirection, sprites[25], -3 * Z_OFFSET));
            }
            if (data.fluidAutoExtract) {
                quads.addCulledFace(data.outputDirection, ModelHelper.bakeSprite(data.outputDirection, sprites[26], -3 * Z_OFFSET));
            }
        }

        return quads.build();
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        var data = level.getModelData(pos).get(MachineModelClientData.KEY);
        if (data == null) {
            data = defaultData;
        }

        var casing = Objects.requireNonNullElse(data.casing, baseCasing);
        CasingModels.getCasingModel(casing).model().collectParts(level, pos, state, random, parts);

        var overlayQuads = assembleOverlayQuads(data);
        parts.add(new SimpleModelWrapper(overlayQuads, true, particleIcon(), ChunkSectionLayer.CUTOUT));
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return CasingModels.getCasingModel(baseCasing).model().particleIcon();
    }
}

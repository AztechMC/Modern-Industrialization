package aztech.modern_industrialization.client.pipes.impl;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record PipeCamouflagePart(
        BlockState blockState,
        QuadCollection quads,
        TriState ambientOcclusion,
        TextureAtlasSprite particleIcon,
        ChunkSectionLayer layer) implements BlockModelPart {
    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return quads.getQuads(direction);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion != TriState.FALSE;
    }

    @Override
    public ChunkSectionLayer getRenderType(BlockState state) {
        return layer;
    }

    // TODO 26.1 - should provide Iris compat but that needs to be verified
    public BlockState getBlockAppearance() {
        return blockState;
    }
}

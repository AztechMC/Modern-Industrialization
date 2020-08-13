package aztech.modern_industrialization.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Random;

/**
 * Reasonable defaults for a model.
 */
public interface BaseModel extends UnbakedModel, BakedModel, FabricBakedModel {
    @Override
    default List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
        return null;
    }

    @Override
    default boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    default boolean hasDepth() {
        return false;
    }

    @Override
    default boolean isSideLit() {
        return false;
    }

    @Override
    default boolean isBuiltin() {
        return false;
    }

    @Override
    default ModelTransformation getTransformation() {
        return ModelUtil.BLOCK_TRANSFORMATION;
    }

    @Override
    default ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    default boolean isVanillaAdapter() {
        return false;
    }
}

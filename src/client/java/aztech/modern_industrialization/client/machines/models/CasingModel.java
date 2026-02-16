package aztech.modern_industrialization.client.machines.models;

import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;

public record CasingModel(BlockStateModel model) {
    public record Unbaked(BlockStateModel.Unbaked unbaked) implements ResolvableModel {
        public static final Codec<CasingModel.Unbaked> CODEC = BlockStateModel.Unbaked.CODEC
                .xmap(Unbaked::new, Unbaked::unbaked);

        public CasingModel bake(ModelBaker modelBakery) {
            return new CasingModel(unbaked.bake(modelBakery));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            unbaked.resolveDependencies(resolver);
        }
    }
}

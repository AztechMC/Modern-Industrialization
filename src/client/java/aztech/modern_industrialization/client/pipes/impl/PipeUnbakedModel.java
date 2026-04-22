package aztech.modern_industrialization.client.pipes.impl;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.client.pipes.MIPipesClient;
import aztech.modern_industrialization.client.pipes.api.PipeRenderer;
import aztech.modern_industrialization.config.MIStartupConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.IdentityHashMap;
import java.util.Map;

public class PipeUnbakedModel implements CustomUnbakedBlockStateModel {
    public static final Identifier TYPE_ID = MI.id("pipe");

    public static final PipeUnbakedModel INSTANCE = new PipeUnbakedModel();
    public static final MapCodec<PipeUnbakedModel> CODEC = MapCodec.unit(INSTANCE);

    private static final Identifier ME_WIRE_CONNECTOR_MODEL = MI.id("part/me_wire_connector");
    private static final Material PARTICLE_SPRITE = new Material(Identifier.parse("minecraft:block/iron_block"));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    public static PipeBlockStateModel getOrBakeBlockModel(ModelBaker modelBakery) {
        return modelBakery.compute(SharedBlockModel.INSTANCE);
    }

    @Override
    public PipeBlockStateModel bake(ModelBaker modelBaker) {
        return getOrBakeBlockModel(modelBaker);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(ME_WIRE_CONNECTOR_MODEL);
    }

    private static class SharedBlockModel implements ModelBaker.SharedOperationKey<PipeBlockStateModel> {
        private static final SharedBlockModel INSTANCE = new SharedBlockModel();

        @Override
        public PipeBlockStateModel compute(ModelBaker modelBakery) {
            Map<PipeRenderer.Factory, PipeRenderer> renderers = new IdentityHashMap<>();
            for (PipeRenderer.Factory rendererFactory : MIPipesClient.RENDERERS) {
                renderers.put(rendererFactory, rendererFactory.create(modelBakery));
            }

            BlockStateModelPart[] meWireConnectors = null;
            if (MIStartupConfig.INSTANCE.loadAe2Compat()) {
                meWireConnectors = RotatedModelHelper.loadRotatedModels(ME_WIRE_CONNECTOR_MODEL, modelBakery);
            }

            return new PipeBlockStateModel(
                    modelBakery.materials().get(PARTICLE_SPRITE, () -> "pipe model"),
                    renderers,
                    meWireConnectors);
        }
    }
}

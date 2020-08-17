package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.MIIdentifier;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

public class PipeModelProvider implements ModelResourceProvider {
    private static final PipeModel PIPE_MODEL = new PipeModel();
    private static final Identifier PIPE_MODEL_ID = new MIIdentifier("block/pipe");

    @Override
    public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) {
        return identifier.equals(PIPE_MODEL_ID) ? PIPE_MODEL : null;
    }
}

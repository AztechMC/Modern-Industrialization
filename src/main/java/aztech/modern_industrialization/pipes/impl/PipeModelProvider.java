package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.MIIdentifier;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

public class PipeModelProvider implements ModelResourceProvider {
    private static final PipeModel PIPE_MODEL = new PipeModel();
    public static Set<Identifier> modelNames = new HashSet<>();

    @Override
    public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) {
        return modelNames.contains(identifier) ? PIPE_MODEL : null;
    }

    static {
        modelNames.add(new MIIdentifier("block/pipe"));
    }
}

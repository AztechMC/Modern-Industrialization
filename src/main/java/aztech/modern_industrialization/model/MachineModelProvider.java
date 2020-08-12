package aztech.modern_industrialization.model;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import java.util.Map;

public class MachineModelProvider implements ModelResourceProvider {
    private final Map<Identifier, UnbakedModel> modelMap;

    public MachineModelProvider(Map<Identifier, UnbakedModel> modelMap) {
        this.modelMap = modelMap;
    }

    @Override
    public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) throws ModelProviderException {
        return modelMap.getOrDefault(identifier, null);
    }
}

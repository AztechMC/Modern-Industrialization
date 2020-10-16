package aztech.modern_industrialization.model.block;

import aztech.modern_industrialization.model.Models;
import java.util.Map;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

public class ModelProvider implements ModelResourceProvider {

    public static Map<Identifier, UnbakedModel> modelMap = Models.getModelMap();

    public ModelProvider() {
    }

    @Override
    public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) throws ModelProviderException {
        return modelMap.getOrDefault(identifier, null);
    }
}

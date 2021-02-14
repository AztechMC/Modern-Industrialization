package aztech.modern_industrialization.machinesv2.models;

import net.fabricmc.fabric.api.client.model.ExtraModelProvider;
import net.fabricmc.fabric.api.client.model.ModelAppender;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MachineModelProvider implements ModelResourceProvider, ExtraModelProvider {
    private static final Map<Identifier, UnbakedModel> modelMap = new HashMap<>();
    private static final List<Identifier> manuallyLoadedModels = new ArrayList<>();

    public static void register(Identifier id, UnbakedModel model) {
        if (modelMap.put(id, model) != null) {
            throw new RuntimeException("Duplicate registration of model " + id);
        }
    }

    public static void loadManually(Identifier identifier) {
        manuallyLoadedModels.add(identifier);
    }

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) {
        return modelMap.get(resourceId);
    }

    @Override
    public void provideExtraModels(ResourceManager manager, Consumer<Identifier> out) {
        for (Identifier id : manuallyLoadedModels) {
            out.accept(id);
        }
    }
}

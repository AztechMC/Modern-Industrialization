package aztech.modern_industrialization.client.machines.models;

import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.BlockStateDefinitions;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.fml.ModWorkManager;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class CasingModels {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String FOLDER_NAME = "modern_industrialization/machine_casings";
    private static final FileToIdConverter CASINGS_LISTER = FileToIdConverter.json(FOLDER_NAME);

    private record LoadedModels(Map<MachineCasing, CasingModel.Unbaked> models) {}

    private static CompletableFuture<LoadedModels> loadModels(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> CASINGS_LISTER.listMatchingResources(manager), executor)
                .thenCompose(
                        resources -> {
                            List<CompletableFuture<Map.@Nullable Entry<MachineCasing, CasingModel.Unbaked>>> result = new ArrayList<>(resources.size());

                            for (var resourceEntry : resources.entrySet()) {
                                result.add(CompletableFuture.<Map.@Nullable Entry<MachineCasing, CasingModel.Unbaked>>supplyAsync(
                                        () -> {
                                            Identifier casingId = CASINGS_LISTER.fileToId(resourceEntry.getKey());
                                            var casing = MachineCasings.getOrNull(casingId);
                                            if (casing == null) {
                                                LOGGER.debug("Discovered unknown machine casing definition {}, ignoring", casingId);
                                                return null;
                                            }

                                            try (var reader = resourceEntry.getValue().openAsReader()) {
                                                JsonElement element = StrictJsonParser.parse(reader);
                                                return Map.entry(
                                                        casing,
                                                        CasingModel.Unbaked.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow(JsonParseException::new));
                                            } catch (Exception exception) {
                                                LOGGER.error(
                                                        "Failed to load machine casing model {} from pack {}", casingId, resourceEntry.getValue().sourcePackId(), exception);
                                                return null;
                                            }
                                        },
                                        executor)
                                );
                            }

                            return Util.sequence(result).thenApply(entries -> {
                                //noinspection unchecked
                                return new LoadedModels(Map.ofEntries(entries.toArray(Map.Entry[]::new)));
                            });
                        });
    }

    private static final StandaloneModelKey<Map<MachineCasing, CasingModel>> KEY = new StandaloneModelKey<>(() -> "casing models");

    public static void loadModels(ModelEvent.RegisterStandalone event) {
        var resourceManager = Minecraft.getInstance().getResourceManager();
        // TODO: this should be made to use the executor of the reload...
        var executor = ModWorkManager.parallelExecutor();

        LoadedModels loadedModels = loadModels(resourceManager, executor).join();

        event.register(KEY, new UnbakedStandaloneModel<>() {
            @Override
            public Map<MachineCasing, CasingModel> bake(ModelBaker baker) {
                Map<MachineCasing, CasingModel> bakedModels = new HashMap<>();
                for (var entry : loadedModels.models.entrySet()) {
                    bakedModels.put(entry.getKey(), entry.getValue().bake(baker));
                }
                return bakedModels;
            }

            @Override
            public void resolveDependencies(Resolver resolver) {
                for (var model : loadedModels.models.values()) {
                    model.resolveDependencies(resolver);
                }
            }
        });
    }

    public static CasingModel getCasingModel(MachineCasing casing) {
        var models = Objects.requireNonNull(Minecraft.getInstance().getModelManager().getStandaloneModel(KEY), "casing models");
        return Objects.requireNonNull(models.get(casing), "casing model");
    }
}

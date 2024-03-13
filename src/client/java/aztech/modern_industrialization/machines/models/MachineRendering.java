/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machines.models;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntityRenderer;
import aztech.modern_industrialization.machines.blockentities.multiblocks.LargeTankMultiblockBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.MultiblockTankBER;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class MachineRendering {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation getCasingModelId(MachineCasing casing) {
        return new MIIdentifier("machine_casing/" + casing.name);
    }

    public static BakedModel getCasingModel(MachineCasing casing) {
        return Minecraft.getInstance().getModelManager().getModel(getCasingModelId(casing));
    }

    public static void init() {
        for (var blockDef : MIBlock.BLOCKS.values()) {
            if (blockDef.asBlock() instanceof MachineBlock machine) {
                registerBer(machine);
            }
        }

        PreparableModelLoadingPlugin.register(
                MachineRendering::prepareUnbakedModels,
                (modelsToResolve, pluginCtx) -> {
                    for (var casing : MachineCasings.registeredCasings.values()) {
                        pluginCtx.addModels(getCasingModelId(casing));
                    }

                    pluginCtx.resolveModel().register(ctx -> {
                        if (!ctx.id().getNamespace().equals(ModernIndustrialization.MOD_ID)) {
                            return null;
                        }

                        return modelsToResolve.get(ctx.id());
                    });
                });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void registerBer(MachineBlock machine) {
        var blockEntity = machine.getBlockEntityInstance();
        BlockEntityType type = blockEntity.getType();

        if (blockEntity instanceof LargeTankMultiblockBlockEntity) {
            BlockEntityRenderers.register(type, MultiblockTankBER::new);
        } else if (blockEntity instanceof MultiblockMachineBlockEntity) {
            BlockEntityRenderers.register(type, MultiblockMachineBER::new);
        } else {
            BlockEntityRenderers.register(type, c -> new MachineBlockEntityRenderer(c));
        }
    }

    private static List<CompletableFuture<Pair<ResourceLocation, @Nullable UnbakedModel>>> startLoadingMachineModels(ResourceManager resourceManager,
            Executor executor) {
        List<CompletableFuture<Pair<ResourceLocation, @Nullable UnbakedModel>>> futures = new ArrayList<>();

        for (var entry : MachineBlock.REGISTERED_MACHINES.entrySet()) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                var machine = entry.getKey();
                var casing = entry.getValue();

                try (var resource = resourceManager.getResource(new MIIdentifier("models/machine/" + machine + ".json")).get()
                        .openAsReader()) {
                    // Use item id for the model id, the blockstate file also redirects to it.
                    return Pair.of(new MIIdentifier("item/" + machine), MachineUnbakedModel.deserialize(casing, resource));
                } catch (IOException exception) {
                    LOGGER.error("Failed to find machine model json for machine " + machine, exception);
                } catch (RuntimeException exception) {
                    LOGGER.error("Failed to load machine model json for machine " + machine, exception);
                }
                return null;
            }, executor));
        }

        return futures;
    }

    private static List<CompletableFuture<Pair<ResourceLocation, @Nullable UnbakedModel>>> startLoadingCasingModels(ResourceManager resourceManager,
            Executor executor) {
        List<CompletableFuture<Pair<ResourceLocation, @Nullable UnbakedModel>>> futures = new ArrayList<>();

        for (var casing : MachineCasings.registeredCasings.values()) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                var jsonLocation = getCasingModelId(casing).withPath(p -> "models/" + p + ".json");
                var resource = resourceManager.getResource(jsonLocation);

                UnbakedModel unbakedModel;
                if (resource.isEmpty()) {
                    // No resource, assume it's a standard BlockModel with top, side and bottom textures.
                    try {
                        unbakedModel = BlockModel.fromString("""
                                {
                                    "parent": "block/cube",
                                    "textures": {
                                        "side": "modern_industrialization:block/casings/%s/side",
                                        "particle": "#side",
                                        "up": "modern_industrialization:block/casings/%s/top",
                                        "down": "modern_industrialization:block/casings/%s/bottom",
                                        "north": "#side",
                                        "south": "#side",
                                        "west": "#side",
                                        "east": "#side"
                                    }
                                }
                                """.formatted(casing.name, casing.name, casing.name));
                    } catch (RuntimeException exception) {
                        LOGGER.error("Failed to construct default casing model: " + casing.name, exception);
                        return null;
                    }
                } else {
                    try (var stream = resource.get().openAsReader()) {
                        var json = JsonParser.parseReader(stream).getAsJsonObject();

                        var blockId = GsonHelper.getAsString(json, "block");
                        var block = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(blockId))
                                .orElseThrow(() -> new JsonSyntaxException("Expected \"block\" to be a block, was unknown string " + blockId));
                        unbakedModel = new ForwardingCasingUnbakedModel(block.defaultBlockState());
                    } catch (IOException exception) {
                        LOGGER.error("Failed to load casing model json for casing " + casing.name, exception);
                        return null;
                    } catch (RuntimeException exception) {
                        LOGGER.error("Failed to parse casing model json for casing " + casing.name, exception);
                        return null;
                    }
                }

                return Pair.of(getCasingModelId(casing), unbakedModel);
            }, executor));
        }

        return futures;
    }

    /**
     * Load all machine jsons in parallel.
     */
    private static CompletableFuture<Map<ResourceLocation, UnbakedModel>> prepareUnbakedModels(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.completedFuture(Boolean.TRUE)
                .thenComposeAsync(ignored -> {
                    List<CompletableFuture<Pair<ResourceLocation, @Nullable UnbakedModel>>> modelFutures = new ArrayList<>();

                    modelFutures.addAll(startLoadingMachineModels(resourceManager, executor));
                    modelFutures.addAll(startLoadingCasingModels(resourceManager, executor));

                    return Util.sequence(modelFutures)
                            .thenApply(list -> list.stream().filter(Objects::nonNull).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
                }, executor);
    }

    private MachineRendering() {
    }
}

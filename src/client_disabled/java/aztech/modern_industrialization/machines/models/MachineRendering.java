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
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.Util;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;

public final class MachineRendering {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        for (var casing : MachineCasings.registeredCasings.values()) {
            casing.model = new MachineCasingModel(casing.name);
        }

        for (var blockDef : MIBlock.BLOCKS.values()) {
            if (blockDef.asBlock() instanceof MachineBlock machine) {
                registerBer(machine);
            }
        }

        PreparableModelLoadingPlugin.register(
                MachineRendering::prepareUnbakedModels,
                (modelsToResolve, pluginCtx) -> {
                    for (var casing : MachineCasings.registeredCasings.values()) {
                        var casingModel = (MachineCasingModel) casing.model;
                        pluginCtx.addModels(casingModel.id);
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

    /**
     * Load all machine jsons in parallel.
     */
    private static CompletableFuture<Map<ResourceLocation, UnbakedModel>> prepareUnbakedModels(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.completedFuture(MachineBlock.REGISTERED_MACHINES)
                .thenComposeAsync(machinesToLoad -> {
                    List<CompletableFuture<Pair<ResourceLocation, MachineUnbakedModel>>> futures = new ArrayList<>();

                    for (var entry : machinesToLoad.entrySet()) {
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

                    return Util.sequence(futures)
                            .thenApply(list -> list.stream().filter(Objects::nonNull).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
                }, executor)
                .thenApply(machineModels -> {
                    Map<ResourceLocation, UnbakedModel> unbakedModels = new HashMap<>(machineModels);

                    for (var casing : MachineCasings.registeredCasings.values()) {
                        var casingModel = (MachineCasingModel) casing.model;
                        unbakedModels.put(casingModel.id, casingModel);
                    }

                    return unbakedModels;
                });
    }

    private MachineRendering() {
    }
}

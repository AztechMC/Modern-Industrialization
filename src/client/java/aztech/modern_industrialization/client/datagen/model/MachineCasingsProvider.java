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

package aztech.modern_industrialization.client.datagen.model;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.client.machines.models.CasingModel;
import aztech.modern_industrialization.client.machines.models.CasingModels;
import aztech.modern_industrialization.client.machines.models.UseBlockModelBakedModel;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MachineCasingsProvider {
    private final PackOutput.PathProvider machineCasingsPathProvider;
    private final Map<Identifier, CasingModel.Unbaked> casingModels = new HashMap<>();

    public MachineCasingsProvider(PackOutput output) {
        this.machineCasingsPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, CasingModels.FOLDER_NAME);
    }

    public CompletableFuture<?> saveAll(CachedOutput cache) {
        return DataProvider.saveAll(cache, CasingModel.Unbaked.CODEC, machineCasingsPathProvider, casingModels);
    }

    protected void generateModels(BlockModelGenerators blockModels) {
        for (var casing : MachineCasings.registeredCasings.values()) {
            if (casing.imitatedBlock != null) {
                imitateBlock(casing, casing.imitatedBlock.get());
            }
        }

        imitateBlock(CableTier.LV.casing, MIBlock.BASIC_MACHINE_HULL.asBlock());
        imitateBlock(CableTier.MV.casing, MIBlock.ADVANCED_MACHINE_HULL.asBlock());
        imitateBlock(CableTier.HV.casing, MIBlock.TURBO_MACHINE_HULL.asBlock());
        imitateBlock(CableTier.EV.casing, MIBlock.HIGHLY_ADVANCED_MACHINE_HULL.asBlock());
        imitateBlock(CableTier.SUPERCONDUCTOR.casing, MIBlock.QUANTUM_MACHINE_HULL.asBlock());

        cubeBottomTop(blockModels, MachineCasings.BRICKED_BRONZE, "block/casings/bricked_bronze", "block/fire_clay_bricks", "block/bronze_machine_casing");
        cubeBottomTop(blockModels, MachineCasings.BRICKED_STEEL, "block/casings/bricked_steel", "block/fire_clay_bricks", "block/steel_machine_casing");
        cubeAll(blockModels, MachineCasings.CONFIGURABLE_TANK, "block/casings/configurable_tank");
        cubeAll(blockModels, MachineCasings.STEEL_CRATE, "block/casings/steel_crate");
    }

    private void generateCasing(MachineCasing casing, BlockStateModel.Unbaked model) {
        casingModels.put(casing.key, new CasingModel.Unbaked(model));
    }

    private void imitateBlock(MachineCasing casing, Block block) {
        generateCasing(casing, new UseBlockModelBakedModel.Unbaked(block));
    }

    private void cubeBottomTop(BlockModelGenerators blockModels, MachineCasing casing, String side, String bottom, String top) {
        var modelId = casing.key.withPrefix("machine_casing/");
        ModelTemplates.CUBE_BOTTOM_TOP.create(
                modelId,
                new TextureMapping()
                        .put(TextureSlot.SIDE, new Material(MI.id(side)))
                        .put(TextureSlot.BOTTOM, new Material(MI.id(bottom)))
                        .put(TextureSlot.TOP, new Material(MI.id(top))),
                blockModels.modelOutput);
        generateCasing(casing, new SingleVariant.Unbaked(BlockModelGenerators.plainModel(modelId)));
    }

    private void cubeAll(BlockModelGenerators blockModels, MachineCasing casing, String side) {
        var modelId = casing.key.withPrefix("machine_casing/");
        ModelTemplates.CUBE_ALL.create(
                modelId,
                new TextureMapping().put(TextureSlot.ALL, new Material(MI.id(side))),
                blockModels.modelOutput);
        generateCasing(casing, new SingleVariant.Unbaked(BlockModelGenerators.plainModel(modelId)));
    }
}

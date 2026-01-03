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
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.client.machines.models.MachineItemModel;
import aztech.modern_industrialization.client.machines.models.MachineUnbakedModel;
import aztech.modern_industrialization.client.machines.models.OverlayName;
import aztech.modern_industrialization.client.pipes.impl.PipeUnbakedModel;
import aztech.modern_industrialization.datagen.model.MachineModelsToGenerate;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.pipes.MIPipes;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MIModelProvider extends ModelProvider {
    private final MachineCasingsProvider casingsProvider;

    public MIModelProvider(PackOutput output) {
        super(output, MI.ID);
        this.casingsProvider = new MachineCasingsProvider(output);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var parentFuture = super.run(cache);
        var casingModelsFuture = casingsProvider.saveAll(cache);
        return CompletableFuture.allOf(parentFuture, casingModelsFuture);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (BlockDefinition<?> blockDefinition : MIBlock.BLOCK_DEFINITIONS.values()) {
            blockDefinition.modelGenerator.accept(blockDefinition.asBlock(), blockModels);
        }

        for (FluidDefinition fluidDefinition : MIFluids.FLUID_DEFINITIONS.values()) {
            blockModels.createNonTemplateModelBlock(fluidDefinition.asFluidBlock(), Blocks.AIR);
        }

        blockModels.blockStateOutput.accept(customModel(MIPipes.BLOCK_PIPE.get(), PipeUnbakedModel.INSTANCE));

        // Item models as well...
        for (ItemDefinition<?> itemDefinition : MIItem.ITEM_DEFINITIONS.values()) {
            itemDefinition.modelGenerator.accept(itemDefinition.asItem(), itemModels);
        }

        // Machine models
        for (var entry : MachineModelsToGenerate.props.entrySet()) {
            var block = BuiltInRegistries.BLOCK.getValue(MI.id(entry.getKey()));
            var props = entry.getValue();

            var defaultOverlays = new HashMap<OverlayName, Identifier>();
            for (var overlay : props.defaultOverlays().entrySet()) {
                defaultOverlays.put(OverlayName.CODEC.byName(overlay.getKey()), overlay.getValue());
            }

            var model = new MachineUnbakedModel(
                    props.casing(),
                    defaultOverlays,
                    Map.of(),
                    props.noOverlayOnOutputSide());
            blockModels.blockStateOutput.accept(customModel(block, model));
            itemModels.itemModelOutput.accept(block.asItem(), new MachineItemModel.Unbaked(block));
        }

        casingsProvider.generateModels(blockModels);
    }

    private static MultiVariantGenerator customModel(Block block, CustomUnbakedBlockStateModel customModel) {
        return MultiVariantGenerator.dispatch(block, MultiVariant.of(new CustomBlockStateModelBuilder.Simple(customModel)));
    }
}

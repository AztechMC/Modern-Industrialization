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
import aztech.modern_industrialization.MIComponents;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.client.machines.models.MachineItemModel;
import aztech.modern_industrialization.client.machines.models.MachineUnbakedModel;
import aztech.modern_industrialization.client.machines.models.OverlayName;
import aztech.modern_industrialization.client.pipes.impl.PipeItemModel;
import aztech.modern_industrialization.client.pipes.impl.PipeUnbakedModel;
import aztech.modern_industrialization.client.util.UseBlockEntityRenderer;
import aztech.modern_industrialization.datagen.model.MachineModelsToGenerate;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.definition.GeneratedBlockModel;
import aztech.modern_industrialization.definition.GeneratedItemModel;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.pipes.MIPipes;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            var block = blockDefinition.asBlock();
            switch (blockDefinition.generatedBlockModel) {
                case GeneratedBlockModel.TrivialCube() -> {
                    blockModels.createTrivialCube(block);
                }
                case GeneratedBlockModel.TrivialColumn() -> {
                    blockModels.createTrivialBlock(block, TexturedModel.COLUMN);
                }
                case GeneratedBlockModel.Explosive() -> {
                    var mapping = new TextureMapping()
                            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.TNT, "_top"))
                            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.TNT, "_bottom"))
                            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block));

                    blockModels.createTrivialBlock(block, b -> new TexturedModel(mapping, ModelTemplates.CUBE_BOTTOM_TOP));
                }
                case GeneratedBlockModel.Tank() -> {
                    var textureSlot = TextureSlot.create("0");
                    var template = new ModelTemplate(Optional.of(MI.id("base/tank")), Optional.empty(), textureSlot);
                    var texturedModel = TexturedModel.createDefault(b -> new TextureMapping().put(textureSlot, TextureMapping.getBlockTexture(b)), template);
                    blockModels.createTrivialBlock(block, texturedModel);
                }
                case GeneratedBlockModel.NoTemplateModel() -> {
                    blockModels.createNonTemplateModelBlock(block);
                }
                case GeneratedBlockModel.None() -> {}
            }
        }

        for (FluidDefinition fluidDefinition : MIFluids.FLUID_DEFINITIONS.values()) {
            blockModels.createNonTemplateModelBlock(fluidDefinition.asFluidBlock(), Blocks.AIR);
        }

        blockModels.blockStateOutput.accept(customModel(MIPipes.BLOCK_PIPE.get(), PipeUnbakedModel.INSTANCE));

        // Item models as well...
        {
            // Redstone control module!
            var highModelId = MI.id("item/redstone_control_module_high");
            ModelTemplates.FLAT_ITEM.create(
                    highModelId,
                    new TextureMapping().put(TextureSlot.LAYER0, new Material(MI.id("item/redstone_control_module_high"))),
                    itemModels.modelOutput);
            var lowModelId = MI.id("item/redstone_control_module_low");
            ModelTemplates.FLAT_ITEM.create(
                    lowModelId,
                    new TextureMapping().put(TextureSlot.LAYER0, new Material(MI.id("item/redstone_control_module_low"))),
                    itemModels.modelOutput);
            itemModels.itemModelOutput.accept(MIItem.REDSTONE_CONTROL_MODULE.asItem(), new SelectItemModel.Unbaked(
                    Optional.empty(),
                    new SelectItemModel.UnbakedSwitch<>(
                            new ComponentContents<>(MIComponents.LOW_SIGNAL.get()),
                            List.of(
                                    new SelectItemModel.SwitchCase<>(List.of(false), new CuboidItemModelWrapper.Unbaked(highModelId, Optional.empty(), List.of())),
                                    new SelectItemModel.SwitchCase<>(List.of(true), new CuboidItemModelWrapper.Unbaked(lowModelId, Optional.empty(), List.of())))),
                    Optional.empty()));
        }

        for (ItemDefinition<?> itemDefinition : MIItem.ITEM_DEFINITIONS.values()) {
            var item = itemDefinition.asItem();
            switch (itemDefinition.generatedItemModel) {
                case GeneratedItemModel.FlatItem() -> {
                    itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
                }
                case GeneratedItemModel.FlatHandheldItem() -> {
                    itemModels.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
                }
                case GeneratedItemModel.BlockEntityRenderer() -> {
                    var baseBlockModel = BuiltInRegistries.ITEM.getKey(item).withPrefix("block/");
                    itemModels.itemModelOutput.accept(item, new CompositeModel.Unbaked(List.of(
                            new CuboidItemModelWrapper.Unbaked(baseBlockModel, Optional.empty(), List.of()),
                            new SpecialModelWrapper.Unbaked(baseBlockModel, Optional.empty(), new UseBlockEntityRenderer.Unbaked())),
                            Optional.empty()
                    ));
                }
                case GeneratedItemModel.Pipe() -> {
                    itemModels.itemModelOutput.accept(item, new PipeItemModel.Unbaked());
                }
                case GeneratedItemModel.Custom() -> {
                    itemModels.declareCustomModelItem(item);
                }
                case GeneratedItemModel.None() -> {}
            }
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

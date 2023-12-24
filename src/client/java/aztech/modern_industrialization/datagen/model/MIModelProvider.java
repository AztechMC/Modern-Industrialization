package aztech.modern_industrialization.datagen.model;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.machines.models.MachineCasingHolderModel;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MIModelProvider extends BaseModelProvider {
    public MIModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (BlockDefinition<?> blockDefinition : MIBlock.BLOCK_DEFINITIONS.values()) {
            if (blockDefinition.modelGenerator != null) {
                blockDefinition.modelGenerator.accept(blockDefinition.asBlock(), this);
            }
        }

        for (FluidDefinition fluidDefinition : MIFluids.FLUID_DEFINITIONS.values()) {
            existingModel(fluidDefinition.asFluidBlock(), Blocks.AIR);
        }

        // TODO NEO
//        blockStateModelGenerator.createNonTemplateModelBlock(MIPipes.BLOCK_PIPE);

        // Item models as well...
        for (ItemDefinition<?> itemDefinition : MIItem.ITEM_DEFINITIONS.values()) {
            if (itemDefinition.modelGenerator != null) {
                itemDefinition.modelGenerator.accept(itemDefinition.asItem(), itemModels());
            }
        }

        // Custom loader to bake machine casing models
        models().getBuilder(MachineCasingHolderModel.MODEL_ID.toString()).customLoader((parent, exFile) -> new CustomLoaderBuilder<BlockModelBuilder>(MachineCasingHolderModel.LOADER_ID, parent, exFile, false) {
            @Override
            public JsonObject toJson(JsonObject json) {
                return super.toJson(json);
            }
        });
        
        // Machine models
        for (var entry : MachineModelsToGenerate.props.entrySet()) {
            simpleBlockWithItem(BuiltInRegistries.BLOCK.get(MI.id(entry.getKey())), models()
                    .getBuilder(entry.getKey())
                        .customLoader((bmb, exFile) -> new MachineModelBuilder<>(entry.getValue(), bmb, exFile))
                    .end());
        }
    }
}

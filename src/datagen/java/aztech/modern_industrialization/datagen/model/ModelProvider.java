package aztech.modern_industrialization.datagen.model;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.definition.ItemDefinition;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;

public class ModelProvider extends FabricModelProvider {


    public ModelProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        for(ItemDefinition<?> itemDefinition : MIItem.ITEMS.values()){
            itemDefinition.modelGenerator.accept(itemDefinition.asItem(), itemModelGenerator);
        }
    }


}
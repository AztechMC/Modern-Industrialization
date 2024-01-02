package aztech.modern_industrialization.datagen.model;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.BiFunction;

public class TrivialModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> BiFunction<T, ExistingFileHelper, TrivialModelBuilder<T>> begin(ResourceLocation loaderId) {
        return (parent, helper) -> new TrivialModelBuilder<>(loaderId, parent, helper);
    }

    private TrivialModelBuilder(ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper) {
        super(loaderId, parent, existingFileHelper, false);
    }
}

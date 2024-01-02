package aztech.modern_industrialization.datagen.model;

import aztech.modern_industrialization.MI;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class DelegatingModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static final ResourceLocation LOADER_ID = MI.id("delegate");
    private ResourceLocation delegate;

    public DelegatingModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(LOADER_ID, parent, existingFileHelper, false);
    }

    public DelegatingModelBuilder<T> delegate(ModelFile file) {
        this.delegate = file.getLocation();
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json.addProperty("delegate", this.delegate.toString());
        return super.toJson(json);
    }
}

package aztech.modern_industrialization.machines.models;

import aztech.modern_industrialization.MI;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.EmptyModel;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class MachineCasingHolderModel implements IUnbakedGeometry<MachineCasingHolderModel> {
    public static final ResourceLocation MODEL_ID = MI.id("misc/machine_casing_holder");
    public static final ResourceLocation LOADER_ID = MI.id("machine_casing_holder");
    public static final IGeometryLoader<MachineCasingHolderModel> LOADER = (object, context) -> {
        return new MachineCasingHolderModel();
    };

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        // Bake all machine casings
        for (var casing : MachineCasings.registeredCasings.values()) {
            casing.model = new MachineCasingModel(casing.name, spriteGetter);
        }
        // Return a dummy model...
        return EmptyModel.BAKED;
    }
}

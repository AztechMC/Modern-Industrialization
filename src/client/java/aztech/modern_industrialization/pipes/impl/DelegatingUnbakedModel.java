package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.MI;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class DelegatingUnbakedModel implements IUnbakedGeometry<DelegatingUnbakedModel> {
    public static final IGeometryLoader<DelegatingUnbakedModel> LOADER = (object, context) -> {
        var loc = GsonHelper.getAsString(object, "delegate");
        return new DelegatingUnbakedModel(new ResourceLocation(loc));
    };

    private final ResourceLocation delegate;

    public DelegatingUnbakedModel(ResourceLocation delegate) {
        this.delegate = delegate;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        return baker.bake(delegate, modelState, spriteGetter);
    }
}

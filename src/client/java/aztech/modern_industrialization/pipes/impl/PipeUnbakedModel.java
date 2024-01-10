package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import aztech.modern_industrialization.thirdparty.fabricrendering.SpriteFinderImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.EmptyModel;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class PipeUnbakedModel implements IUnbakedGeometry<PipeUnbakedModel> {
    public static final ResourceLocation LOADER_ID = MI.id("pipe");
    public static final IGeometryLoader<PipeUnbakedModel> LOADER = (object, context) -> {
        return new PipeUnbakedModel();
    };

    private static final ResourceLocation ME_WIRE_CONNECTOR_MODEL = MI.id("part/me_wire_connector");
    private static final Material PARTICLE_SPRITE = new Material(InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("minecraft:block/iron_block"));

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        Map<PipeRenderer.Factory, PipeRenderer> renderers = new IdentityHashMap<>();
        for (PipeRenderer.Factory rendererFactory : MIPipesClient.RENDERERS) {
            renderers.put(rendererFactory, rendererFactory.create(spriteGetter));
        }

        BakedModel[] meWireConnectors = null;
        if (MIConfig.loadAe2Compat()) {
            // TODO NEO
            //meWireConnectors = RotatedModelHelper.loadRotatedModels(ME_WIRE_CONNECTOR_MODEL, baker);
        }

        var blockAtlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        var spriteFinder = new SpriteFinderImpl(blockAtlas.getTextures(), blockAtlas);

        return new PipeBakedModel(
                spriteGetter.apply(PARTICLE_SPRITE),
                renderers,
                meWireConnectors,
                spriteFinder);
    }
}

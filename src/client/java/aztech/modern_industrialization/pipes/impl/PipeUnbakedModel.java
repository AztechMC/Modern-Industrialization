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
package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import aztech.modern_industrialization.thirdparty.fabricrendering.SpriteFinderImpl;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public class PipeUnbakedModel implements IUnbakedGeometry<PipeUnbakedModel> {
    public static final ResourceLocation LOADER_ID = MI.id("pipe");
    public static final IGeometryLoader<PipeUnbakedModel> LOADER = (object, context) -> {
        return new PipeUnbakedModel();
    };

    private static final ResourceLocation ME_WIRE_CONNECTOR_MODEL = MI.id("part/me_wire_connector");
    private static final Material PARTICLE_SPRITE = new Material(InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("minecraft:block/iron_block"));

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        Map<PipeRenderer.Factory, PipeRenderer> renderers = new IdentityHashMap<>();
        for (PipeRenderer.Factory rendererFactory : MIPipesClient.RENDERERS) {
            renderers.put(rendererFactory, rendererFactory.create(spriteGetter));
        }

        BakedModel[] meWireConnectors = null;
        if (MIConfig.loadAe2Compat()) {
            meWireConnectors = RotatedModelHelper.loadRotatedModels(ME_WIRE_CONNECTOR_MODEL, baker, spriteGetter);
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

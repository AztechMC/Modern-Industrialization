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

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;

public class PipeUnbakedModel implements UnbakedModel {
    private static final ResourceLocation ME_WIRE_CONNECTOR_MODEL = new MIIdentifier("part/me_wire_connector");
    private static final Material PARTICLE_SPRITE = new Material(InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation("minecraft:block/iron_block"));

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return MIConfig.loadAe2Compat() ? List.of(ME_WIRE_CONNECTOR_MODEL) : List.of();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ResourceLocation location) {
        Map<PipeRenderer.Factory, PipeRenderer> renderers = new IdentityHashMap<>();
        for (PipeRenderer.Factory rendererFactory : MIPipesClient.RENDERERS) {
            renderers.put(rendererFactory, rendererFactory.create(spriteGetter));
        }

        @Nullable
        BakedModel[] meWireConnectors = null;
        if (MIConfig.loadAe2Compat()) {
            meWireConnectors = RotatedModelHelper.loadRotatedModels(ME_WIRE_CONNECTOR_MODEL, baker);
        }

        return new PipeBakedModel(
                spriteGetter.apply(PARTICLE_SPRITE),
                renderers,
                meWireConnectors,
                RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(BlendMode.TRANSLUCENT).find());
    }
}

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
package aztech.modern_industrialization.machines.models;

import aztech.modern_industrialization.MI;
import com.google.gson.JsonSyntaxException;
import java.util.function.Function;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public class UseBlockModelUnbakedModel implements IUnbakedGeometry<UseBlockModelUnbakedModel> {
    public static final ResourceLocation LOADER_ID = MI.id("use_block_model");
    public static final IGeometryLoader<UseBlockModelUnbakedModel> LOADER = (jsonObject, deserializationContext) -> {

        var blockId = GsonHelper.getAsString(jsonObject, "block");
        var block = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(blockId))
                .orElseThrow(() -> new JsonSyntaxException("Expected \"block\" to be a block, was unknown string " + blockId));

        return new UseBlockModelUnbakedModel(block.defaultBlockState());
    };

    private final BlockState targetState;
    private final ResourceLocation delegate;

    private UseBlockModelUnbakedModel(BlockState targetState) {
        this.targetState = targetState;
        this.delegate = BlockModelShaper.stateToModelLocation(targetState);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new UseBlockModelBakedModel(targetState, baker.bake(delegate, modelState, spriteGetter));
    }
}

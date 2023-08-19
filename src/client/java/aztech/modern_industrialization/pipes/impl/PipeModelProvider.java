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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.MIPipes;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.model.loading.v1.DelegatingUnbakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class PipeModelProvider implements ModelResourceProvider {
    public static final ResourceLocation BLOCK_MODEL = new MIIdentifier("block/pipe");

    private final UnbakedModel itemModel = new DelegatingUnbakedModel(BLOCK_MODEL);

    @Override
    @Nullable
    public UnbakedModel loadModelResource(ResourceLocation identifier, ModelProviderContext modelProviderContext) {
        if (BLOCK_MODEL.equals(identifier)) {
            return new PipeUnbakedModel();
        }

        if (MIPipes.ITEM_PIPE_MODELS.contains(identifier)) {
            return itemModel;
        }

        return null;
    }
}

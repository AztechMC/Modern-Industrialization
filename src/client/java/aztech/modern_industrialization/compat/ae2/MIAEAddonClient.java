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

package aztech.modern_industrialization.compat.ae2;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import aztech.modern_industrialization.pipes.impl.PipeMeshCache;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.inventory.InventoryMenu;

public class MIAEAddonClient {

    private static final PipeRenderer.Factory ME_RENDERER = new PipeRenderer.Factory() {
        @Override
        public Collection<Material> getSpriteDependencies() {
            return List.of(new Material(InventoryMenu.BLOCK_ATLAS, MI.id("block/pipes/me")),
                    new Material(InventoryMenu.BLOCK_ATLAS, MI.id("block/pipes/me_blocks")));
        }

        @Override
        public PipeRenderer create(Function<Material, TextureAtlasSprite> textureGetter) {
            return new PipeMeshCache(textureGetter, new Material[] {
                    new Material(InventoryMenu.BLOCK_ATLAS, MI.id("block/pipes/me")),
                    new Material(InventoryMenu.BLOCK_ATLAS, MI.id("block/pipes/me_blocks"))
            }, false);
        }
    };

    public static void registerPipeRenderers() {
        for (var type : PipeNetworkType.getTypes().values()) {
            if (MIAEAddon.PIPES.contains(type)) {
                PipeRenderer.register(type, ME_RENDERER);
            }
        }
    }
}

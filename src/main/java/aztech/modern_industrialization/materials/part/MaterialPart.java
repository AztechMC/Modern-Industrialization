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
package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.textures.TextureManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/**
 * Stores a single combination material/part Register it and generate the
 * associated texture
 **/
public interface MaterialPart extends ItemLike {
    /**
     * @return The name of this part, for example "ingot" or "dust".
     */
    Part getPart();

    /**
     * @return The common tag of this material prefixed by # if available, or the id
     *         otherwise.
     */
    String getTaggedItemId();

    /**
     * @return The full id of this part. Includes the namespace and the path, separated by :.
     */
    String getItemId();

    default Item asItem() {
        return Registry.ITEM.getOrThrow(ResourceKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(getItemId())));
    }

    default void register(MaterialBuilder.RegisteringContext context) {

    }

    default void registerTextures(TextureManager textureManager) {

    }

    default void registerClient() {
    }
}

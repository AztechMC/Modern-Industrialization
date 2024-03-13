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

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.MaterialBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public sealed interface MaterialItemPart extends PartKeyProvider, ItemLike permits MaterialItemPartImpl {

    /**
     * External parts are already registered and already have a texture,
     * but they're in the material system for recipe generation.
     */
    static MaterialItemPart external(PartKey key, String taggedItemId, String itemId) {
        return new MaterialItemPartImpl(key, taggedItemId, itemId, ctx -> {
        }, new TextureGenParams.NoTexture(), false);
    }

    static MaterialItemPart external(PartKeyProvider part, String taggedItemId, String itemId) {
        return external(part.key(), taggedItemId, itemId);
    }

    /**
     * External parts are already registered and already have a texture,
     * but they're in the material system for recipe generation.
     */
    static MaterialItemPart external(PartKeyProvider part, String itemId) {
        return external(part, itemId, itemId);
    }

    /**
     * Simple item parts are just regular MIItems.
     */
    static MaterialItemPart simpleItem(PartKeyProvider part, String englishName, String itemPath) {
        String itemId = "modern_industrialization:" + itemPath;

        return new MaterialItemPartImpl(part.key(), itemId, itemId, ctx -> {
            MIItem.item(englishName, itemPath, SortOrder.MATERIALS.and(ctx.getMaterialName()));
        }, new TextureGenParams.NoTexture(), true);
    }

    /**
     * @return The common tag of this material prefixed by # if available, or the id
     *         otherwise.
     */
    String getTaggedItemId();

    default Ingredient getTaggedIngredient() {
        var taggedItem = getTaggedItemId();
        if (taggedItem.startsWith("#")) {
            return Ingredient.of(ItemTags.create(new ResourceLocation(taggedItem.substring(1))));
        } else {
            return Ingredient.of(asItem());
        }
    }

    /**
     * @return The full id of this part. Includes the namespace and the path, separated by :.
     */
    String getItemId();

    @Override
    default Item asItem() {
        return BuiltInRegistries.ITEM.getOrThrow(ResourceKey.create(Registries.ITEM, new ResourceLocation(getItemId())));
    }

    default Block asBlock() {
        return BuiltInRegistries.BLOCK.getOrThrow(ResourceKey.create(Registries.BLOCK, new ResourceLocation(getItemId())));
    }

    /**
     * Perform any required registration.
     */
    void register(MaterialBuilder.PartContext partContext);

    /**
     * Return texture generation parameters. Return {@link TextureGenParams.NoTexture} if no texture is required.
     */
    TextureGenParams getTextureGenParams();

    boolean isInternal();
}

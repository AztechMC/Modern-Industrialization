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
import java.util.function.Function;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ExternalPart implements MaterialPart {
    private final String part, tag, itemId;

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(String part, String taggedItemId, String itemId) {
        return ctx -> new ExternalPart(part, taggedItemId, itemId);
    }

    private ExternalPart(String part, String tag, String itemId) {
        this.part = part;
        this.tag = tag;
        this.itemId = itemId;
    }

    @Override
    public String getPart() {
        return part;
    }

    @Override
    public String getTaggedItemId() {
        return tag;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register() {
    }

    @Override
    public Item getItem() {
        return Registry.ITEM.get(new Identifier(itemId));
    }

    @Override
    public void registerTextures(TextureManager textureManager) {
    }
}

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
import java.util.function.Consumer;

/**
 * The unique implementation of {@link MaterialItemPart}, to keep impl details out of the interface.
 */
final class MaterialItemPartImpl implements MaterialItemPart {
    private final PartKey key;
    private final String taggedItemId;
    private final String itemId;
    private final Consumer<MaterialBuilder.PartContext> registration;
    private final TextureGenParams textureGenParams;
    private final boolean internal;

    MaterialItemPartImpl(PartKey key, String taggedItemId, String itemId,
            Consumer<MaterialBuilder.PartContext> registration, TextureGenParams textureGenParams, boolean internal) {
        this.key = key;
        this.taggedItemId = taggedItemId;
        this.itemId = itemId;
        this.registration = registration;
        this.textureGenParams = textureGenParams;
        this.internal = internal;
    }

    @Override
    public PartKey key() {
        return key;
    }

    @Override
    public String getTaggedItemId() {
        return taggedItemId;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register(MaterialBuilder.PartContext partContext) {
        registration.accept(partContext);
    }

    @Override
    public TextureGenParams getTextureGenParams() {
        return textureGenParams;
    }

    @Override
    public boolean isInternal() {
        return internal;
    }
}

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

import aztech.modern_industrialization.materials.set.MaterialBlockSet;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.materials.set.MaterialRawSet;
import org.jetbrains.annotations.Nullable;

public sealed interface TextureGenParams {
    record NoTexture() implements TextureGenParams {
    }

    record Block(MaterialBlockSet blockSet) implements TextureGenParams {
    }

    record CasingBlock() implements TextureGenParams {
    }

    record ColumnBlock() implements TextureGenParams {
    }

    record DepletedNuclear() implements TextureGenParams {
    }

    record DoubleIngot() implements TextureGenParams {
    }

    record Gem() implements TextureGenParams {
    }

    record HotIngot() implements TextureGenParams {
    }

    record Ore(boolean deepslate, MaterialOreSet oreSet) implements TextureGenParams {
    }

    record RawMetal(boolean isBlock, MaterialRawSet rawSet) implements TextureGenParams {
    }

    record SimpleRecoloredBlock() implements TextureGenParams {
    }

    record SimpleRecoloredItem(@Nullable PartKey basePart, @Nullable String overlay) implements TextureGenParams {
        public SimpleRecoloredItem() {
            this(null, null);
        }
    }
}

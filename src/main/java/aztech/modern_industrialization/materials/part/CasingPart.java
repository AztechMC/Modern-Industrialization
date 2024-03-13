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

import aztech.modern_industrialization.items.SortOrder;
import org.jetbrains.annotations.Nullable;

public class CasingPart implements PartKeyProvider {

    public final String englishName;
    public final PartKey key;

    public CasingPart(String englishName, String key) {
        this.englishName = englishName;
        this.key = new PartKey(key);
    }

    @Override
    public PartKey key() {
        return key;
    }

    public PartTemplate of(PartEnglishNameFormatter formatter, @Nullable String path, float resistance) {
        PartTemplate regPart = new PartTemplate(formatter, this.key)
                .asBlock(SortOrder.CASINGS, new TextureGenParams.CasingBlock(), 5, resistance, 1, false);
        if (path != null) {
            return regPart.withCustomPath(path, path);
        }
        return regPart;
    }

    public PartTemplate of(String englishName, String path, float resistance) {
        return of(new PartEnglishNameFormatter.Overridden(englishName), path, resistance);
    }

    public PartTemplate of(String englishName, String path) {
        return of(new PartEnglishNameFormatter.Overridden(englishName), path, 6f);
    }

    public PartTemplate of(float resistance) {
        return of(new PartEnglishNameFormatter.Default(englishName), null, resistance);
    }

    public PartTemplate of() {
        return of(6);
    }
}

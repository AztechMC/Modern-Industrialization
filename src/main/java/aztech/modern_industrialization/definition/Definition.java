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

package aztech.modern_industrialization.definition;

import aztech.modern_industrialization.MI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public abstract class Definition {
    public static List<Definition> TRANSLATABLE_DEFINITION = new ArrayList<>();

    private final ResourceLocation id;
    private final String englishName;

    public String getEnglishName() {
        return englishName;
    }

    public ResourceLocation getId() {
        return id;
    }

    public abstract String getTranslationKey();

    public Definition(String englishName, String path, boolean addToTranslation) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(englishName, "englishName");
        this.id = MI.id(path);
        this.englishName = englishName;
        if (addToTranslation) {
            TRANSLATABLE_DEFINITION.add(this);
        }
    }

    public Definition(String englishName, String path) {
        this(englishName, path, true);
    }

    public String getResourceAsString(boolean id) {
        if (id) {
            return this.id.toString();
        } else {
            return this.id.getPath();
        }
    }

    public String path() {
        return getResourceAsString(false);
    }

    public String id() {
        return getResourceAsString(true);
    }
}

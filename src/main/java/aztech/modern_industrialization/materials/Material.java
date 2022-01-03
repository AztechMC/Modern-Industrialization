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
package aztech.modern_industrialization.materials;

import aztech.modern_industrialization.materials.part.MaterialPart;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.data.recipes.FinishedRecipe;

/**
 * A read-only material. Build with {@link MaterialBuilder}.
 */
public class Material {
    public final String name;
    final Map<String, MaterialPart> parts;

    public final Consumer<Consumer<FinishedRecipe>> registerRecipes;

    Material(String name, Map<String, MaterialPart> parts, Consumer<Consumer<FinishedRecipe>> registerRecipes) {
        this.name = name;
        this.parts = parts;
        this.registerRecipes = registerRecipes;
    }

    public Map<String, MaterialPart> getParts() {
        return Collections.unmodifiableMap(parts);
    }
}

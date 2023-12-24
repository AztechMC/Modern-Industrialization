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

import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class MaterialRegistry {
    static final Map<String, Material> MATERIALS = new TreeMap<>();

    public static Material addMaterial(MaterialBuilder materialBuilder) {
        KubeJSProxy.instance.fireModifyMaterialEvent(materialBuilder);

        Material material = materialBuilder.build();
        if (MATERIALS.put(material.name, material) != null) {
            throw new IllegalStateException("Duplicate registration of material " + material.name);
        }
        return material;
    }

    public static Material getMaterial(String name) {
        Material material = MATERIALS.get(name);
        if (material == null) {
            throw new IllegalArgumentException("No such material: " + name);
        }
        return material;
    }

    public static Map<String, Material> getMaterials() {
        return Collections.unmodifiableMap(MATERIALS);
    }
}

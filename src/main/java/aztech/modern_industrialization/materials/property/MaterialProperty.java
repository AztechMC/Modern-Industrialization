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

package aztech.modern_industrialization.materials.property;

import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.materials.part.PartKeyProvider;
import aztech.modern_industrialization.materials.set.MaterialSet;
import aztech.modern_industrialization.nuclear.IsotopeFuelParams;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Some extra property for a material
 */
public class MaterialProperty<T> {
    private static final Map<String, MaterialProperty<?>> PROPERTY_IDS = new HashMap<>();
    public static final Collection<MaterialProperty<?>> PROPERTIES = Collections.unmodifiableCollection(PROPERTY_IDS.values());

    public final T defaultValue;

    public MaterialProperty(String id, T defaultValue) {
        this.defaultValue = defaultValue;

        if (PROPERTY_IDS.put(id, this) != null) {
            throw new IllegalArgumentException("Duplicate material property id: " + id);
        }
    }

    /**
     * Defines the color to use for the material.
     */
    public static final MaterialProperty<Integer> MEAN_RGB = new MaterialProperty<>("mean_rgb", 0);
    /**
     * The hardness influences the speed of some recipes.
     */
    public static final MaterialProperty<MaterialHardness> HARDNESS = new MaterialProperty<>("hardness", MaterialHardness.AVERAGE);
    /**
     * Defines the "main" part, i.e. the default form of the material. (ingot for metals, gem for gems, etc.)
     */
    public static final MaterialProperty<PartKeyProvider> MAIN_PART = new MaterialProperty<>("main_part", MIParts.INGOT);
    /**
     * Which template set to use for the textures.
     */
    public static final MaterialProperty<MaterialSet> SET = new MaterialProperty<>("material_set", MaterialSet.DULL);
    /**
     * Isotope parameters for nuclear fission fuels.
     */
    public static final MaterialProperty<@Nullable IsotopeFuelParams> ISOTOPE = new MaterialProperty<>("isotope", null);
}

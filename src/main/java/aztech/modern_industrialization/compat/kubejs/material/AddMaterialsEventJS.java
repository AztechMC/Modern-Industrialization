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
package aztech.modern_industrialization.compat.kubejs.material;

import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.PartTemplate;
import aztech.modern_industrialization.materials.property.ColorampParameters;
import aztech.modern_industrialization.materials.property.MaterialProperty;
import aztech.modern_industrialization.materials.recipe.SmeltingRecipes;
import aztech.modern_industrialization.materials.recipe.StandardRecipes;
import dev.latvian.mods.kubejs.event.EventJS;
import java.util.ArrayList;
import java.util.List;

public class AddMaterialsEventJS extends EventJS implements PartJsonCreator {

    public MaterialBuilder materialBuilder(String englishName, String material_name, int color) {
        return new MaterialBuilder(englishName, material_name).set(MaterialProperty.COLORAMP, new ColorampParameters.Uniform(color));
    }

    public void trivialRegister(MaterialBuilder materialBuilder) {
        MaterialRegistry.addMaterial(materialBuilder.addRecipes(StandardRecipes::apply).addRecipes(SmeltingRecipes::apply));
    }

    public MaterialBuilder trivialMaterial(String englishName, String material_name, int color, List<String> regularPartKey) {

        List<PartTemplate> materialParts = new ArrayList<>();

        for (String part : regularPartKey) {
            materialParts.add(regularPart(part));
        }

        return materialBuilder(englishName, material_name, color).addParts(materialParts);

    }

}

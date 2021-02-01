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
import aztech.modern_industrialization.materials.part.RegularMaterialPart;
import aztech.modern_industrialization.materials.recipe.builder.MaterialRecipeBuilder;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public final class MaterialBuilder {
    private final Map<String, MaterialPart> partsMap = new TreeMap<>();
    private final Map<String, MaterialRecipeBuilder> recipesMap = new HashMap<>();
    private final PartContext partContext = new PartContext();
    private final RecipeContext recipeContext = new RecipeContext();
    private final String materialName;
    private final String materialSet;
    private final int color;

    public MaterialBuilder(String materialName, MaterialSet materialSet, int color) {
        this.materialName = materialName;
        this.materialSet = materialSet.name;
        this.color = color;
    }

    public MaterialBuilder addRegularParts(String... parts) {
        for (String part : parts) {
            addPart(new RegularMaterialPart(materialName, part, materialSet, color));
        }
        return this;
    }

    @SafeVarargs
    public final MaterialBuilder addParts(Function<PartContext, MaterialPart>... partFunctions) {
        for (Function<PartContext, MaterialPart> partFunction : partFunctions) {
            addPart(partFunction.apply(partContext));
        }
        return this;
    }

    private void addPart(MaterialPart part) {
        if (partsMap.put(part.getPart(), part) != null) {
            throw new IllegalStateException("Part " + part.getItemId() + " is already registered for this material!");
        }
    }

    public MaterialBuilder overridePart(Function<PartContext, MaterialPart> partFunction) {
        MaterialPart part = partFunction.apply(partContext);
        if (partsMap.put(part.getPart(), part) == null) {
            throw new IllegalStateException("Part " + part.getItemId() + " was not already registered for this material!");
        }
        return this;
    }

    @SafeVarargs
    public final MaterialBuilder addRecipes(Consumer<RecipeContext>... consumers) {
        for (Consumer<RecipeContext> consumer : consumers) {
            consumer.accept(recipeContext);
        }
        return this;
    }

    public MaterialBuilder cancelRecipes(String... recipeIds) {
        for (String recipeId : recipeIds) {
            if (recipesMap.remove(recipeId) == null) {
                throw new IllegalArgumentException("Recipe does not exist and cannot be cancelled: " + recipeId);
            }
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    public Material build() {
        for (MaterialRecipeBuilder builder : recipesMap.values()) {
            builder.save();
        }
        for (MaterialPart part : partsMap.values()) {
            part.register();
        }
        return new Material(materialName, Collections.unmodifiableMap(partsMap));
    }

    public class PartContext {
        public int getColor() {
            return color;
        }

        public String getMaterialName() {
            return materialName;
        }
    }

    public class RecipeContext {
        public void addRecipe(MaterialRecipeBuilder builder) {
            if (recipesMap.put(builder.getRecipeId(), builder) != null) {
                throw new IllegalStateException("Duplicate registration of recipe " + builder.getRecipeId());
            }
        }

        public @Nullable MaterialPart getPart(String part) {
            return partsMap.get(part);
        }

        public String getMaterialName() {
            return materialName;
        }
    }
}

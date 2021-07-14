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

import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.materials.part.MaterialPart;
import aztech.modern_industrialization.materials.part.RegularMaterialPart;
import aztech.modern_industrialization.materials.recipe.builder.MaterialRecipeBuilder;
import aztech.modern_industrialization.materials.set.MaterialSet;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import aztech.modern_industrialization.textures.coloramp.DefaultColoramp;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class MaterialBuilder {

    private final Map<String, MaterialPart> partsMap = new TreeMap<>();
    private final PartContext partContext = new PartContext();
    private final String materialName;
    private final String materialSet;
    private final Coloramp coloramp;
    private final String mainPart;

    private final Queue<RecipeAction> recipesActions = new LinkedList<>();

    public MaterialBuilder(String materialName, MaterialSet materialSet, String mainPart, Coloramp coloramp) {
        this.materialName = materialName;
        this.materialSet = materialSet.name;
        this.coloramp = coloramp;
        this.mainPart = mainPart;
    }

    public MaterialBuilder(String materialName, MaterialSet materialSet, String mainPart, int color) {
        this(materialName, materialSet, mainPart, new DefaultColoramp(color));
    }

    public MaterialBuilder(String materialName, MaterialSet materialSet, Coloramp coloramp) {
        this(materialName, materialSet, MIParts.INGOT, coloramp);
    }

    public MaterialBuilder(String materialName, MaterialSet materialSet, int color) {
        this(materialName, materialSet, MIParts.INGOT, new DefaultColoramp(color));
    }

    public String getMaterialName() {
        return materialName;
    }

    public MaterialBuilder addRegularParts(String... parts) {
        for (String part : parts) {
            addPart(new RegularMaterialPart(materialName, part, materialSet, coloramp));
        }
        return this;
    }

    public MaterialBuilder removeRegularParts(String... parts) {
        for (String part : parts) {
            removePart(part);
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

    private void removePart(String part) {

        partsMap.remove(part);
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
        recipesActions.add(new RecipeAction() {
            @Override
            void apply(RecipeContext recipeContext) {
                for (Consumer<RecipeContext> consumer : consumers) {
                    consumer.accept(recipeContext);
                }
            }
        });
        return this;
    }

    public MaterialBuilder cancelRecipes(String... recipeIds) {
        recipesActions.add(new RecipeAction() {
            @Override
            void apply(RecipeContext context) {
                for (String recipeId : recipeIds) {
                    context.removeRecipe(recipeId);
                }
            }
        });
        return this;
    }

    public Material build() {

        RegisteringContext context = new RegisteringContext();

        for (MaterialPart part : partsMap.values()) {
            part.register(context);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                part.registerClient();
            }
        }
        return new Material(materialName, Collections.unmodifiableMap(partsMap), this::buildRecipes);
    }

    public void buildRecipes() {
        Map<String, MaterialRecipeBuilder> recipesMap = new HashMap<>();
        RecipeContext recipeContext = new RecipeContext(recipesMap);
        for (RecipeAction action : recipesActions) {
            action.apply(recipeContext);
        }
        for (MaterialRecipeBuilder builder : recipesMap.values()) {
            builder.save();
        }
    }

    public class PartContext {
        public Coloramp getColoramp() {
            return coloramp;
        }

        public String getMaterialName() {
            return materialName;
        }

        public String getMaterialSet() {
            return materialSet;
        }

        public String getMainPart() {
            return mainPart;
        }
    }

    public class RegisteringContext {

        public MaterialPart getMaterialPart(String part) {
            return partsMap.get(part);
        }
    }

    public class RecipeContext {
        private final Map<String, MaterialRecipeBuilder> recipesMap;

        public RecipeContext(Map<String, MaterialRecipeBuilder> recipesMap) {
            this.recipesMap = recipesMap;
        }

        public void addRecipe(MaterialRecipeBuilder builder) {
            if (recipesMap.containsKey(builder.getRecipeId())) {
                if (recipesMap.get(builder.getRecipeId()).isCanceled()) {
                    recipesMap.remove(builder.getRecipeId());
                } else {
                    throw new IllegalStateException(
                            "Duplicate registration of recipe " + builder.getRecipeId() + " for Material : " + getMaterialName());
                }
            }
            recipesMap.put(builder.getRecipeId(), builder);
        }

        public void removeRecipe(String recipeId) {
            if (recipesMap.remove(recipeId) == null) {
                throw new IllegalArgumentException("Recipe does not exist and cannot be cancelled: " + recipeId + " for Material : " + materialName);
            }
        }

        public MaterialPart getPart(String part) {
            return partsMap.get(part);
        }

        public String getMaterialName() {
            return materialName;
        }

        public String getMainPart() {
            return mainPart;
        }
    }

    public abstract class RecipeAction {

        abstract void apply(RecipeContext context);

    }
}

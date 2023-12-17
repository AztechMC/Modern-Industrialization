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
package aztech.modern_industrialization.datagen.recipe;

import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.apache.commons.io.IOUtils;

public class AssemblerRecipesProvider extends MIRecipesProvider {
    private static final Gson GSON = new Gson();

    private final FabricDataOutput packOutput;

    public AssemblerRecipesProvider(FabricDataOutput packOutput) {
        super(packOutput);
        this.packOutput = packOutput;
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {
        var nonGeneratedResources = packOutput.getOutputFolder().resolve("../../main/resources");
        try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(new PathPackResources("ngr", nonGeneratedResources, true)))) {
            var possibleTargets = manager.listResources("recipes", path -> path.getPath().endsWith(".json"));
            for (var entry : possibleTargets.entrySet()) {
                var pathId = entry.getKey();
                if (shouldConvertToAssembler(pathId)) {
                    try (var stream = entry.getValue().open()) {
                        convertToAssembler(consumer, pathId, IOUtils.toByteArray(stream));
                    } catch (Exception exception) {
                        throw new RuntimeException("Failed to convert asbl recipe %s. Error: %s".formatted(pathId, exception), exception);
                    }
                }
            }
        }
    }

    public static boolean shouldConvertToAssembler(ResourceLocation pathId) {
        if (pathId.getNamespace().equals("modern_industrialization")) {
            String path = pathId.toString();
            String postfix = path.substring(path.length() - 10, path.length() - 5);
            if (postfix.equals("_asbl")) {
                return true;
            } else if (postfix.contains("_") && postfix.contains("a") && postfix.contains("s") && postfix.contains("b") && postfix.contains("l")) {
                throw new RuntimeException("Detected potential typo in _asbl.json. Crashing just to be safe. Recipe path: " + path);
            }
        }
        return false;
    }

    public static void convertToAssembler(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId, byte[] recipe) {
        String recipeString = new String(recipe, StandardCharsets.UTF_8);
        ShapedRecipeJson json = GSON.fromJson(recipeString, ShapedRecipeJson.class);
        if (json.result.count == 0) {
            json.result.count = 1;
        }
        MIRecipeJson<?> assemblerJson = json.exportToAssembler();
        String outputSuffix = recipeId.getPath().substring("recipes/".length(), recipeId.getPath().length() - "_asbl.json".length());
        assemblerJson.offerTo(consumer, "assembler_generated/" + outputSuffix);
    }
}

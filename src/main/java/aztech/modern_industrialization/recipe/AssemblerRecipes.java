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
package aztech.modern_industrialization.recipe;

import aztech.modern_industrialization.MIRuntimeResourcePack;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import aztech.modern_industrialization.util.ResourceUtil;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class AssemblerRecipes {
    private static final Gson GSON = new Gson();

    public static void yes(MIRuntimeResourcePack pack, ResourceManager manager) {
        Collection<Identifier> possibleTargets = manager.findResources("recipes", path -> path.endsWith(".json"));
        for (Identifier pathId : possibleTargets) {
            if (shouldConvertToAssembler(pathId)) {
                try {
                    convertToAssembler(pack, pathId, ResourceUtil.getBytes(manager.getResource(pathId)));
                } catch (Exception exception) {
                    ModernIndustrialization.LOGGER.warn("Failed to convert asbl recipe {}. Error: {}", pathId, exception);
                }
            }
        }
    }

    public static boolean shouldConvertToAssembler(Identifier pathId) {
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

    public static void convertToAssembler(MIRuntimeResourcePack pack, Identifier recipeId, byte[] recipe) {
        convertToAssembler(pack, recipeId, recipe, false);
    }

    public static void convertToAssembler(MIRuntimeResourcePack pack, Identifier recipeId, byte[] recipe, boolean override) {
        String recipeString = new String(recipe, StandardCharsets.UTF_8);
        ShapedRecipeJson json = GSON.fromJson(recipeString, ShapedRecipeJson.class);
        if (json.result.count == 0) {
            json.result.count = 1;
        }
        MIRecipeJson assemblerJson = json.exportToMachine("assembler", 8, 200, 1);
        String outputSuffix = recipeId.getPath().substring("recipes/".length());
        pack.addData("modern_industrialization/recipes/generated/assembler/" + outputSuffix, GSON.toJson(assemblerJson).getBytes(), override);
    }

    private AssemblerRecipes() {
    }
}

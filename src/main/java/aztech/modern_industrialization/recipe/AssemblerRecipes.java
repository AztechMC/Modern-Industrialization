package aztech.modern_industrialization.recipe;

import aztech.modern_industrialization.MIRuntimeResourcePack;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import aztech.modern_industrialization.util.ResourceUtil;
import com.google.gson.Gson;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

public final class AssemblerRecipes {
    private static final Gson GSON = new Gson();

    public static void yes(MIRuntimeResourcePack pack, ResourceManager manager) {
        Collection<Identifier> possibleTargets = manager.findResources("recipes", path -> path.endsWith(".json"));
        for (Identifier pathId : possibleTargets) {
            if (pathId.getNamespace().equals("modern_industrialization")) {
                String path = pathId.toString();
                String postfix = path.substring(path.length() - 10, path.length() - 5);
                if (postfix.equals("_asbl")) {
                    try {
                        convertToAssembler(pack, pathId, ResourceUtil.getBytes(manager.getResource(pathId)));
                    } catch (Exception exception) {
                        ModernIndustrialization.LOGGER.warn("Failed to convert asbl recipe {}. Error: {}", pathId, exception);
                    }
                } else if (postfix.contains("_") && postfix.contains("a") && postfix.contains("s") && postfix.contains("b") && postfix.contains("l")) {
                    throw new RuntimeException("Detected potential typo in _asbl.json. Crashing just to be safe. Recipe path: " + path);
                }
            }
        }
    }

    private static void convertToAssembler(MIRuntimeResourcePack pack, Identifier recipeId, byte[] recipe) {
        String recipeString = new String(recipe, StandardCharsets.UTF_8);
        ShapedRecipeJson json = GSON.fromJson(recipeString, ShapedRecipeJson.class);
        if (json.result.count == 0) {
            json.result.count = 1;
        }
        MIRecipeJson assemblerJson = json.exportToMachine("assembler", 8, 200, 1);
        String outputSuffix = recipeId.getPath().substring("recipes/".length());
        pack.addData("modern_industrialization/recipes/generated/assembler/" + outputSuffix, GSON.toJson(assemblerJson).getBytes());
    }

    private AssemblerRecipes() {
    }
}

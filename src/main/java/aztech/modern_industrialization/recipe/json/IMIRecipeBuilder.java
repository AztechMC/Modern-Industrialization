package aztech.modern_industrialization.recipe.json;

import net.minecraft.data.recipes.RecipeOutput;

public interface IMIRecipeBuilder {
    void offerTo(RecipeOutput recipeOutput, String path);
}

package aztech.modern_industrialization.materials.recipe.builder;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerRecipe;
import aztech.modern_industrialization.materials.MaterialBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ForgeHammerRecipeBuilder implements MaterialRecipeBuilder {
    private final String recipeId;
    private final MaterialBuilder.RecipeContext context;
    private boolean canceled = false;
    private final ForgeHammerRecipe recipe;

    public ForgeHammerRecipeBuilder(MaterialBuilder.RecipeContext context, String id, Ingredient input, int inputCount, ItemStack output, int hammerDamage) {
        this.recipeId = "forge_hammer/" + id;
        this.context = context;
        this.recipe = new ForgeHammerRecipe(input, inputCount, output, hammerDamage);
        context.addRecipe(this);
    }

    @Override
    public String getRecipeId() {
        return recipeId;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        if (!canceled) {
            String fullId = "materials/" + context.getMaterialName() + "/" + recipeId;
            recipeOutput.accept(MI.id(fullId), recipe, null);
        }
    }
}

package aztech.modern_industrialization.materials.recipe.builder;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.part.PartKeyProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class ShapelessRecipeBuilder implements MaterialRecipeBuilder {
    public final String recipeId;
    private final MaterialBuilder.RecipeContext context;
    private boolean canceled = false;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();

    public ShapelessRecipeBuilder(MaterialBuilder.RecipeContext context, PartKeyProvider result, int count, String id) {
        this.recipeId = "craft/" + id;
        this.context = context;
        var output = context.getPart(result);
        if (output == null) {
            this.result = null;
            canceled = true;
        } else {
            this.result = new ItemStack(output.asItem(), count);
        }
        context.addRecipe(this);
    }

    public ShapelessRecipeBuilder addPart(PartKeyProvider part) {
        if (context.getPart(part) != null) {
            ingredients.add(Ingredient.of(context.getPart(part).asItem()));
        } else {
            canceled = true;
        }
        return this;
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
            recipeOutput.accept(
                    MI.id(fullId),
                    new ShapelessRecipe(
                            "",
                            CraftingBookCategory.MISC,
                            result,
                            ingredients),
                    null);
        }
    }
}

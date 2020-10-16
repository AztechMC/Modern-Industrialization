package aztech.modern_industrialization.compat.rei.forgehammer_recipe;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.function.Predicate;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

public class ForgeHammerRecipePlugin implements REIPluginV0 {
    private MachineRecipeType[] RECIPE_TYPES = new MachineRecipeType[] { ForgeHammerScreenHandler.RECIPE_HAMMER,
            ForgeHammerScreenHandler.RECIPE_SAW };

    @Override
    public Identifier getPluginIdentifier() {
        return new MIIdentifier("forge_hammer_recipe");
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        for (MachineRecipeType type : RECIPE_TYPES) {
            recipeHelper.registerCategory(new ForgeHammerRecipeCategory(type, type == RECIPE_TYPES[0]));
        }
    }

    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        for (MachineRecipeType type : RECIPE_TYPES) {
            recipeHelper.registerRecipes(type.getId(),
                    (Predicate<Recipe>) recipe -> recipe instanceof MachineRecipe && ((MachineRecipe) recipe).getType() == type,
                    recipe -> new ForgeHammerRecipeDisplay(type, (MachineRecipe) recipe));
        }
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        for (MachineRecipeType type : RECIPE_TYPES) {
            recipeHelper.registerWorkingStations(type.getId(), EntryStack.create(ModernIndustrialization.ITEM_FORGE_HAMMER));
            recipeHelper.removeAutoCraftButton(type.getId());
        }

        recipeHelper.registerContainerClickArea(new Rectangle(71, 48, 53, 15), ForgeHammerScreen.class, RECIPE_TYPES[0].getId(),
                RECIPE_TYPES[1].getId());
    }
}

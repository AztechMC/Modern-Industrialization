package aztech.modern_industrialization.machines.recipe;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.recipe.json.IMIRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;

public class MachineRecipeBuilder extends MIRecipeJson<MachineRecipeBuilder> implements IMIRecipeBuilder {
    public MachineRecipeBuilder(MachineRecipeType machineRecipeType, int eu, int duration) {
        super(machineRecipeType, eu, duration);
    }

    public MachineRecipeBuilder(MIRecipeJson<?> otherWithSameData) {
        super(otherWithSameData);
    }

    @Override
    public void offerTo(RecipeOutput recipeOutput, String path) {
        recipeOutput.accept(MI.id(path), recipe, null);
    }
}

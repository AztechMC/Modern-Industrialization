package aztech.modern_industrialization.machines.recipe;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public class FillingMachineRecipeType extends ProxyableMachineRecipeType {
    public FillingMachineRecipeType(ResourceLocation id) {
        super(id);
    }

    protected void fillRecipeList(Level world) {
        for (Fluid fluid : Registry.FLUID) {
            MachineRecipe recipe = RecipeConversions.getFillingMachineRecipe(fluid, this, false);
            if (recipe != null) {
                recipeList.add(recipe);
            }
        }
    }
}

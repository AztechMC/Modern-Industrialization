package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.machines.recipe.MachineRecipeType;

@FunctionalInterface
public interface BlockEntityFactory {
    MachineBlockEntity create(MachineFactory factory, MachineRecipeType recipeType);
}

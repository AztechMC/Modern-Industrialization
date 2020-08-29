package aztech.modern_industrialization.rei.forgehammer_recipe;

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ForgeHammerRecipeDisplay implements RecipeDisplay {
    private final MachineRecipe recipe;
    private final Identifier category;

    public ForgeHammerRecipeDisplay(MachineRecipeType type, MachineRecipe recipe) {
        this.recipe = recipe;
        this.category = type.getId();
    }

    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        MachineRecipe.ItemInput input = recipe.itemInputs.get(0);
        return Collections.singletonList(Collections.singletonList(EntryStack.create(new ItemStack(input.item, input.amount))));
    }

    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        MachineRecipe.ItemOutput output = recipe.itemOutputs.get(0);
        return Collections.singletonList(Collections.singletonList(EntryStack.create(new ItemStack(output.item, output.amount))));
    }

    @Override
    public @NotNull Identifier getRecipeCategory() {
        return category;
    }
}

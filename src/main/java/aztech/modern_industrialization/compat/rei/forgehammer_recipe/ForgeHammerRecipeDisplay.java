package aztech.modern_industrialization.compat.rei.forgehammer_recipe;

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

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
        return Collections.singletonList(createInputEntries(input));
    }

    private static List<EntryStack> createInputEntries(MachineRecipe.ItemInput input) {
        return input.item == null
                ? input.tag.values().stream().map(i -> EntryStack.create(new ItemStack(i, input.amount))).collect(Collectors.toList())
                : Collections.singletonList(EntryStack.create(new ItemStack(input.item, input.amount)));
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

package aztech.modern_industrialization.datagen.recipe;

import aztech.modern_industrialization.machines.recipe.MIRecipeJson;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jspecify.annotations.Nullable;

public class AssemblerExportRecipeOutput implements RecipeOutput {
    private final RecipeOutput wrapped;

    public AssemblerExportRecipeOutput(RecipeOutput wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Advancement.Builder advancement() {
        return wrapped.advancement();
    }

    @Override
    public void includeRootAdvancement() {
        wrapped.includeRootAdvancement();
    }

    @Override
    public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        if (!(recipe instanceof ShapedRecipe shapedRecipe)) {
            throw new IllegalArgumentException("Can only export shaped recipes to the assembler, received: " + recipe);
        }
        wrapped.accept(key, recipe, advancement, conditions);

        var assemblerKey = ResourceKey.create(Registries.RECIPE, key.identifier().withPrefix("assembler_generated/"));
        new MachineRecipeBuilder(MIRecipeJson.assemblerFromShaped(shapedRecipe))
                .save(wrapped, assemblerKey);
    }
}

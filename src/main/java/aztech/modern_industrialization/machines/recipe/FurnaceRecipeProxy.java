package aztech.modern_industrialization.machines.recipe;

import aztech.modern_industrialization.mixin_impl.IngredientMatchingStacksAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FurnaceRecipeProxy extends MachineRecipeType {
    public FurnaceRecipeProxy(Identifier id) {
        super(id);
    }

    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 20 * 1000;

    private Map<Identifier, MachineRecipe> cachedRecipes = new HashMap<>();

    private void buildCachedRecipes(ServerWorld world) {
        cachedRecipes.clear();
        for(SmeltingRecipe smeltingRecipe : world.getRecipeManager().listAllOfType(RecipeType.SMELTING)) {
            Ingredient ingredient = smeltingRecipe.getPreviewInputs().get(0);
            ItemStack[] matchingStacks = ((IngredientMatchingStacksAccessor)(Object) ingredient).modern_industrialization_getMatchingStacks();
            for(ItemStack matchingStack : matchingStacks) {
                Identifier id = new Identifier(smeltingRecipe.getId().getNamespace(), smeltingRecipe.getId().getPath() + Registry.ITEM.getId(matchingStack.getItem()).toString().replace(":", "__modern_industrialization_furnace_proxy__"));
                MachineRecipe recipe = new MachineRecipe(id, this);
                recipe.eu = 2;
                recipe.duration = smeltingRecipe.getCookTime();
                recipe.itemInputs = Collections.singletonList(new MachineRecipe.ItemInput(matchingStack.getItem(), matchingStack.getCount(), 1));
                recipe.fluidInputs = Collections.emptyList();
                recipe.itemOutputs = Collections.singletonList(new MachineRecipe.ItemOutput(smeltingRecipe.getOutput().getItem(), 1, 1));
                recipe.fluidOutputs = Collections.emptyList();
                cachedRecipes.put(id, recipe);
            }
        }
    }

    @Override
    public Collection<MachineRecipe> getRecipes(ServerWorld world) {
        long time = System.currentTimeMillis();
        if(time - lastUpdate > UPDATE_INTERVAL) {
            lastUpdate = time;
            buildCachedRecipes(world);
        }
        return cachedRecipes.values();
    }
}

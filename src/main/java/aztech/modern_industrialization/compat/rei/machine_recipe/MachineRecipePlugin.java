package aztech.modern_industrialization.compat.rei.machine_recipe;

import static me.shedaniel.rei.api.BuiltinPlugin.SMELTING;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.MIMachines;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineScreen;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.fluid.Fluid;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public class MachineRecipePlugin implements REIPluginV0 {
    @Override
    public Identifier getPluginIdentifier() {
        return new MIIdentifier("machine_recipe");
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        for (Map.Entry<MachineRecipeType, MIMachines.RecipeInfo> entry : MIMachines.RECIPE_TYPES.entrySet()) {
            List<MachineFactory> factories = entry.getValue().factories;
            recipeHelper.registerCategory(
                    new MachineRecipeCategory(entry.getKey(), factories.get(factories.size() - 1), EntryStack.create(factories.get(0).item)));
        }
    }

    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        for (MachineRecipeType type : MIMachines.RECIPE_TYPES.keySet()) {
            recipeHelper.registerRecipes(type.getId(),
                    (Predicate<Recipe>) recipe -> recipe instanceof MachineRecipe && ((MachineRecipe) recipe).getType() == type,
                    recipe -> new MachineRecipeDisplay(type, (MachineRecipe) recipe));
        }
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        for (Map.Entry<MachineRecipeType, MIMachines.RecipeInfo> entry : MIMachines.RECIPE_TYPES.entrySet()) {
            recipeHelper.registerWorkingStations(entry.getKey().getId(),
                    entry.getValue().factories.stream().map(f -> EntryStack.create(f.item)).toArray(EntryStack[]::new));
            MachineFactory factory = entry.getValue().factories.get(entry.getValue().factories.size() - 1);
            recipeHelper.registerContainerClickArea(screen -> {
                if (screen.getScreenHandler().getMachineFactory().recipeType == factory.recipeType) {
                    return new Rectangle(factory.getProgressBarDrawX(), factory.getProgressBarDrawY(), factory.getProgressBarSizeX(),
                            factory.getProgressBarSizeY());
                } else {
                    return new Rectangle(-1, -1, 0, 0);
                }
            }, MachineScreen.class, entry.getKey().getId());
        }

        for (MachineFactory factory : MIMachines.WORKSTATIONS_FURNACES) {
            recipeHelper.registerWorkingStations(SMELTING, EntryStack.create(factory.item));
        }

        recipeHelper.registerAutoCraftingHandler(new OutputLockTransferHandler());

        recipeHelper.registerFocusedStackProvider(screen -> {
            if (screen instanceof MachineScreen) {
                MachineScreen machineScreen = (MachineScreen) screen;
                Slot slot = machineScreen.getFocusedSlot();
                if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                    ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
                    if (stack.getAmount() > 0) {
                        Fluid fluid = stack.getFluid().getRawFluid();
                        if (fluid != null) {
                            return TypedActionResult.success(EntryStack.create(fluid));
                        }
                    } else if (stack.getLockedFluid() != null) {
                        Fluid fluid = stack.getLockedFluid().getRawFluid();
                        if (fluid != null) {
                            return TypedActionResult.success(EntryStack.create(fluid));
                        }
                    }
                } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                    ConfigurableItemStack stack = ((ConfigurableItemStack.ConfigurableItemSlot) slot).getConfStack();
                    // the normal stack is already handled by REI, we just need to handle the locked
                    // item!
                    if (stack.getLockedItem() != null) {
                        return TypedActionResult.success(EntryStack.create(stack.getLockedItem()));
                    }
                }
            }
            return TypedActionResult.pass(EntryStack.empty());
        });
    }
}

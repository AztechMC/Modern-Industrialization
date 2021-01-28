/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.compat.rei.machine_recipe;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.MIMachines;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineScreen;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.recipe.RecipeConversions;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.fluid.Fluid;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
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
        // regular categories
        for (Map.Entry<MachineRecipeType, MIMachines.RecipeInfo> entry : MIMachines.RECIPE_TYPES.entrySet()) {
            List<MachineFactory> factories = entry.getValue().factories;
            recipeHelper.registerCategory(
                    new MachineRecipeCategory(entry.getKey(), factories.get(factories.size() - 1), EntryStack.create(factories.get(0).item)));
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        // regular recipes
        for (MachineRecipeType type : MIMachines.RECIPE_TYPES.keySet()) {
            recipeHelper.registerRecipes(type.getId(),
                    (Predicate<Recipe>) recipe -> recipe instanceof MachineRecipe && ((MachineRecipe) recipe).getType() == type,
                    recipe -> new MachineRecipeDisplay(type.getId(), (MachineRecipe) recipe));
        }
        // furnace recipes
        recipeHelper.registerRecipes(MIMachines.RECIPE_FURNACE.getId(), (Predicate<Recipe>) recipe -> recipe.getType() == RecipeType.SMELTING,
                recipe -> new MachineRecipeDisplay(MIMachines.RECIPE_FURNACE.getId(),
                        RecipeConversions.of((SmeltingRecipe) recipe, MIMachines.RECIPE_FURNACE)));
        // cutting machine recipes
        recipeHelper.registerRecipes(MIMachines.RECIPE_CUTTING_MACHINE.getId(),
                (Predicate<Recipe>) recipe -> recipe.getType() == RecipeType.STONECUTTING,
                recipe -> new MachineRecipeDisplay(MIMachines.RECIPE_CUTTING_MACHINE.getId(),
                        RecipeConversions.of((StonecuttingRecipe) recipe, MIMachines.RECIPE_CUTTING_MACHINE)));
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

        recipeHelper.registerAutoCraftingHandler(new OutputLockTransferHandler());

        recipeHelper.registerFocusedStackProvider(screen -> {
            if (screen instanceof MachineScreen) {
                MachineScreen machineScreen = (MachineScreen) screen;
                Slot slot = machineScreen.getFocusedSlot();
                if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                    ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
                    if (stack.getAmount() > 0) {
                        Fluid fluid = stack.getFluid();
                        if (fluid != null) {
                            return TypedActionResult.success(EntryStack.create(fluid));
                        }
                    } else if (stack.getLockedFluid() != null) {
                        Fluid fluid = stack.getLockedFluid();
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

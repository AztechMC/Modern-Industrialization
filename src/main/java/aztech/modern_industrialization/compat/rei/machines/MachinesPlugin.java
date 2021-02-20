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
package aztech.modern_industrialization.compat.rei.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.RecipeConversions;
import aztech.modern_industrialization.machinesv2.MachineScreenHandlers;
import aztech.modern_industrialization.machinesv2.init.MIMachineRecipeTypes;
import java.util.Map;
import java.util.function.Predicate;
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

public class MachinesPlugin implements REIPluginV0 {
    @Override
    public Identifier getPluginIdentifier() {
        return new MIIdentifier("machines");
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        for (Map.Entry<String, MachineCategoryParams> entry : ReiMachineRecipes.categories.entrySet()) {
            Identifier id = new MIIdentifier(entry.getKey());
            recipeHelper.registerCategory(new MachineRecipeCategory(id, entry.getValue()));
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        // regular recipes
        for (Map.Entry<String, MachineCategoryParams> entry : ReiMachineRecipes.categories.entrySet()) {
            Identifier id = new MIIdentifier(entry.getKey());
            recipeHelper.registerRecipes(id, (Predicate<Recipe>) recipe -> {
                if (recipe instanceof MachineRecipe) {
                    return entry.getValue().recipePredicate.test((MachineRecipe) recipe);
                } else {
                    return false;
                }
            }, recipe -> new MachineRecipeDisplay(id, (MachineRecipe) recipe));
        }
        // furnace recipes
        Identifier furnaceId = new MIIdentifier("bronze_furnace");
        recipeHelper.registerRecipes(furnaceId, (Predicate<Recipe>) recipe -> recipe.getType() == RecipeType.SMELTING,
                recipe -> new MachineRecipeDisplay(furnaceId, RecipeConversions.of((SmeltingRecipe) recipe, MIMachineRecipeTypes.FURNACE)));
        // stonecutter recipes
        Identifier cuttingMachineId = new MIIdentifier("bronze_cutting_machine");
        recipeHelper.registerRecipes(cuttingMachineId, (Predicate<Recipe>) recipe -> recipe.getType() == RecipeType.STONECUTTING,
                recipe -> new MachineRecipeDisplay(cuttingMachineId,
                        RecipeConversions.of((StonecuttingRecipe) recipe, MIMachineRecipeTypes.CUTTING_MACHINE)));
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        // TODO: workstations
        // TODO: arrow clicking
        // TODO: "+" handler
        recipeHelper.registerFocusedStackProvider(screen -> {
            if (screen instanceof MachineScreenHandlers.ClientScreen) {
                Slot slot = ((MachineScreenHandlers.ClientScreen) screen).getFocusedSlot();
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

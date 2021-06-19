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
import aztech.modern_industrialization.compat.rei.Rectangle;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.RecipeConversions;
import dev.architectury.event.CompoundEventResult;
import java.util.*;
import java.util.function.Predicate;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.fluid.Fluid;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

public class MachinesPlugin implements REIClientPlugin {
    static CategoryRegistry categoryRegistry;
    static DisplayRegistry displayRegistry;

    @Override
    public void registerCategories(CategoryRegistry registry) {
        categoryRegistry = registry;

        for (Map.Entry<String, MachineCategoryParams> entry : ReiMachineRecipes.categories.entrySet()) {
            Identifier id = new MIIdentifier(entry.getKey());
            MachineRecipeCategory category = new MachineRecipeCategory(id, entry.getValue());
            registry.add(category);

            for (String workstation : entry.getValue().workstations) {
                registry.addWorkstations(category.getCategoryIdentifier(), EntryStacks.of(Registry.ITEM.get(new MIIdentifier(workstation))));
            }
        }

        MultiblockRecipeCategory multiblockCategory = new MultiblockRecipeCategory();
        registry.add(multiblockCategory);
        registry.removePlusButton(multiblockCategory.getCategoryIdentifier());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        displayRegistry = registry;

        // regular recipes
        for (Map.Entry<String, MachineCategoryParams> entry : ReiMachineRecipes.categories.entrySet()) {
            Identifier id = new MIIdentifier(entry.getKey());
            registry.registerFiller((Predicate<Recipe<?>>) recipe -> {
                if (recipe instanceof MachineRecipe) {
                    return entry.getValue().recipePredicate.test((MachineRecipe) recipe);
                } else {
                    return false;
                }
            }, recipe -> new MachineRecipeDisplay(id, (MachineRecipe) recipe));
        }
        // furnace recipes
        Identifier furnaceId = new MIIdentifier("bronze_furnace");
        registry.registerFiller((Predicate<Recipe<?>>) recipe -> recipe.getType() == RecipeType.SMELTING,
                recipe -> new MachineRecipeDisplay(furnaceId, RecipeConversions.of((SmeltingRecipe) recipe, MIMachineRecipeTypes.FURNACE)));
        // stonecutter recipes
        Identifier cuttingMachineId = new MIIdentifier("bronze_cutting_machine");
        registry.registerFiller((Predicate<Recipe<?>>) recipe -> recipe.getType() == RecipeType.STONECUTTING,
                recipe -> new MachineRecipeDisplay(cuttingMachineId,
                        RecipeConversions.of((StonecuttingRecipe) recipe, MIMachineRecipeTypes.CUTTING_MACHINE)));
        // multiblock shapes
        for (Pair<String, ShapeTemplate> entry : ReiMachineRecipes.multiblockShapes) {
            registry.add(new MultiblockRecipeDisplay(entry.getLeft(), entry.getRight()));
        }
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new SlotLockingHandler());
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerClickArea(MachineScreenHandlers.ClientScreen.class, context -> {
            MachineScreenHandlers.Client screenHandler = context.getScreen().getScreenHandler();
            String blockId = screenHandler.guiParams.blockId;
            List<ReiMachineRecipes.ClickAreaCategory> categories = ReiMachineRecipes.machineToClickAreaCategory.getOrDefault(blockId,
                    Collections.emptyList());
            Rectangle rectangle = ReiMachineRecipes.machineToClickArea.get(blockId);
            Point point = context.getMousePosition().clone();
            point.translate(-context.getScreen().x(), -context.getScreen().y());
            if (categories.size() > 0 && rectangle != null && contains(rectangle, point)) {
                ClickArea.Result result = ClickArea.Result.success();
                boolean foundSome = false;
                for (ReiMachineRecipes.ClickAreaCategory cac : categories) {
                    if (!cac.predicate.test(context.getScreen()))
                        continue;
                    List<Display> displays = displayRegistry.get(CategoryIdentifier.of(cac.category));
                    if (displays.size() > 0) {
                        result.category(CategoryIdentifier.of(cac.category));
                        foundSome = true;
                    }
                }
                return foundSome ? result : ClickArea.Result.fail();
            } else {
                return ClickArea.Result.fail();
            }
        });

        registry.registerFocusedStack((screen, mouse) -> {
            if (screen instanceof MachineScreenHandlers.ClientScreen) {
                Slot slot = ((MachineScreenHandlers.ClientScreen) screen).getFocusedSlot();
                if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                    ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
                    if (stack.getAmount() > 0) {
                        Fluid fluid = stack.getFluid().getFluid();
                        if (fluid != null) {
                            return CompoundEventResult.interruptTrue(EntryStacks.of(fluid));
                        }
                    } else if (stack.getLockedFluid() != null) {
                        Fluid fluid = stack.getLockedFluid().getFluid();
                        if (fluid != null) {
                            return CompoundEventResult.interruptTrue(EntryStacks.of(fluid));
                        }
                    }
                } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                    ConfigurableItemStack stack = ((ConfigurableItemStack.ConfigurableItemSlot) slot).getConfStack();
                    // the normal stack is already handled by REI, we just need to handle the locked
                    // item!
                    if (stack.getLockedItem() != null) {
                        return CompoundEventResult.interruptTrue(EntryStacks.of(stack.getLockedItem()));
                    }
                }
            }
            return CompoundEventResult.pass();
        });
    }

    private static boolean contains(Rectangle rectangle, Point mousePosition) {
        return rectangle.x <= mousePosition.x && mousePosition.x <= rectangle.x + rectangle.w && rectangle.y <= mousePosition.y
                && mousePosition.y <= rectangle.y + rectangle.h;
    }
}

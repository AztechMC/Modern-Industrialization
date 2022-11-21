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
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.RecipeConversions;
import aztech.modern_industrialization.util.Rectangle;
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
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.material.Fluid;

public class MachinesPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        for (Map.Entry<String, MachineCategoryParams> entry : ReiMachineRecipes.categories.entrySet()) {
            ResourceLocation id = new MIIdentifier(entry.getKey());
            MachineRecipeCategory category = new MachineRecipeCategory(id, entry.getValue());
            registry.add(category);

            for (String workstation : entry.getValue().workstations) {
                registry.addWorkstations(category.getCategoryIdentifier(), EntryStacks.of(Registry.ITEM.get(new MIIdentifier(workstation))));
            }
        }

        MultiblockRecipeCategory multiblockCategory = new MultiblockRecipeCategory();
        registry.add(multiblockCategory);
        // noinspection removal
        registry.removePlusButton(multiblockCategory.getCategoryIdentifier());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // regular recipes
        for (Map.Entry<String, MachineCategoryParams> entry : ReiMachineRecipes.categories.entrySet()) {
            ResourceLocation id = new MIIdentifier(entry.getKey());
            registry.registerFiller((Predicate<Recipe<?>>) recipe -> {
                if (recipe instanceof MachineRecipe) {
                    return entry.getValue().recipePredicate.test((MachineRecipe) recipe);
                } else {
                    return false;
                }
            }, recipe -> new MachineRecipeDisplay(id, (MachineRecipe) recipe));
        }
        // furnace recipes
        ResourceLocation furnaceId = new MIIdentifier("bronze_furnace");
        registry.registerFiller((Predicate<Recipe<?>>) recipe -> recipe.getType() == RecipeType.SMELTING,
                recipe -> new MachineRecipeDisplay(furnaceId, RecipeConversions.of((SmeltingRecipe) recipe, MIMachineRecipeTypes.FURNACE)));
        // stonecutter recipes
        ResourceLocation cuttingMachineId = new MIIdentifier("bronze_cutting_machine");
        registry.registerFiller((Predicate<Recipe<?>>) recipe -> recipe.getType() == RecipeType.STONECUTTING,
                recipe -> new MachineRecipeDisplay(cuttingMachineId,
                        RecipeConversions.of((StonecutterRecipe) recipe, MIMachineRecipeTypes.CUTTING_MACHINE)));

        // Plant Oil in Centrifuge
        for (var itemCompostable : ComposterBlock.COMPOSTABLES.keySet()) {
            var recipe = RecipeConversions.ofCompostable(itemCompostable);
            if (recipe != null) {
                registry.add(new MachineRecipeDisplay(new MIIdentifier("centrifuge"), recipe));
            }
        }

        // multiblock shapes
        for (Tuple<String, ShapeTemplate> entry : ReiMachineRecipes.multiblockShapes) {
            registry.add(new MultiblockRecipeDisplay(entry.getA(), entry.getB()));
        }
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new SlotLockingHandler());
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerClickArea(MachineScreen.class, context -> {
            MachineMenuClient screenHandler = context.getScreen().getMenu();
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
                    List<Display> displays = DisplayRegistry.getInstance().get(CategoryIdentifier.of(cac.category));
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
            if (screen instanceof MachineScreen) {
                Slot slot = ((MachineScreen) screen).getFocusedSlot();
                if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                    ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
                    if (stack.getAmount() > 0) {
                        Fluid fluid = stack.getResource().getFluid();
                        if (fluid != null) {
                            return CompoundEventResult.interruptTrue(EntryStacks.of(fluid));
                        }
                    } else if (stack.getLockedInstance() != null) {
                        Fluid fluid = stack.getLockedInstance();
                        if (fluid != null) {
                            return CompoundEventResult.interruptTrue(EntryStacks.of(fluid));
                        }
                    }
                } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                    ConfigurableItemStack stack = ((ConfigurableItemStack.ConfigurableItemSlot) slot).getConfStack();
                    // the normal stack is already handled by REI, we just need to handle the locked
                    // item!
                    if (stack.getLockedInstance() != null) {
                        return CompoundEventResult.interruptTrue(EntryStacks.of(stack.getLockedInstance()));
                    }
                }
            }
            return CompoundEventResult.pass();
        });

        registry.exclusionZones().register(MachineScreen.class, screen -> {
            return screen.getExtraBoxes().stream().map(r -> new me.shedaniel.math.Rectangle(r.x(), r.y(), r.w(), r.h())).toList();
        });
    }

    private static boolean contains(Rectangle rectangle, Point mousePosition) {
        return rectangle.x() <= mousePosition.x && mousePosition.x <= rectangle.x() + rectangle.w() && rectangle.y() <= mousePosition.y
                && mousePosition.y <= rectangle.y() + rectangle.h();
    }
}

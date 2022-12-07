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
package aztech.modern_industrialization.compat.viewer.impl.jei;

import aztech.modern_industrialization.compat.jeirei.MachineScreenPredicateTest;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.util.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

record MachineGuiContainerHandler(Supplier<IJeiRuntime> jeiRuntime) implements IGuiContainerHandler<MachineScreen> {
    @Override
    public List<Rect2i> getGuiExtraAreas(MachineScreen screen) {
        return screen.getExtraBoxes().stream().map(r -> new Rect2i(r.x(), r.y(), r.w(), r.h())).toList();
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(MachineScreen screen, double guiMouseX, double guiMouseY) {

        MachineMenuClient screenHandler = screen.getMenu();
        String blockId = screenHandler.guiParams.blockId;
        List<ReiMachineRecipes.ClickAreaCategory> categories = ReiMachineRecipes.machineToClickAreaCategory.getOrDefault(blockId,
                Collections.emptyList());
        Rectangle rectangle = ReiMachineRecipes.machineToClickArea.get(blockId);

        if (categories.size() > 0 && rectangle != null && contains(rectangle, guiMouseX, guiMouseY)) {
            boolean foundSome = false;
            var result = new ArrayList<RecipeType<?>>();
            for (ReiMachineRecipes.ClickAreaCategory cac : categories) {
                if (!MachineScreenPredicateTest.test(cac.predicate, screen))
                    continue;

                var recipeManager = jeiRuntime.get().getRecipeManager();
                var recipeType = recipeManager.getRecipeType(cac.category).orElse(null);
                if (recipeType != null) {
                    if (recipeManager.createRecipeLookup(recipeType).get().anyMatch(t -> true)) {
                        result.add(recipeType);
                        foundSome = true;
                    }
                }
            }

            if (foundSome) {
                return List.of(
                        new IGuiClickableArea() {
                            @Override
                            public Rect2i getArea() {
                                return new Rect2i(rectangle.x(), rectangle.y(), rectangle.w(), rectangle.h());
                            }

                            @Override
                            public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
                                recipesGui.showTypes(result);
                            }
                        });
            }
        }

        return Collections.emptyList();
    }

    @Override
    public @Nullable Object getIngredientUnderMouse(MachineScreen screen, double mouseX, double mouseY) {
        Slot slot = screen.getFocusedSlot();
        if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
            var fluidHelper = jeiRuntime.get().getJeiHelpers().getPlatformFluidHelper();

            ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
            if (stack.getAmount() > 0) {
                Fluid fluid = stack.getResource().getFluid();
                if (fluid != null) {
                    return fluidHelper.create(fluid, 1);
                }
            } else if (stack.getLockedInstance() != null) {
                Fluid fluid = stack.getLockedInstance();
                if (fluid != null) {
                    return fluidHelper.create(fluid, 1);
                }
            }
        } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
            ConfigurableItemStack stack = ((ConfigurableItemStack.ConfigurableItemSlot) slot).getConfStack();
            // the normal stack is already handled by REI, we just need to handle the locked
            // item!
            if (stack.getLockedInstance() != null) {
                return stack.getLockedInstance().getDefaultInstance();
            }
        }
        return null;
    }

    private static boolean contains(Rectangle rectangle, double x, double y) {
        return rectangle.x() <= x && x <= rectangle.x() + rectangle.w() && rectangle.y() <= y
                && y <= rectangle.y() + rectangle.h();
    }
}

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
package aztech.modern_industrialization.compat.jei.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.RecipeConversions;
import aztech.modern_industrialization.util.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class MachinesPlugin implements IModPlugin {

    private final Map<String, mezz.jei.api.recipe.RecipeType<MachineRecipe>> types = new HashMap<>();

    private IJeiRuntime jeiRuntime;

    @Override
    public ResourceLocation getPluginUid() {
        return new MIIdentifier("machines");
    }

    private mezz.jei.api.recipe.RecipeType<MachineRecipe> getType(String id) {
        return types.computeIfAbsent(id, s -> mezz.jei.api.recipe.RecipeType.create(ModernIndustrialization.MOD_ID, id, MachineRecipe.class));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helpers = registration.getJeiHelpers();
        for (var entry : ReiMachineRecipes.categories.entrySet()) {
            var type = getType(entry.getKey());
            registration.addRecipeCategories(new MachineRecipeCategory(helpers, type, entry.getValue()));
        }

        registration.addRecipeCategories(new MultiblockRecipeCategory(helpers.getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var entry : ReiMachineRecipes.categories.entrySet()) {
            var type = getType(entry.getKey());
            for (String workstation : entry.getValue().workstations) {
                registration.addRecipeCatalyst(Registry.ITEM.get(new MIIdentifier(workstation)).getDefaultInstance(), type);
            }
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = Minecraft.getInstance().level.getRecipeManager();

        var machineRecipes = recipeManager.getRecipes().stream()
                .filter(r -> r instanceof MachineRecipe)
                .map(r -> (MachineRecipe) r)
                .toList();

        // regular recipes
        for (var entry : ReiMachineRecipes.categories.entrySet()) {
            var type = getType(entry.getKey());

            var categoryRecipes = machineRecipes
                    .stream()
                    .filter(entry.getValue().recipePredicate)
                    .toList();

            registration.addRecipes(type, categoryRecipes);
        }

        // furnace recipes
        var furnaceId = getType("bronze_furnace");
        var furnaceRecipes = recipeManager.getAllRecipesFor(RecipeType.SMELTING)
                .stream()
                .map(r -> RecipeConversions.of(r, MIMachineRecipeTypes.FURNACE))
                .toList();
        registration.addRecipes(furnaceId, furnaceRecipes);

        // stonecutter recipes
        var cuttingMachineId = getType("bronze_cutting_machine");
        var cuttingMachineRecipes = recipeManager.getAllRecipesFor(RecipeType.STONECUTTING)
                .stream()
                .map(r -> RecipeConversions.of(r, MIMachineRecipeTypes.CUTTING_MACHINE))
                .toList();
        registration.addRecipes(cuttingMachineId, cuttingMachineRecipes);

        // Plant Oil in Centrifuge
        var centrifugeId = getType("centrifuge");
        for (var itemCompostable : ComposterBlock.COMPOSTABLES.keySet()) {
            var recipe = RecipeConversions.ofCompostable(itemCompostable);
            if (recipe != null) {
                registration.addRecipes(centrifugeId, List.of(recipe));
            }
        }

        // multiblock shapes
        for (var entry : ReiMachineRecipes.multiblockShapes) {
            registration.addRecipes(MultiblockRecipeCategory.TYPE, List.of(new MultiblockRecipeDisplay(entry.getA(), entry.getB())));
        }
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        var helper = registration.getTransferHelper();
        for (var entry : ReiMachineRecipes.categories.entrySet()) {
            var type = getType(entry.getKey());

            registration.addRecipeTransferHandler(
                    new SlotLockingHandler(helper, () -> jeiRuntime, type),
                    type);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        this.jeiRuntime = jeiRuntime;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

        registration.addGuiContainerHandler(MachineScreen.class, new IGuiContainerHandler<>() {
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
                    var result = new ArrayList<mezz.jei.api.recipe.RecipeType<?>>();
                    for (ReiMachineRecipes.ClickAreaCategory cac : categories) {
                        if (!cac.predicate.test(screen))
                            continue;

                        var recipeManager = jeiRuntime.getRecipeManager();
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
                    var fluidHelper = jeiRuntime.getJeiHelpers().getPlatformFluidHelper();

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
        });
    }

    private static boolean contains(Rectangle rectangle, double x, double y) {
        return rectangle.x() <= x && x <= rectangle.x() + rectangle.w() && rectangle.y() <= y
                && y <= rectangle.y() + rectangle.h();
    }

}

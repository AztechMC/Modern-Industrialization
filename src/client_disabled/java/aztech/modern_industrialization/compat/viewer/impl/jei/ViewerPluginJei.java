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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.compat.viewer.usage.ViewerSetup;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
@REIPluginCompatIgnore
public class ViewerPluginJei implements IModPlugin {
    private static final ResourceLocation ID = new MIIdentifier("viewer");

    private final List<ViewerCategoryJei<?>> categories = new ArrayList<>();

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        categories.clear(); // needed for reloads

        for (var category : ViewerSetup.setup()) {
            categories.add(new ViewerCategoryJei<>(registration.getJeiHelpers(), category));
        }

        registration.addRecipeCategories(categories.toArray(new IRecipeCategory[0]));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var category : categories) {
            category.wrapped.buildWorkstations(items -> {
                for (var item : items) {
                    registration.addRecipeCatalyst(item.asItem().getDefaultInstance(), category.recipeType);
                }
            });
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (var category : categories) {
            registerCategoryRecipes(registration, category);
        }
    }

    private static <D> void registerCategoryRecipes(IRecipeRegistration registration, ViewerCategoryJei<D> category) {
        var level = Minecraft.getInstance().level;
        List<D> recipes = new ArrayList<>();
        category.wrapped.buildRecipes(level.getRecipeManager(), level.registryAccess(), recipes::add);
        registration.addRecipes(category.recipeType, recipes);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}

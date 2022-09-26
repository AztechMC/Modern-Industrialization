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
package aztech.modern_industrialization.compat.jei.fluid_fuels;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

@JeiPlugin
public class FluidFuelsPlugin implements IModPlugin {
    static final RecipeType<Fluid> CATEGORY = RecipeType.create(ModernIndustrialization.MOD_ID, "fluid_fuels", Fluid.class);

    @Override
    public ResourceLocation getPluginUid() {
        return CATEGORY.getUid();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new FluidFuelsCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        addItem(registration, "diesel_generator", "turbo_diesel_generator", "large_diesel_generator", "large_steam_boiler",
                "advanced_large_steam_boiler",
                "high_pressure_large_steam_boiler",
                "high_pressure_advanced_large_steam_boiler");
        addItem(registration, MIItem.DIESEL_JETPACK.asItem(), MIItem.DIESEL_CHAINSAW.asItem(), MIItem.DIESEL_MINING_DRILL.asItem());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CATEGORY, FluidFuelRegistry.getRegisteredFluids());
    }

    private static Item get(String id) {
        return Registry.ITEM.get(new MIIdentifier(id));
    }

    private void addItem(IRecipeCatalystRegistration registry, Item... items) {
        for (Item item : items) {
            registry.addRecipeCatalyst(item.getDefaultInstance(), CATEGORY);
        }
    }

    private void addItem(IRecipeCatalystRegistration registry, String... idNamespaces) {
        for (String idNamespace : idNamespaces) {
            registry.addRecipeCatalyst(get(idNamespace).getDefaultInstance(), CATEGORY);
        }
    }

}

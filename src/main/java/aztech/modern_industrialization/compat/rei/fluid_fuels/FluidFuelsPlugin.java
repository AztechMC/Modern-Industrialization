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
package aztech.modern_industrialization.compat.rei.fluid_fuels;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.util.TextHelper;
import java.util.Collections;
import java.util.List;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FluidFuelsPlugin implements REIPluginV0 {
    static final Identifier CATEGORY = new MIIdentifier("fluid_fuels");

    @Override
    public Identifier getPluginIdentifier() {
        return new MIIdentifier("fluid_fuels");
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategory(new FluidFuelsCategory());
    }

    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        for (Fluid fluid : FluidFuelRegistry.getRegisteredFluids()) {
            recipeHelper.registerDisplay(new FluidFuelDisplay(fluid));
        }
    }

    private static Item get(String id) {
        return Registry.ITEM.get(new MIIdentifier(id));
    }

    private void addItem(RecipeHelper recipeHelper, Item... items) {
        for (Item item : items) {
            recipeHelper.registerWorkingStations(CATEGORY, EntryStack.create(item));
        }
    }

    private void addItem(RecipeHelper recipeHelper, String... idNamespaces) {
        for (String idNamespace : idNamespaces) {
            recipeHelper.registerWorkingStations(CATEGORY, EntryStack.create(get(idNamespace)));
        }
    }

    private static final List<Text> DOUBLE_EFFICIENCY = Collections
            .singletonList(new TranslatableText("text.modern_industrialization.double_fluid_fuel_efficiency").setStyle(TextHelper.UPGRADE_TEXT));

    private void addDoubleEfficiency(RecipeHelper recipeHelper, String... idNamespaces) {
        for (String idNamespace : idNamespaces) {
            EntryStack entry = EntryStack.create(get(idNamespace));
            entry.addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, es -> DOUBLE_EFFICIENCY);
            recipeHelper.registerWorkingStations(CATEGORY, entry);
        }
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        addItem(recipeHelper, "diesel_generator", "turbo_diesel_generator", "large_diesel_generator");
        addDoubleEfficiency(recipeHelper, "large_steam_boiler", "advanced_large_steam_boiler", "high_pressure_large_steam_boiler",
                "high_pressure_advanced_large_steam_boiler");
        addItem(recipeHelper, ModernIndustrialization.ITEM_JETPACK, ModernIndustrialization.ITEM_DIESEL_CHAINSAW,
                ModernIndustrialization.ITEM_DIESEL_DRILL);

        recipeHelper.removeAutoCraftButton(CATEGORY);
    }
}

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
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.util.TextHelper;
import java.util.Collections;
import java.util.List;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class FluidFuelsPlugin implements REIClientPlugin {
    static final CategoryIdentifier<FluidFuelDisplay> CATEGORY = CategoryIdentifier.of(new MIIdentifier("fluid_fuels"));

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new FluidFuelsCategory());

        addItem(registry, "diesel_generator", "turbo_diesel_generator", "large_diesel_generator");
        addDoubleEfficiency(registry, "large_steam_boiler", "advanced_large_steam_boiler", "high_pressure_large_steam_boiler",
                "high_pressure_advanced_large_steam_boiler");
        addItem(registry, MIItem.ITEM_DIESEL_JETPACK.asItem(), MIItem.ITEM_DIESEL_CHAINSAW.asItem(), MIItem.ITEM_DIESEL_MINING_DRILL.asItem());

        registry.removePlusButton(CATEGORY);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (Fluid fluid : FluidFuelRegistry.getRegisteredFluids()) {
            registry.add(new FluidFuelDisplay(fluid));
        }
    }

    private static Item get(String id) {
        return Registry.ITEM.get(new MIIdentifier(id));
    }

    private void addItem(CategoryRegistry registry, Item... items) {
        for (Item item : items) {
            registry.addWorkstations(CATEGORY, EntryStacks.of(item));
        }
    }

    private void addItem(CategoryRegistry registry, String... idNamespaces) {
        for (String idNamespace : idNamespaces) {
            registry.addWorkstations(CATEGORY, EntryStacks.of(get(idNamespace)));
        }
    }

    private static final List<Component> DOUBLE_EFFICIENCY = Collections
            .singletonList(MIText.DoubleFluidFuelEfficiency.text().setStyle(TextHelper.UPGRADE_TEXT));

    private void addDoubleEfficiency(CategoryRegistry registry, String... idNamespaces) {
        for (String idNamespace : idNamespaces) {
            EntryStack<?> entry = EntryStacks.of(get(idNamespace)).setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, es -> DOUBLE_EFFICIENCY);
            registry.addWorkstations(CATEGORY, entry);
        }
    }
}

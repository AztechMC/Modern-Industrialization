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
package aztech.modern_industrialization.compat.rei.nuclear;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.nuclear.NuclearComponentItem;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.util.registry.Registry;

public class NeutronInteractionPlugin implements REIClientPlugin {

    static final CategoryIdentifier<NeutronInteractionDisplay> CATEGORY = CategoryIdentifier.of(new MIIdentifier("neutron_interaction"));

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new NeutronInteractionCategory());
        registry.addWorkstations(CATEGORY, EntryStacks.of(Registry.ITEM.get(new MIIdentifier("nuclear_reactor"))));
        registry.removePlusButton(CATEGORY);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        Registry.ITEM.stream().filter(item -> item instanceof NuclearComponentItem).forEach(item -> {
            registry.add(new NeutronInteractionDisplay((NuclearComponentItem) item, NeutronInteractionDisplay.CategoryType.FAST_NEUTRON_INTERACTION));
            registry.add(
                    new NeutronInteractionDisplay((NuclearComponentItem) item, NeutronInteractionDisplay.CategoryType.THERMAL_NEUTRON_INTERACTION));
            if (item instanceof NuclearFuel) {
                registry.add(new NeutronInteractionDisplay((NuclearComponentItem) item, NeutronInteractionDisplay.CategoryType.FISSION));
            }
        });
    }
}

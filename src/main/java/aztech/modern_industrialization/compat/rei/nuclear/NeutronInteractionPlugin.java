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
import aztech.modern_industrialization.nuclear.*;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class NeutronInteractionPlugin implements REIClientPlugin {

    static final CategoryIdentifier<NeutronInteractionDisplay> NEUTRON_CATEGORY = CategoryIdentifier.of(new MIIdentifier("neutron_interaction"));
    static final CategoryIdentifier<ThermalInteractionDisplay> THERMAL_CATEGORY = CategoryIdentifier.of(new MIIdentifier("thermal_interaction"));

    @Override
    public void registerCategories(CategoryRegistry registry) {

        registry.add(new NeutronInteractionCategory());
        registry.addWorkstations(NEUTRON_CATEGORY, EntryStacks.of(Registry.ITEM.get(new MIIdentifier("nuclear_reactor"))));
        registry.removePlusButton(NEUTRON_CATEGORY);

        registry.add(new ThermalInteractionCategory());
        registry.addWorkstations(THERMAL_CATEGORY, EntryStacks.of(Registry.ITEM.get(new MIIdentifier("nuclear_reactor"))));
        registry.removePlusButton(THERMAL_CATEGORY);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {

        Registry.ITEM.stream().filter(item -> item instanceof NuclearComponentItem).forEach(item -> {

            NuclearComponentItem component = (NuclearComponentItem) item;
            if (component.neutronBehaviour != INeutronBehaviour.NO_INTERACTION) {
                registry.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.FAST_NEUTRON_INTERACTION));
                registry.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.THERMAL_NEUTRON_INTERACTION));
            }

            registry.add(new ThermalInteractionDisplay(component, ThermalInteractionDisplay.CategoryType.THERMAL_PROPERTIES));

            if (item instanceof NuclearFuel) {
                registry.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.FISSION));
                registry.add(new ThermalInteractionDisplay(component, ThermalInteractionDisplay.CategoryType.NEUTRON_EFFICIENCY));
            }

            ItemVariant product = component.getNeutronProduct();
            if (product != null) {
                registry.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.NEUTRON_PRODUCT));
            }

        });

        for (Fluid fluid : Registry.FLUID) {
            if (fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
                FluidVariant variant = FluidVariant.of(fluid);
                INuclearComponent component = INuclearComponent.of(variant);
                if (component != null) {
                    registry.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.FAST_NEUTRON_INTERACTION));
                    registry.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.THERMAL_NEUTRON_INTERACTION));
                    registry.add(new ThermalInteractionDisplay(component, ThermalInteractionDisplay.CategoryType.THERMAL_PROPERTIES));

                    if (component.getVariant() != null) {
                        registry.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.NEUTRON_PRODUCT));
                    }
                }
            }
        }

        for (String s : new String[] { "item", "fluid" }) {
            registry.add(new ThermalInteractionDisplay(new INuclearComponent<ItemVariant>() {
                @Override
                public double getHeatConduction() {
                    return NuclearConstant.BASE_HEAT_CONDUCTION;
                }

                @Override
                public INeutronBehaviour getNeutronBehaviour() {
                    return null;
                }

                public int getMaxTemperature() {
                    return NuclearConstant.MAX_TEMPERATURE;
                }

                @Override
                public ItemVariant getVariant() {
                    return ItemVariant.of(Registry.ITEM.get(new MIIdentifier(String.format("nuclear_%s_hatch", s))));
                }
            }, ThermalInteractionDisplay.CategoryType.THERMAL_PROPERTIES));
        }
    }
}

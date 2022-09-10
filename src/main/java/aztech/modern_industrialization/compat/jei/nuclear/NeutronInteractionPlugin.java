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
package aztech.modern_industrialization.compat.jei.nuclear;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.nuclear.INeutronBehaviour;
import aztech.modern_industrialization.nuclear.INuclearComponent;
import aztech.modern_industrialization.nuclear.NuclearComponentItem;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import java.util.ArrayList;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

@JeiPlugin
public class NeutronInteractionPlugin implements IModPlugin {

    static final RecipeType<NeutronInteractionDisplay> NEUTRON_CATEGORY = RecipeType.create(ModernIndustrialization.MOD_ID, "neutron_interaction",
            NeutronInteractionDisplay.class);
    static final RecipeType<ThermalInteractionDisplay> THERMAL_CATEGORY = RecipeType.create(ModernIndustrialization.MOD_ID, "thermal_interaction",
            ThermalInteractionDisplay.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new MIIdentifier("neutron_interaction");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new NeutronInteractionCategory(registration.getJeiHelpers()));
        registration.addRecipeCategories(new ThermalInteractionCategory(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        var reactor = Registry.ITEM.get(new MIIdentifier("nuclear_reactor")).getDefaultInstance();
        registration.addRecipeCatalyst(reactor, NEUTRON_CATEGORY);
        registration.addRecipeCatalyst(reactor, THERMAL_CATEGORY);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        var neutronRecipes = new ArrayList<NeutronInteractionDisplay>();
        var thermalRecipes = new ArrayList<ThermalInteractionDisplay>();

        Registry.ITEM.stream().filter(item -> item instanceof NuclearComponentItem).forEach(item -> {

            NuclearComponentItem component = (NuclearComponentItem) item;
            if (component.neutronBehaviour != INeutronBehaviour.NO_INTERACTION) {
                neutronRecipes.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.FAST_NEUTRON_INTERACTION));
                neutronRecipes.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.THERMAL_NEUTRON_INTERACTION));
            }

            thermalRecipes.add(new ThermalInteractionDisplay(component, ThermalInteractionDisplay.CategoryType.THERMAL_PROPERTIES));

            if (item instanceof NuclearFuel) {
                neutronRecipes.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.FISSION));
                thermalRecipes.add(new ThermalInteractionDisplay(component, ThermalInteractionDisplay.CategoryType.NEUTRON_EFFICIENCY));
            }

            ItemVariant product = component.getNeutronProduct();
            if (product != null) {
                neutronRecipes.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.NEUTRON_PRODUCT));
            }

        });

        for (Fluid fluid : Registry.FLUID) {
            if (fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
                FluidVariant variant = FluidVariant.of(fluid);
                INuclearComponent component = INuclearComponent.of(variant);
                if (component != null) {
                    neutronRecipes.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.FAST_NEUTRON_INTERACTION));
                    neutronRecipes.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.THERMAL_NEUTRON_INTERACTION));
                    thermalRecipes.add(new ThermalInteractionDisplay(component, ThermalInteractionDisplay.CategoryType.THERMAL_PROPERTIES));

                    if (component.getVariant() != null) {
                        neutronRecipes.add(new NeutronInteractionDisplay(component, NeutronInteractionDisplay.CategoryType.NEUTRON_PRODUCT));
                    }
                }
            }
        }

        for (String s : new String[] { "item", "fluid" }) {
            thermalRecipes.add(new ThermalInteractionDisplay(new INuclearComponent<ItemVariant>() {
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

        registration.addRecipes(NEUTRON_CATEGORY, neutronRecipes);
        registration.addRecipes(THERMAL_CATEGORY, thermalRecipes);
    }
}

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
package aztech.modern_industrialization.compat.viewer.usage;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.nuclear.FluidNuclearComponent;
import aztech.modern_industrialization.nuclear.INeutronBehaviour;
import aztech.modern_industrialization.nuclear.INuclearComponent;
import aztech.modern_industrialization.nuclear.NuclearComponentItem;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Rectangle;
import aztech.modern_industrialization.util.TextHelper;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ThermalInteractionCategory extends ViewerCategory<ThermalInteractionCategory.Recipe> {
    private final int centerX;
    private final int px, py;

    protected ThermalInteractionCategory() {
        super(Recipe.class, new MIIdentifier("thermal_interaction"), MIText.ThermalInteraction.text(),
                new Icon.Texture(MachineScreen.SLOT_ATLAS, 145, 1), 150, 100);

        this.centerX = width / 2;
        this.px = centerX - 9;
        this.py = 22;
    }

    @Override
    public void buildWorkstations(WorkstationConsumer consumer) {
        consumer.accept("nuclear_reactor");
    }

    @Override
    public void buildRecipes(RecipeManager recipeManager, RegistryAccess registryAccess, Consumer<Recipe> consumer) {
        BuiltInRegistries.ITEM.stream().filter(item -> item instanceof NuclearComponentItem).forEach(item -> {
            NuclearComponentItem component = (NuclearComponentItem) item;
            consumer.accept(new Recipe(component, CategoryType.THERMAL_PROPERTIES));

            if (item instanceof NuclearFuel) {
                consumer.accept(new Recipe(component, CategoryType.NEUTRON_EFFICIENCY));
            }
        });

        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
                INuclearComponent<?> component = FluidNuclearComponent.get(fluid);

                if (component != null) {
                    consumer.accept(new Recipe(component, CategoryType.THERMAL_PROPERTIES));
                }
            }
        }

        for (String s : new String[] { "item", "fluid" }) {
            consumer.accept(new Recipe(new INuclearComponent<ItemVariant>() {
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
                    return ItemVariant.of(BuiltInRegistries.ITEM.get(new MIIdentifier(String.format("nuclear_%s_hatch", s))));
                }
            }, CategoryType.THERMAL_PROPERTIES));
        }
    }

    @Override
    public void buildLayout(Recipe recipe, LayoutBuilder builder) {
        builder.inputSlot(px, 22).variant(recipe.nuclearComponent.getVariant());
    }

    @Override
    public void buildWidgets(Recipe recipe, WidgetList widgets) {
        widgets.text(switch (recipe.type) {
        case NEUTRON_EFFICIENCY -> MIText.NeutronProductionTemperatureEffect.text();
        case THERMAL_PROPERTIES -> MIText.ThermalInteraction.text();
        }, centerX, 8, TextAlign.CENTER, false, true, null);

        switch (recipe.type) {
        case NEUTRON_EFFICIENCY -> {
            var nuclearComponent = (NuclearFuel) recipe.nuclearComponent;
            var area = new Rectangle(10, 45, width - 20, height - 50);
            widgets.rectangle(area, 0xFF8b8b8b);

            int py = height - 14;

            widgets.text(Component.literal("0°C"), 11, py, TextAlign.LEFT, true, false, null);
            widgets.text(Component.literal(String.format("%d°C", nuclearComponent.tempLimitLow)), centerX, py, TextAlign.CENTER, true, false, null);
            widgets.text(Component.literal(String.format("%d°C", nuclearComponent.tempLimitHigh)), width - 10, py, TextAlign.RIGHT, true, false,
                    null);

            widgets.drawable(guiGraphics -> {
                var helper = Minecraft.getInstance().screen;
                for (int i = 1; i < area.w() / 2; i++) {
                    guiGraphics.blit(MachineScreen.SLOT_ATLAS, area.x() + i, area.y() + 4, 0, 255, 1, 1);
                }
                for (int i = area.w() / 2; i < area.w() - 1; i++) {
                    double f = (i - area.w() / 2d) / (area.w() - area.w() / 2d);
                    int y = (int) ((1 - f) * (area.y() + 4) + f * (area.y() + area.h() - 14));
                    guiGraphics.blit(MachineScreen.SLOT_ATLAS, area.x() + i, y, 0, 255, 1, 1);
                }
            });

            widgets.text(Component.literal(String.format("%.1f", nuclearComponent.neutronMultiplicationFactor)), 11, area.y() + 2, TextAlign.LEFT,
                    false, false, null);
            widgets.text(Component.literal("0"), width - 10, py - 10, TextAlign.RIGHT, false, false, null);
        }
        case THERMAL_PROPERTIES -> {
            var nuclearComponent = recipe.nuclearComponent;
            int centerX = width / 2;
            int centerY = height / 2;

            Component heatConduction = MIText.HeatConduction.text(
                    String.format("%d", (int) (1000 * nuclearComponent.getHeatConduction()))).setStyle(TextHelper.HEAT_CONDUCTION);

            widgets.text(heatConduction, centerX, centerY, TextAlign.CENTER, false, false, null);

            int maxTemperature = nuclearComponent.getMaxTemperature();

            if (maxTemperature != Integer.MAX_VALUE) {
                Component maxTemp = MIText.MaxTemp.text(maxTemperature).setStyle(TextHelper.MAX_TEMP_TEXT);
                widgets.text(maxTemp, centerX, centerY + 12, TextAlign.CENTER, false, false, null);
            }
        }
        }
    }

    protected record Recipe(INuclearComponent<?> nuclearComponent, CategoryType type) {
    }

    enum CategoryType {
        NEUTRON_EFFICIENCY,
        THERMAL_PROPERTIES,
    }
}

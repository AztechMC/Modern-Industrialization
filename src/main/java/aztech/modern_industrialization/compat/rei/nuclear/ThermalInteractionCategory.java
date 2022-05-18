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

import static net.minecraft.client.gui.GuiComponent.blit;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.compat.rei.ReiUtil;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.nuclear.INuclearComponent;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ThermalInteractionCategory implements DisplayCategory<ThermalInteractionDisplay> {

    @Override
    public Renderer getIcon() {
        return new Renderer() {
            private int z = 2;

            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                blit(matrices, bounds.x - 1, bounds.y - 1, z, 145, 1, 18, 18, 256, 256);
            }

            public int getZ() {
                return z;
            }

            public void setZ(int z) {
                this.z = z;
            }
        };
    }

    @Override
    public List<Widget> setupDisplay(ThermalInteractionDisplay display, Rectangle bounds) {
        if (display.type == ThermalInteractionDisplay.CategoryType.NEUTRON_EFFICIENCY) {
            return setupNeutronEfficiency((NuclearFuel) display.nuclearComponent, bounds);
        } else if (display.type == ThermalInteractionDisplay.CategoryType.THERMAL_PROPERTIES) {
            return setupThermalProperties(display.nuclearComponent, bounds);
        }
        return null;
    }

    private List<Widget> setupNeutronEfficiency(NuclearFuel nuclearComponent, Rectangle bounds) {
        List<Widget> widgets = defaultWidget(nuclearComponent, bounds, MIText.NeutronProductionTemperatureEffect.text());

        Rectangle area = new Rectangle(bounds.x + 10, bounds.y + 45, bounds.getWidth() - 20, bounds.getHeight() - 50);
        widgets.add(Widgets.createFilledRectangle(area, 0xFF8b8b8b));

        int py = bounds.y + bounds.getHeight() - 14;
        int px1 = bounds.x + 20;
        int px3 = bounds.x + bounds.getWidth() - 20;
        int px2 = (px1 + px3) / 2;

        widgets.add(Widgets.createLabel(new Point(px1, py), new TextComponent("0°C")));

        widgets.add(Widgets.createLabel(new Point(px2, py), new TextComponent(String.format("%d°C", nuclearComponent.tempLimitLow))));

        widgets.add(Widgets.createLabel(new Point(px3 - 8, py), new TextComponent(String.format("%d°C", nuclearComponent.tempLimitHigh))));

        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
            for (int i = 1; i < area.getWidth() / 2; i++) {
                helper.blit(matrices, area.x + i, area.y + 4, 0, 255, 1, 1);
            }
            for (int i = area.getWidth() / 2; i < area.getWidth() - 1; i++) {
                double f = (i - area.getWidth() / 2d) / (area.getWidth() - area.getWidth() / 2d);
                int y = (int) ((1 - f) * (area.y + 4) + f * (area.y + area.height - 14));
                helper.blit(matrices, area.x + i, y, 0, 255, 1, 1);
            }
        }));

        widgets.add(Widgets
                .createLabel(new Point(px1 - 2, area.y + 2), new TextComponent(String.format("%.1f", nuclearComponent.neutronMultiplicationFactor)))
                .noShadow());

        widgets.add(Widgets.createLabel(new Point(px3 + 6, py - 10), new TextComponent("0")).noShadow());

        return widgets;
    }

    private static List<Widget> defaultWidget(INuclearComponent nuclearComponent, Rectangle bounds, Component title) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        int centerX = bounds.x + bounds.width / 2;

        int px = centerX - 9;
        int py = bounds.y + 22;

        Point pos = new Point(px, py);

        if (nuclearComponent.getVariant() instanceof ItemVariant itemVariant) {
            widgets.add(Widgets.createSlot(pos).entry(EntryStacks.of(itemVariant.getItem())));
        } else if (nuclearComponent.getVariant() instanceof FluidVariant fluidVariant) {
            widgets.add(Widgets.createSlot(pos).entry(ReiUtil.createFluidEntryStack(fluidVariant.getFluid())));
        }

        Point posTitle = new Point(centerX, bounds.y + 8);
        widgets.add(Widgets.createLabel(posTitle, title));
        return widgets;

    }

    private List<Widget> setupThermalProperties(INuclearComponent nuclearComponent, Rectangle bounds) {
        List<Widget> widgets = defaultWidget(nuclearComponent, bounds, MIText.ThermalInteraction.text());
        int centerX = bounds.x + bounds.width / 2;
        int centerY = bounds.y + bounds.height / 2;

        Component heatConduction = MIText.HeatConduction.text(
                String.format("%.2f", nuclearComponent.getHeatConduction())).setStyle(TextHelper.HEAT_CONDUCTION);

        widgets.add(Widgets.createLabel(new Point(centerX, centerY), heatConduction).noShadow());

        int maxTemperature = nuclearComponent.getMaxTemperature();

        if (maxTemperature != Integer.MAX_VALUE) {
            Component maxTemp = MIText.MaxTemp.text(maxTemperature).setStyle(TextHelper.MAX_TEMP_TEXT);
            widgets.add(Widgets.createLabel(new Point(centerX, centerY + 12), maxTemp).noShadow());
        }

        return widgets;
    }

    @Override
    public Component getTitle() {
        return MIText.ThermalInteraction.text();
    }

    @Override
    public int getDisplayHeight() {
        return 100;
    }

    @Override
    public CategoryIdentifier<? extends ThermalInteractionDisplay> getCategoryIdentifier() {
        return NeutronInteractionPlugin.THERMAL_CATEGORY;
    }
}

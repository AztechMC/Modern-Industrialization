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

import static net.minecraft.client.gui.GuiComponent.blit;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.compat.jei.Label;
import aztech.modern_industrialization.compat.jei.Widget;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.nuclear.INuclearComponent;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.util.Rectangle;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;

public class ThermalInteractionCategory implements IRecipeCategory<ThermalInteractionDisplay> {

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotBackground;

    public ThermalInteractionCategory(IJeiHelpers helpers) {
        var guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(150, 100);
        this.icon = new IDrawable() {
            @Override
            public int getWidth() {
                return 18;
            }

            @Override
            public int getHeight() {
                return 18;
            }

            @Override
            public void draw(PoseStack poseStack, int xOffset, int yOffset) {
                RenderSystem.setShaderTexture(0, MachineScreen.SLOT_ATLAS);
                blit(poseStack, xOffset - 1, yOffset - 1, 0, 145, 1, 18, 18, 256, 256);
            }
        };
        this.slotBackground = helpers.getGuiHelper().getSlotDrawable();
    }

    @Override
    public RecipeType<ThermalInteractionDisplay> getRecipeType() {
        return NeutronInteractionPlugin.THERMAL_CATEGORY;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ThermalInteractionDisplay recipe, IFocusGroup focuses) {
        getView(recipe).setupSlots(builder);
    }

    @Override
    public void draw(ThermalInteractionDisplay recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        getView(recipe).draw(stack);
    }

    @Override
    public Component getTitle() {
        return MIText.ThermalInteraction.text();
    }

    private class NeutronEfficiencyView extends View {
        public NeutronEfficiencyView(NuclearFuel nuclearFuel) {
            super(nuclearFuel, MIText.NeutronProductionTemperatureEffect.text());

            var area = new Rectangle(10, 45, background.getWidth() - 20, background.getHeight() - 50);
            widgets.add(stack -> GuiComponent.fill(stack, area.x(), area.y(), area.x() + area.w(), area.y() + area.h(), 0xFF8b8b8b));

            int py = background.getHeight() - 14;
            int px1 = 20;
            int px3 = background.getWidth() - 20;
            int px2 = (px1 + px3) / 2;

            widgets.add(new Label(px1, py, Component.literal("0°C")));

            widgets.add(new Label(px2, py, Component.literal(String.format("%d°C", nuclearFuel.tempLimitLow))));

            widgets.add(new Label(px3 - 8, py, Component.literal(String.format("%d°C", nuclearFuel.tempLimitHigh))));

            widgets.add(new Widget() {
                @Override
                public void draw(PoseStack stack) {
                    var helper = Minecraft.getInstance().screen;
                    RenderSystem.setShaderTexture(0, MachineScreen.SLOT_ATLAS);
                    for (int i = 1; i < area.w() / 2; i++) {
                        helper.blit(stack, area.x() + i, area.y() + 4, 0, 255, 1, 1);
                    }
                    for (int i = area.w() / 2; i < area.w() - 1; i++) {
                        double f = (i - area.w() / 2d) / (area.w() - area.w() / 2d);
                        int y = (int) ((1 - f) * (area.y() + 4) + f * (area.y() + area.h() - 14));
                        helper.blit(stack, area.x() + i, y, 0, 255, 1, 1);
                    }
                }
            });

            widgets.add(new Label(px1 - 2, area.y() + 2, Component.literal(String.format("%.1f", nuclearFuel.neutronMultiplicationFactor)))
                    .noShadow());

            widgets.add(new Label(px3 + 6, py - 10, Component.literal("0")).noShadow());
        }
    }

    private View getView(ThermalInteractionDisplay recipe) {
        if (recipe.type == ThermalInteractionDisplay.CategoryType.NEUTRON_EFFICIENCY) {
            return new NeutronEfficiencyView((NuclearFuel) recipe.nuclearComponent);
        } else if (recipe.type == ThermalInteractionDisplay.CategoryType.THERMAL_PROPERTIES) {
            return new ThermalPropertiesView(recipe.nuclearComponent);
        } else {
            throw new IllegalStateException("Unknown recipe type: " + recipe.type);
        }
    }

    private class ThermalPropertiesView extends View {

        public ThermalPropertiesView(INuclearComponent nuclearComponent) {
            super(nuclearComponent, MIText.ThermalInteraction.text());
            int centerX = background.getWidth() / 2;
            int centerY = background.getHeight() / 2;

            Component heatConduction = MIText.HeatConduction.text(
                    String.format("%.2f", nuclearComponent.getHeatConduction())).setStyle(TextHelper.HEAT_CONDUCTION);

            widgets.add(new Label(centerX, centerY, heatConduction).noShadow());

            int maxTemperature = nuclearComponent.getMaxTemperature();

            if (maxTemperature != Integer.MAX_VALUE) {
                Component maxTemp = MIText.MaxTemp.text(maxTemperature).setStyle(TextHelper.MAX_TEMP_TEXT);
                widgets.add(new Label(centerX, centerY + 12, maxTemp).noShadow());
            }

        }
    }

    abstract class View {
        protected final INuclearComponent nuclearComponent;
        private final int centerX;
        protected final List<Widget> widgets = new ArrayList<>();

        public View(INuclearComponent nuclearComponent, Component title) {
            this.nuclearComponent = nuclearComponent;

            centerX = background.getWidth() / 2;

            widgets.add(new Label(centerX, 8, title));
        }

        public void setupSlots(IRecipeLayoutBuilder builder) {
            int px = centerX - 9;
            int py = 22;

            var slot = builder.addSlot(RecipeIngredientRole.INPUT, px, py)
                    .setBackground(slotBackground, -1, -1);

            if (nuclearComponent.getVariant() instanceof ItemVariant itemVariant) {
                slot.addItemStack(itemVariant.toStack());
            } else if (nuclearComponent.getVariant() instanceof FluidVariant fluidVariant) {
                slot.addFluidStack(fluidVariant.getFluid(), FluidConstants.BUCKET, fluidVariant.copyNbt());
            }
        }

        public void draw(PoseStack stack) {
            for (var label : widgets) {
                label.draw(stack);
            }
        }
    }

}

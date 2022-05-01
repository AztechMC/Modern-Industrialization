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
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.compat.rei.ReiUtil;
import aztech.modern_industrialization.nuclear.NeutronInteraction;
import aztech.modern_industrialization.nuclear.NeutronType;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class NeutronInteractionCategory implements DisplayCategory<NeutronInteractionDisplay> {

    public static final ResourceLocation TEXTURE_ATLAS = new ResourceLocation(ModernIndustrialization.MOD_ID, "textures/gui/rei/texture_atlas.png");
    private static final ResourceLocation PROGRESS_BAR = new MIIdentifier("textures/gui/progress_bar/long_arrow.png");

    @Override
    public CategoryIdentifier<? extends NeutronInteractionDisplay> getCategoryIdentifier() {
        return NeutronInteractionPlugin.NEUTRON_CATEGORY;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Registry.ITEM.get(new MIIdentifier("uranium_fuel_rod")));
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("text.modern_industrialization.neutron_interaction");
    }

    public List<Widget> displayNeutronScattering(NeutronInteractionDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        int centerY = bounds.y + bounds.height / 2 - 5;
        int centerX = bounds.x + 66;

        if (display.nuclearComponent.getVariant() instanceof ItemVariant itemVariant) {
            widgets.add(Widgets.createSlot(new Point(centerX, centerY)).entry(EntryStacks.of(itemVariant.getItem())));
        } else if (display.nuclearComponent.getVariant() instanceof FluidVariant fluidVariant) {
            widgets.add(Widgets.createSlot(new Point(centerX, centerY)).entry(ReiUtil.createFluidEntryStack(fluidVariant.getFluid())));
        }

        Component title;
        NeutronType type;

        if (display.type == NeutronInteractionDisplay.CategoryType.FAST_NEUTRON_INTERACTION) {
            type = NeutronType.FAST;
            title = new TranslatableComponent("text.modern_industrialization.fast_neutron");

            widgets.add(Widgets.createTexturedWidget(TEXTURE_ATLAS, centerX - 53, centerY - 19, 0, 0, 88, 54));
        } else {
            type = NeutronType.THERMAL;
            title = new TranslatableComponent("text.modern_industrialization.thermal_neutron");
            widgets.add(Widgets.createTexturedWidget(TEXTURE_ATLAS, centerX - 53, centerY - 19, 0, 54, 88, 54));
        }

        double interactionProb = display.nuclearComponent.getNeutronBehaviour().interactionTotalProbability(type);
        double scattering = display.nuclearComponent.getNeutronBehaviour().interactionRelativeProbability(type, NeutronInteraction.SCATTERING);
        double absorption = display.nuclearComponent.getNeutronBehaviour().interactionRelativeProbability(type, NeutronInteraction.ABSORPTION);

        widgets.add(Widgets.createLabel(new Point(centerX + 10, centerY - 30), title));

        String scatteringString = String.format("%.1f ", 100 * interactionProb * scattering) + "%";
        String absorptionString = String.format("%.1f ", 100 * interactionProb * absorption) + "%";

        widgets.add(Widgets.createLabel(new Point(centerX - 20, centerY - 15), new TextComponent(scatteringString))
                .tooltip(new TranslatableComponent("text.modern_industrialization.scattering_probability").getString()));

        widgets.add(Widgets.createLabel(new Point(centerX + 30, centerY + 35), new TextComponent(absorptionString).setStyle(TextHelper.NEUTRONS))
                .noShadow().tooltip(new TranslatableComponent("text.modern_industrialization.absorption_probability").getString()));

        if (type == NeutronType.FAST) {
            double slowingProba = display.nuclearComponent.getNeutronBehaviour().neutronSlowingProbability();

            String thermalFractionString = String.format("%.1f ", 100 * slowingProba) + "%";
            String fastFractionString = String.format("%.1f ", 100 * (1 - slowingProba)) + "%";

            widgets.add(Widgets
                    .createLabel(new Point(centerX + 60, centerY + 20),
                            new TextComponent(fastFractionString).setStyle(Style.EMPTY.withColor(0xbc1a1a)))
                    .noShadow().tooltip(new TranslatableComponent("text.modern_industrialization.fast_neutron_fraction").getString()));

            widgets.add(Widgets
                    .createLabel(new Point(centerX + 60, centerY - 10),
                            new TextComponent(thermalFractionString).setStyle(Style.EMPTY.withColor(0x0c27a7)))
                    .noShadow().tooltip(new TranslatableComponent("text.modern_industrialization.thermal_neutron_fraction").getString()));

            int index = 1 + (int) Math.floor((slowingProba) * 9);
            if (slowingProba == 0) {
                index = 0;
            } else if (slowingProba == 1) {
                index = 10;
            }
            widgets.add(Widgets.createTexturedWidget(TEXTURE_ATLAS, centerX + 48, centerY, index * 16, 240, 16, 16));
        }

        return widgets;
    }

    public List<Widget> displayNeutronFission(NeutronInteractionDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        int centerY = bounds.y + bounds.height / 2 - 5;
        int centerX = bounds.x + 52;

        NuclearFuel fuel = (NuclearFuel) display.nuclearComponent;

        widgets.add(Widgets.createSlot(new Point(centerX, centerY)).entry(EntryStacks.of(fuel)));

        widgets.add(Widgets.createLabel(new Point(centerX + 20, centerY - 30),
                new TranslatableComponent("text.modern_industrialization.single_neutron_capture")));
        widgets.add(Widgets.createTexturedWidget(TEXTURE_ATLAS, centerX - 28, centerY - 7, 0, 109, 92, 31)

        );

        widgets.add(Widgets
                .createLabel(new Point(centerX + 20, centerY + 35),
                        new TranslatableComponent("text.modern_industrialization.neutrons_multiplication",
                                String.format("%.1f", fuel.neutronMultiplicationFactor)).setStyle(TextHelper.NEUTRONS))
                .noShadow().tooltip(new TranslatableComponent("text.modern_industrialization.neutron_temperature_variation").getString()));

        widgets.add(Widgets
                .createLabel(new Point(centerX - 18, centerY + 23),
                        new TextComponent(String.format("%d EU", NuclearConstant.EU_FOR_FAST_NEUTRON)).setStyle(TextHelper.NEUTRONS))
                .noShadow().tooltip(new TranslatableComponent("text.modern_industrialization.fast_neutron_energy").getString()));

        widgets.add(
                Widgets.createLabel(new Point(centerX + 55, centerY + 23), new TextComponent(String.format("%d EU", fuel.directEUbyDesintegration)))
                        .tooltip(new TranslatableComponent("text.modern_industrialization.direct_energy").getString()));

        widgets.add(Widgets
                .createLabel(new Point(centerX + 55, centerY - 12),
                        new TextComponent(String.format("%.2f °C", (double) fuel.directEUbyDesintegration / NuclearConstant.EU_PER_DEGREE)))
                .tooltip(new TranslatableComponent("text.modern_industrialization.direct_heat_by_desintegration").getString()));

        return widgets;
    }

    public List<Widget> displayNeutronProduct(NeutronInteractionDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        int centerY = bounds.y + bounds.height / 2 - 5;
        int centerX = bounds.x + 66;

        float probability = (float) display.nuclearComponent.getNeutronProductProbability();
        long amount = display.nuclearComponent.getNeutronProductAmount();

        int neutronNumber = 1;

        if (display.nuclearComponent.getVariant() instanceof ItemVariant itemVariant) {
            widgets.add(Widgets.createSlot(new Point(centerX - 35, centerY)).entry(EntryStacks.of(itemVariant.getItem())));
            ItemVariant product = (ItemVariant) display.nuclearComponent.getNeutronProduct();
            widgets.add(Widgets.createSlot(new Point(centerX + 35, centerY)).entry(EntryStacks.of(product.getItem(), (int) amount)));

            if (display.nuclearComponent instanceof NuclearFuel nuclearFuel) {
                neutronNumber = nuclearFuel.desintegrationMax;
            }

        } else if (display.nuclearComponent.getVariant() instanceof FluidVariant fluidVariant) {

            widgets.add(Widgets.createSlot(new Point(centerX - 35, centerY))
                    .entry(ReiUtil.createFluidEntryStack(fluidVariant.getFluid(), 1, probability)));
            FluidVariant product = (FluidVariant) display.nuclearComponent.getNeutronProduct();

            widgets.add(Widgets.createSlot(new Point(centerX + 35, centerY))
                    .entry(ReiUtil.createFluidEntryStack(product.getFluid(), amount, probability)));
        }

        Component title = new TranslatableComponent("text.modern_industrialization.neutron_absorption");
        widgets.add(Widgets.createLabel(new Point(centerX + 10, centerY - 30), title));

        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            RenderSystem.setShaderTexture(0, PROGRESS_BAR);
            int posX = centerX - 12;
            int posY = centerY - 2;

            GuiComponent.blit(matrices, posX, posY, helper.getBlitOffset(), 0, 0, 40, 20, 40, 40);

            GuiComponent.blit(matrices, posX, posY, helper.getBlitOffset(), 0, 20, (int) (40 * (System.currentTimeMillis() % 3000) / 3000d), 20,
                    40, 40);
        }));

        Component neutronNumberText;
        if (neutronNumber > 1) {
            neutronNumberText = new TranslatableComponent("text.modern_industrialization.neutrons", neutronNumber);
        } else {
            neutronNumberText = new TranslatableComponent("text.modern_industrialization.neutron", neutronNumber);
        }

        widgets.add(Widgets.createLabel(new Point(centerX + 10, centerY + 20), neutronNumberText));

        return widgets;
    }

    @Override
    public List<Widget> setupDisplay(NeutronInteractionDisplay display, Rectangle bounds) {

        if (display.type == NeutronInteractionDisplay.CategoryType.FISSION) {
            return displayNeutronFission(display, bounds);

        } else if (display.type == NeutronInteractionDisplay.CategoryType.NEUTRON_PRODUCT) {
            return displayNeutronProduct(display, bounds);
        } else {
            return displayNeutronScattering(display, bounds);
        }

    }

    @Override
    public int getDisplayHeight() {
        return 90;
    }

}

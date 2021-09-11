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
import aztech.modern_industrialization.nuclear.NeutronInteraction;
import aztech.modern_industrialization.nuclear.NeutronType;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.util.TextHelper;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

class NeutronInteractionCategory implements DisplayCategory<NeutronInteractionDisplay> {

    public static final Identifier TEXTURE_ATLAS = new Identifier(ModernIndustrialization.MOD_ID, "textures/gui/rei/texture_atlas.png");

    @Override
    public CategoryIdentifier<? extends NeutronInteractionDisplay> getCategoryIdentifier() {
        return NeutronInteractionPlugin.CATEGORY;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Registry.ITEM.get(new MIIdentifier("uranium_fuel_rod")));
    }

    @Override
    public Text getTitle() {
        return new TranslatableText("text.modern_industrialization.neutron_interaction");
    }

    @Override
    public List<Widget> setupDisplay(NeutronInteractionDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        int centerY = bounds.y + bounds.height / 2 - 5;
        widgets.add(Widgets.createRecipeBase(bounds));

        if (display.type == NeutronInteractionDisplay.CategoryType.FISSION) {
            NuclearFuel fuel = (NuclearFuel) display.nuclearComponent;
            int centerX = bounds.x + 52;

            widgets.add(Widgets.createSlot(new Point(centerX, centerY)).entry(EntryStacks.of(display.nuclearComponent)));

            widgets.add(Widgets.createLabel(new Point(centerX + 20, centerY - 30),
                    new TranslatableText("text.modern_industrialization.single_neutron_capture")));
            widgets.add(Widgets.createTexturedWidget(TEXTURE_ATLAS, centerX - 28, centerY - 7, 0, 109, 92, 31));

            widgets.add(Widgets
                    .createLabel(new Point(centerX + 20, centerY + 35), new TranslatableText("text.modern_industrialization.neutrons_multiplication",
                            String.format("%.1f", fuel.neutronMultiplicationFactor)).setStyle(TextHelper.NEUTRONS))
                    .noShadow());

            widgets.add(Widgets
                    .createLabel(new Point(centerX - 18, centerY + 23),
                            new LiteralText(String.format("%d EU", NuclearConstant.EU_FOR_FAST_NEUTRON)).setStyle(TextHelper.NEUTRONS))
                    .noShadow().tooltipLine(new TranslatableText("text.modern_industrialization.fast_neutron_energy").getString()));

            widgets.add(
                    Widgets.createLabel(new Point(centerX + 55, centerY + 23), new LiteralText(String.format("%d EU", fuel.directEUbyDesintegration)))
                            .tooltipLine(new TranslatableText("text.modern_industrialization.direct_energy").getString()));

        } else {
            int centerX = bounds.x + 66;

            widgets.add(Widgets.createSlot(new Point(centerX, centerY)).entry(EntryStacks.of(display.nuclearComponent)));

            Text title;
            NeutronType type;

            if (display.type == NeutronInteractionDisplay.CategoryType.FAST_NEUTRON_INTERACTION) {
                type = NeutronType.FAST;
                title = new TranslatableText("text.modern_industrialization.fast_neutron");

                widgets.add(Widgets.createTexturedWidget(TEXTURE_ATLAS, centerX - 53, centerY - 19, 0, 0, 88, 54));
            } else {
                type = NeutronType.THERMAL;
                title = new TranslatableText("text.modern_industrialization.thermal_neutron");
                widgets.add(Widgets.createTexturedWidget(TEXTURE_ATLAS, centerX - 53, centerY - 19, 0, 54, 88, 54));
            }

            double interactionProb = display.nuclearComponent.neutronBehaviour.interactionTotalProbability(type);
            double scattering = display.nuclearComponent.neutronBehaviour.interactionRelativeProbability(type, NeutronInteraction.SCATTERING);
            double absorption = display.nuclearComponent.neutronBehaviour.interactionRelativeProbability(type, NeutronInteraction.ABSORPTION);

            widgets.add(Widgets.createLabel(new Point(centerX + 10, centerY - 30), title));

            String scatteringString = String.format("%.1f ", 100 * interactionProb * scattering) + "%";
            String absorptionString = String.format("%.1f ", 100 * interactionProb * absorption) + "%";

            widgets.add(Widgets.createLabel(new Point(centerX - 20, centerY - 15), new LiteralText(scatteringString))
                    .tooltipLine(new TranslatableText("text.modern_industrialization.scattering_probability").getString()));

            widgets.add(Widgets.createLabel(new Point(centerX + 30, centerY + 35), new LiteralText(absorptionString).setStyle(TextHelper.NEUTRONS))
                    .noShadow().tooltipLine(new TranslatableText("text.modern_industrialization.absorption_probability").getString()));

            if (type == NeutronType.FAST) {
                double slowingProba = display.nuclearComponent.neutronBehaviour.neutronSlowingProbability();

                String thermalFractionString = String.format("%.1f ", 100 * slowingProba) + "%";
                String fastFractionString = String.format("%.1f ", 100 * (1 - slowingProba)) + "%";

                widgets.add(Widgets
                        .createLabel(new Point(centerX + 60, centerY + 20),
                                new LiteralText(fastFractionString).setStyle(Style.EMPTY.withColor(0xbc1a1a)))
                        .noShadow().tooltipLine(new TranslatableText("text.modern_industrialization.fast_neutron_fraction").getString()));

                widgets.add(Widgets
                        .createLabel(new Point(centerX + 60, centerY - 10),
                                new LiteralText(thermalFractionString).setStyle(Style.EMPTY.withColor(0x0c27a7)))
                        .noShadow().tooltipLine(new TranslatableText("text.modern_industrialization.thermal_neutron_fraction").getString()));

                int index = 1 + (int) Math.floor((slowingProba) * 9);
                if (slowingProba == 0) {
                    index = 0;
                } else if (slowingProba == 1) {
                    index = 10;
                }
                widgets.add(Widgets.createTexturedWidget(TEXTURE_ATLAS, centerX + 48, centerY, index * 16, 240, 16, 16));
            }

        }
        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 90;
    }

}

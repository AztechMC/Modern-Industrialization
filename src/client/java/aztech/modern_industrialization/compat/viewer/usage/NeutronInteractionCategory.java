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
import aztech.modern_industrialization.machines.guicomponents.NuclearReactorGuiClient;
import aztech.modern_industrialization.nuclear.FluidNuclearComponent;
import aztech.modern_industrialization.nuclear.INeutronBehaviour;
import aztech.modern_industrialization.nuclear.INuclearComponent;
import aztech.modern_industrialization.nuclear.NeutronInteraction;
import aztech.modern_industrialization.nuclear.NeutronType;
import aztech.modern_industrialization.nuclear.NuclearComponentItem;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.TextHelper;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class NeutronInteractionCategory extends ViewerCategory<NeutronInteractionCategory.Recipe> {
    public static final ResourceLocation TEXTURE_ATLAS = NuclearReactorGuiClient.TEXTURE_ATLAS;
    private static final ResourceLocation PROGRESS_BAR = new MIIdentifier("textures/gui/progress_bar/long_arrow.png");

    private final int centerX;
    private final int centerY;

    public NeutronInteractionCategory() {
        super(Recipe.class, new MIIdentifier("neutron_interaction"), MIText.NeutronInteraction.text(),
                BuiltInRegistries.ITEM.get(new MIIdentifier("uranium_fuel_rod")).getDefaultInstance(), 150, 90);

        this.centerX = width / 2;
        this.centerY = height / 2 - 5;
    }

    @Override
    public void buildWorkstations(WorkstationConsumer consumer) {
        consumer.accept("nuclear_reactor");
    }

    @Override
    public void buildRecipes(RecipeManager recipeManager, RegistryAccess registryAccess, Consumer<Recipe> consumer) {
        BuiltInRegistries.ITEM.stream().filter(item -> item instanceof NuclearComponentItem).forEach(item -> {
            NuclearComponentItem component = (NuclearComponentItem) item;
            if (component.neutronBehaviour != INeutronBehaviour.NO_INTERACTION) {
                consumer.accept(new Recipe(component, CategoryType.FAST_NEUTRON_INTERACTION));
                consumer.accept(new Recipe(component, CategoryType.THERMAL_NEUTRON_INTERACTION));
            }

            if (item instanceof NuclearFuel) {
                consumer.accept(new Recipe(component, CategoryType.FISSION));
            }

            ItemVariant product = component.getNeutronProduct();
            if (product != null) {
                consumer.accept(new Recipe(component, CategoryType.NEUTRON_PRODUCT));
            }

        });

        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
                INuclearComponent<?> component = FluidNuclearComponent.get(fluid);
                if (component != null) {
                    consumer.accept(new Recipe(component, CategoryType.FAST_NEUTRON_INTERACTION));
                    consumer.accept(new Recipe(component, CategoryType.THERMAL_NEUTRON_INTERACTION));

                    if (component.getVariant() != null) {
                        consumer.accept(new Recipe(component, CategoryType.NEUTRON_PRODUCT));
                    }
                }
            }
        }
    }

    @Override
    public void buildLayout(Recipe recipe, LayoutBuilder builder) {
        switch (recipe.type) {
        case FISSION -> {
            builder.inputSlot(52, centerY).variant(recipe.nuclearComponent.getVariant());
        }
        case NEUTRON_PRODUCT -> {
            long amount = recipe.nuclearComponent.getNeutronProductAmount();
            int baseX = 66;

            if (recipe.nuclearComponent.getVariant() instanceof ItemVariant itemVariant) {
                builder.inputSlot(baseX - 35, centerY).item(itemVariant.toStack());
                ItemVariant product = (ItemVariant) recipe.nuclearComponent.getNeutronProduct();
                builder.outputSlot(baseX + 35, centerY).item(product.toStack((int) amount));
            } else if (recipe.nuclearComponent.getVariant() instanceof FluidVariant fluidVariant) {
                float probability = (float) recipe.nuclearComponent.getNeutronProductProbability();
                builder.inputSlot(baseX - 35, centerY).fluid(fluidVariant, 1, probability);
                FluidVariant product = (FluidVariant) recipe.nuclearComponent.getNeutronProduct();
                builder.outputSlot(baseX + 35, centerY).fluid(product, amount, probability);
            }
        }
        case FAST_NEUTRON_INTERACTION, THERMAL_NEUTRON_INTERACTION -> {
            builder.inputSlot(66, centerY).variant(recipe.nuclearComponent.getVariant());
        }
        }
    }

    @Override
    public void buildWidgets(Recipe recipe, WidgetList widgets) {
        switch (recipe.type) {
        case FISSION -> {
            int centerX = 52;

            NuclearFuel fuel = (NuclearFuel) recipe.nuclearComponent;
            widgets.text(MIText.SingleNeutronCapture.text(), this.centerX, centerY - 34, TextAlign.CENTER, false, true, null);
            widgets.texture(TEXTURE_ATLAS, centerX - 28, centerY - 7, 0, 109, 92, 31);

            widgets.text(
                    MIText.NeutronsMultiplication.text(String.format("%.1f", fuel.neutronMultiplicationFactor)).setStyle(TextHelper.NEUTRONS),
                    this.centerX, centerY + 35, TextAlign.CENTER, false, true,
                    MIText.NeutronTemperatureVariation.text());

            widgets.text(
                    Component.literal(String.format("%d EU", NuclearConstant.EU_FOR_FAST_NEUTRON)).setStyle(TextHelper.NEUTRONS),
                    centerX - 18, centerY + 23, TextAlign.CENTER, false, false,
                    MIText.FastNeutronEnergy.text());

            widgets.text(
                    Component.literal(String.format("%d EU", fuel.directEUbyDesintegration)),
                    centerX + 55, centerY + 23, TextAlign.CENTER, false, true,
                    MIText.DirectEnergy.text());

            widgets.text(
                    Component.literal(String.format("%.2f °C", (double) fuel.directEUbyDesintegration / NuclearConstant.EU_PER_DEGREE)),
                    centerX + 55, centerY - 12, TextAlign.CENTER, false, true,
                    MIText.DirectHeatByDesintegration.text());
        }
        case NEUTRON_PRODUCT -> {
            int centerX = 66;

            int neutronNumber = 1;

            if (recipe.nuclearComponent instanceof NuclearFuel nuclearFuel) {
                neutronNumber = nuclearFuel.desintegrationMax;
            }

            widgets.text(MIText.NeutronAbsorption.text(), this.centerX, centerY - 34, TextAlign.CENTER, false, true, null);

            widgets.drawable(guiGraphics -> {
                int posX = centerX - 12;
                int posY = centerY - 2;

                guiGraphics.blit(PROGRESS_BAR, posX, posY, 0, 0, 40, 20, 40, 40);

                guiGraphics.blit(PROGRESS_BAR, posX, posY, 0, 20, (int) (40 * (System.currentTimeMillis() % 3000) / 3000d), 20, 40, 40);
            });

            Component neutronNumberText;
            if (neutronNumber > 1) {
                neutronNumberText = MIText.Neutrons.text(neutronNumber);
            } else {
                neutronNumberText = MIText.Neutron.text(neutronNumber);
            }

            widgets.text(neutronNumberText, this.centerX, centerY + 20, TextAlign.CENTER, false, true, null);
        }
        case FAST_NEUTRON_INTERACTION, THERMAL_NEUTRON_INTERACTION -> {
            int centerX = 66;

            Component title;
            NeutronType type;

            if (recipe.type == CategoryType.FAST_NEUTRON_INTERACTION) {
                type = NeutronType.FAST;
                title = MIText.FastNeutron.text();
                widgets.texture(TEXTURE_ATLAS, centerX - 53, centerY - 19, 0, 0, 88, 54);
            } else {
                type = NeutronType.THERMAL;
                title = MIText.ThermalNeutron.text();
                widgets.texture(TEXTURE_ATLAS, centerX - 53, centerY - 19, 0, 54, 88, 54);
            }

            double interactionProb = recipe.nuclearComponent.getNeutronBehaviour().interactionTotalProbability(type);
            double scattering = recipe.nuclearComponent.getNeutronBehaviour().interactionRelativeProbability(type, NeutronInteraction.SCATTERING);
            double absorption = recipe.nuclearComponent.getNeutronBehaviour().interactionRelativeProbability(type, NeutronInteraction.ABSORPTION);

            widgets.text(title, this.centerX, centerY - 34, TextAlign.CENTER, false, true, null);

            String scatteringString = String.format("%.1f ", 100 * interactionProb * scattering) + "%";
            String absorptionString = String.format("%.1f ", 100 * interactionProb * absorption) + "%";

            widgets.text(
                    Component.literal(scatteringString),
                    centerX - 20, centerY - 15, TextAlign.CENTER, false, true,
                    MIText.ScatteringProbability.text());

            widgets.text(
                    Component.literal(absorptionString).setStyle(TextHelper.NEUTRONS),
                    centerX + 30, centerY + 35, TextAlign.CENTER, false, false,
                    MIText.AbsorptionProbability.text());

            if (type == NeutronType.FAST) {
                double slowingProba = recipe.nuclearComponent.getNeutronBehaviour().neutronSlowingProbability();

                String thermalFractionString = String.format("%.1f ", 100 * slowingProba) + "%";
                String fastFractionString = String.format("%.1f ", 100 * (1 - slowingProba)) + "%";

                widgets.text(
                        Component.literal(fastFractionString).setStyle(Style.EMPTY.withColor(0xbc1a1a)),
                        centerX + 60, centerY + 20, TextAlign.CENTER, false, false,
                        MIText.FastNeutronFraction.text());

                widgets.text(
                        Component.literal(thermalFractionString).setStyle(Style.EMPTY.withColor(0x0c27a7)),
                        centerX + 60, centerY - 10, TextAlign.CENTER, false, false,
                        MIText.ThermalNeutronFraction.text());

                int index = 1 + (int) Math.floor((slowingProba) * 9);
                if (slowingProba == 0) {
                    index = 0;
                } else if (slowingProba == 1) {
                    index = 10;
                }
                widgets.texture(TEXTURE_ATLAS, centerX + 48, centerY, index * 16, 240, 16, 16);
            }
        }
        }
    }

    public record Recipe(INuclearComponent<?> nuclearComponent, CategoryType type) {
    }

    public enum CategoryType {
        FAST_NEUTRON_INTERACTION,
        THERMAL_NEUTRON_INTERACTION,
        FISSION,
        NEUTRON_PRODUCT,
    }
}

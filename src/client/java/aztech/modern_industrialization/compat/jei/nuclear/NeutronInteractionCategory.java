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
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.compat.jei.DrawableWidget;
import aztech.modern_industrialization.compat.jei.JeiUtil;
import aztech.modern_industrialization.compat.jei.Label;
import aztech.modern_industrialization.compat.jei.Widget;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.nuclear.NeutronInteraction;
import aztech.modern_industrialization.nuclear.NeutronType;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
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
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class NeutronInteractionCategory implements IRecipeCategory<NeutronInteractionDisplay> {

    public static final ResourceLocation TEXTURE_ATLAS = new ResourceLocation(ModernIndustrialization.MOD_ID, "textures/gui/rei/texture_atlas.png");
    private static final ResourceLocation PROGRESS_BAR = new MIIdentifier("textures/gui/progress_bar/long_arrow.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable itemSlotBackground;
    private final IDrawable fluidSlotBackground;
    private final IDrawableStatic fastNeutron;
    private final IDrawableStatic thermalNeutron;
    private final IGuiHelper guiHelper;
    private final IDrawable[] slowingDrawable;
    private final IDrawable singleNeutronCapture;

    public NeutronInteractionCategory(IJeiHelpers helpers) {
        guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(150, 90);
        this.icon = guiHelper.createDrawableItemStack(Registry.ITEM.get(new MIIdentifier("uranium_fuel_rod")).getDefaultInstance());
        this.itemSlotBackground = guiHelper.getSlotDrawable();
        this.fluidSlotBackground = guiHelper.createDrawable(MachineScreen.SLOT_ATLAS, 18, 0, 18, 18);

        this.fastNeutron = guiHelper.createDrawable(TEXTURE_ATLAS, 0, 0, 88, 54);
        this.thermalNeutron = guiHelper.createDrawable(TEXTURE_ATLAS, 0, 54, 88, 54);

        slowingDrawable = new IDrawable[11];
        for (int i = 0; i < slowingDrawable.length; i++) {
            slowingDrawable[i] = guiHelper.createDrawable(TEXTURE_ATLAS, i * 16, 240, 16, 16);
        }
        singleNeutronCapture = guiHelper.createDrawable(TEXTURE_ATLAS, 0, 109, 92, 31);
    }

    @Override
    public RecipeType<NeutronInteractionDisplay> getRecipeType() {
        return NeutronInteractionPlugin.NEUTRON_CATEGORY;
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
    public void setRecipe(IRecipeLayoutBuilder builder, NeutronInteractionDisplay recipe, IFocusGroup focuses) {
        getView(recipe).buildSlots(builder);
    }

    @Override
    public void draw(NeutronInteractionDisplay recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        getView(recipe).draw(stack);
    }

    @Override
    public List<Component> getTooltipStrings(NeutronInteractionDisplay recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return getView(recipe).getTooltipStrings(mouseX, mouseY);
    }

    @Override
    public Component getTitle() {
        return MIText.NeutronInteraction.text();
    }

    private View getView(NeutronInteractionDisplay recipe) {
        return switch (recipe.type) {
        case FISSION -> new NeutronFissionView(recipe);
        case NEUTRON_PRODUCT -> new NeutronProductView(recipe);
        default -> new NeutronScatteringView(recipe);
        };
    }

    class NeutronFissionView extends View {

        private final int centerX;
        private final int centerY;
        private final NuclearFuel fuel;

        public NeutronFissionView(NeutronInteractionDisplay recipe) {
            super(recipe);

            centerY = background.getHeight() / 2 - 5;
            centerX = 52;

            fuel = (NuclearFuel) recipe.nuclearComponent;

            widgets.add(new Label(centerX + 20, centerY - 30, MIText.SingleNeutronCapture.text()));
            widgets.add(new DrawableWidget(singleNeutronCapture, centerX - 28, centerY - 7));

            widgets.add(new Label(centerX + 20, centerY + 35,
                    MIText.NeutronsMultiplication.text(String.format("%.1f", fuel.neutronMultiplicationFactor)).setStyle(TextHelper.NEUTRONS))
                            .noShadow().tooltip(MIText.NeutronTemperatureVariation.text()));

            widgets.add(new Label(centerX - 18, centerY + 23,
                    Component.literal(String.format("%d EU", NuclearConstant.EU_FOR_FAST_NEUTRON)).setStyle(TextHelper.NEUTRONS))
                            .noShadow().tooltip(MIText.FastNeutronEnergy.text()));

            widgets.add(
                    new Label(centerX + 55, centerY + 23, Component.literal(String.format("%d EU", fuel.directEUbyDesintegration)))
                            .tooltip(MIText.DirectEnergy.text()));

            widgets.add(new Label(centerX + 55, centerY - 12,
                    Component.literal(String.format("%.2f °C", (double) fuel.directEUbyDesintegration / NuclearConstant.EU_PER_DEGREE)))
                            .tooltip(MIText.DirectHeatByDesintegration.text()));

        }

        @Override
        public void buildSlots(IRecipeLayoutBuilder builder) {
            builder.addSlot(RecipeIngredientRole.INPUT, centerX, centerY)
                    .setBackground(itemSlotBackground, -1, -1)
                    .addItemStack(fuel.getDefaultInstance());
        }
    }

    class NeutronProductView extends View {

        private final int centerY;
        private final int centerX;

        public NeutronProductView(NeutronInteractionDisplay recipe) {
            super(recipe);
            centerX = 66;
            centerY = background.getHeight() / 2 - 5;

            int neutronNumber = 1;

            if (recipe.nuclearComponent.getVariant() instanceof ItemVariant) {
                if (recipe.nuclearComponent instanceof NuclearFuel nuclearFuel) {
                    neutronNumber = nuclearFuel.desintegrationMax;
                }
            }

            Component title = MIText.NeutronAbsorption.text();
            widgets.add(new Label(centerX + 10, centerY - 30, title));

            widgets.add(stack -> {
                RenderSystem.setShaderTexture(0, PROGRESS_BAR);
                int posX = centerX - 12;
                int posY = centerY - 2;

                var helper = Minecraft.getInstance().screen;
                GuiComponent.blit(stack, posX, posY, helper.getBlitOffset(), 0, 0, 40, 20, 40, 40);

                GuiComponent.blit(stack, posX, posY, helper.getBlitOffset(), 0, 20, (int) (40 * (System.currentTimeMillis() % 3000) / 3000d), 20,
                        40, 40);
            });

            Component neutronNumberText;
            if (neutronNumber > 1) {
                neutronNumberText = MIText.Neutrons.text(neutronNumber);
            } else {
                neutronNumberText = MIText.Neutron.text(neutronNumber);
            }

            widgets.add(new Label(centerX + 10, centerY + 20, neutronNumberText));

        }

        @Override
        public void buildSlots(IRecipeLayoutBuilder builder) {
            long amount = recipe.nuclearComponent.getNeutronProductAmount();
            float probability = (float) recipe.nuclearComponent.getNeutronProductProbability();

            IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, centerX - 35, centerY);
            IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, centerX + 35, centerY);
            if (recipe.nuclearComponent.getVariant() instanceof ItemVariant itemVariant) {
                inputSlot.setBackground(itemSlotBackground, -1, -1)
                        .addItemStack(itemVariant.getItem().getDefaultInstance());

                ItemVariant product = (ItemVariant) recipe.nuclearComponent.getNeutronProduct();
                outputSlot.setBackground(itemSlotBackground, -1, -1)
                        .addItemStack(new ItemStack(product.getItem(), (int) amount));

            } else if (recipe.nuclearComponent.getVariant() instanceof FluidVariant fluidVariant) {

                inputSlot.setBackground(fluidSlotBackground, -1, -1)
                        .addFluidStack(fluidVariant.getFluid(), FluidConstants.DROPLET, fluidVariant.copyNbt());

                FluidVariant product = (FluidVariant) recipe.nuclearComponent.getNeutronProduct();

                outputSlot.setBackground(fluidSlotBackground, -1, -1)
                        .addFluidStack(product.getFluid(), amount, product.copyNbt());
            }

            JeiUtil.customizeTooltip(inputSlot, probability);
            JeiUtil.customizeTooltip(outputSlot, probability);
        }
    }

    class NeutronScatteringView extends View {

        private final int centerY;
        private final int centerX;

        public NeutronScatteringView(NeutronInteractionDisplay recipe) {
            super(recipe);

            centerY = background.getHeight() / 2 - 5;
            centerX = 66;

            Component title;
            NeutronType type;

            if (recipe.type == NeutronInteractionDisplay.CategoryType.FAST_NEUTRON_INTERACTION) {
                type = NeutronType.FAST;
                title = MIText.FastNeutron.text();

                widgets.add(new DrawableWidget(fastNeutron, centerX - 53, centerY - 19));
            } else {
                type = NeutronType.THERMAL;
                title = MIText.ThermalNeutron.text();
                widgets.add(new DrawableWidget(thermalNeutron, centerX - 53, centerY - 19));
            }

            double interactionProb = recipe.nuclearComponent.getNeutronBehaviour().interactionTotalProbability(type);
            double scattering = recipe.nuclearComponent.getNeutronBehaviour().interactionRelativeProbability(type, NeutronInteraction.SCATTERING);
            double absorption = recipe.nuclearComponent.getNeutronBehaviour().interactionRelativeProbability(type, NeutronInteraction.ABSORPTION);

            widgets.add(new Label(centerX + 10, centerY - 30, title));

            String scatteringString = String.format("%.1f ", 100 * interactionProb * scattering) + "%";
            String absorptionString = String.format("%.1f ", 100 * interactionProb * absorption) + "%";

            widgets.add(new Label(centerX - 20, centerY - 15, Component.literal(scatteringString))
                    .tooltip(MIText.ScatteringProbability.text()));

            widgets.add(new Label(centerX + 30, centerY + 35, Component.literal(absorptionString).setStyle(TextHelper.NEUTRONS))
                    .noShadow().tooltip(MIText.AbsorptionProbability.text()));

            if (type == NeutronType.FAST) {
                double slowingProba = recipe.nuclearComponent.getNeutronBehaviour().neutronSlowingProbability();

                String thermalFractionString = String.format("%.1f ", 100 * slowingProba) + "%";
                String fastFractionString = String.format("%.1f ", 100 * (1 - slowingProba)) + "%";

                widgets.add(new Label(centerX + 60, centerY + 20,
                        Component.literal(fastFractionString).setStyle(Style.EMPTY.withColor(0xbc1a1a)))
                                .noShadow().tooltip(MIText.FastNeutronFraction.text()));

                widgets.add(new Label(centerX + 60, centerY - 10,
                        Component.literal(thermalFractionString).setStyle(Style.EMPTY.withColor(0x0c27a7)))
                                .noShadow().tooltip(MIText.ThermalNeutronFraction.text()));

                int index = 1 + (int) Math.floor((slowingProba) * 9);
                if (slowingProba == 0) {
                    index = 0;
                } else if (slowingProba == 1) {
                    index = 10;
                }
                widgets.add(new DrawableWidget(slowingDrawable[index], centerX + 48, centerY));
            }
        }

        @Override
        public void buildSlots(IRecipeLayoutBuilder builder) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, centerX, centerY);
            if (recipe.nuclearComponent.getVariant() instanceof ItemVariant itemVariant) {
                slot.setBackground(itemSlotBackground, -1, -1);
                slot.addItemStack(itemVariant.toStack());
            } else if (recipe.nuclearComponent.getVariant() instanceof FluidVariant fluidVariant) {
                slot.setBackground(fluidSlotBackground, -1, -1);
                slot.addFluidStack(fluidVariant.getFluid(), FluidConstants.BUCKET, fluidVariant.copyNbt());
            }
            JeiUtil.customizeTooltip(slot);
        }
    }

    abstract static class View {
        protected final NeutronInteractionDisplay recipe;
        protected final List<Widget> widgets = new ArrayList<>();

        public View(NeutronInteractionDisplay recipe) {
            this.recipe = recipe;
        }

        public void buildSlots(IRecipeLayoutBuilder builder) {
        }

        public void draw(PoseStack stack) {
            for (var widget : widgets) {
                widget.draw(stack);
            }
        }

        public List<Component> getTooltipStrings(double mouseX, double mouseY) {
            for (int i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.hitTest(mouseX, mouseY)) {
                    var lines = widget.getTooltipLines();
                    if (!lines.isEmpty()) {
                        return lines;
                    }
                }
            }
            return List.of();
        }
    }

}

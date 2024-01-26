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
package aztech.modern_industrialization.machines.guicomponents;

import static aztech.modern_industrialization.nuclear.NeutronType.*;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.blockentities.hatches.NuclearHatch;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.nuclear.INuclearComponent;
import aztech.modern_industrialization.nuclear.INuclearTileData;
import aztech.modern_industrialization.nuclear.NeutronType;
import aztech.modern_industrialization.nuclear.NuclearComponentItem;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.proxy.CommonProxy;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class NuclearReactorGuiClient implements GuiComponentClient {
    public static final ResourceLocation TEXTURE_ATLAS = MI.id("textures/gui/rei/texture_atlas.png");

    private NuclearReactorGui.Data data;

    public NuclearReactorGuiClient(FriendlyByteBuf buf) {
        readCurrentData(buf);
    }

    @Override
    public void readCurrentData(FriendlyByteBuf buf) {
        boolean valid = buf.readBoolean();
        if (valid) {
            int sizeX = buf.readInt();
            int sizeY = buf.readInt();
            Optional<INuclearTileData>[] tilesData = new Optional[sizeX * sizeY];

            for (int i = 0; i < sizeX * sizeY; i++) {
                tilesData[i] = INuclearTileData.read(buf);
            }
            double euProduction = buf.readDouble();
            double euFuelConsumption = buf.readDouble();
            data = new NuclearReactorGui.Data(true, sizeX, sizeY, tilesData, euProduction, euFuelConsumption);

        } else {
            data = new NuclearReactorGui.Data(false, 0, 0, null, 0, 0);
        }
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    public class Renderer implements ClientComponentRenderer {

        final int centerX = 88, centerY = 88;
        Renderer.Mode currentMode = Renderer.Mode.NUCLEAR_FUEL;
        NeutronType neutronMode = BOTH;

        ItemStack fuelStack = new ItemStack(BuiltInRegistries.ITEM.get(new MIIdentifier("uranium_fuel_rod")), 1);

        private final ResourceLocation COLORBAR = new MIIdentifier("textures/gui/colorbar.png");

        private enum Mode {

            NUCLEAR_FUEL(0),
            TEMPERATURE(1),
            NEUTRON_ABSORPTION(2),
            NEUTRON_FLUX(3),
            NEUTRON_GENERATION(4),
            EU_GENERATION(5);

            final int index;

            Mode(int index) {
                this.index = index;
            }

        }

        Component[] modeTooltip = new Component[] { MIText.NuclearFuelMode.text(), MIText.TemperatureMode.text(), MIText.NeutronAbsorptionMode.text(),
                MIText.NeutronFluxMode.text(), MIText.NeutronGenerationMode.text(), MIText.EuGenerationMode.text() };

        Component[] neutronModeTooltip = new Component[] { MIText.FastNeutron.text(), MIText.ThermalNeutron.text(), MIText.Both.text() };

        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {

            if (data.valid()) {
                for (int i = 0; i < data.gridSizeX(); i++) {
                    for (int j = 0; j < data.gridSizeY(); j++) {
                        int index = data.toIndex(i, j);
                        Optional<INuclearTileData> tile = data.tilesData()[index];
                        if (tile.isPresent()) {
                            INuclearTileData tileData = tile.get();
                            int px = x + centerX - data.gridSizeX() * 9 + i * 18;
                            int py = y + centerY - data.gridSizeY() * 9 + j * 18;

                            if (tileData.isFluid()) {
                                guiGraphics.blit(MachineScreen.SLOT_ATLAS, px, py, 18, 0, 18, 18);
                            } else {
                                guiGraphics.blit(MachineScreen.SLOT_ATLAS, px, py, 0, 0, 18, 18);
                            }

                            TransferVariant<?> variant = tile.get().getVariant();
                            long variantAmount = tile.get().getVariantAmount();

                            if (variantAmount > 0 & !variant.isBlank()) {
                                if (variant instanceof ItemVariant itemVariant) {
                                    var stack = itemVariant.toStack((int) variantAmount);
                                    RenderHelper.renderAndDecorateItem(guiGraphics, Minecraft.getInstance().font, stack, px + 1, py + 1);
                                } else if (variant instanceof FluidVariant fluidVariant) {
                                    RenderSystem.disableBlend();
                                    RenderHelper.drawFluidInGui(guiGraphics, fluidVariant, px + 1, py + 1);
                                }
                            }

                            if (currentMode != Renderer.Mode.NUCLEAR_FUEL) {
                                int u;
                                int v;

                                if (currentMode == Renderer.Mode.TEMPERATURE) {
                                    v = 30;
                                    u = (int) (299 * tileData.getTemperature() / NuclearConstant.MAX_TEMPERATURE);
                                } else if (currentMode == Renderer.Mode.EU_GENERATION) {
                                    v = 30;
                                    u = (int) (299 * Math.min(1.0, tileData.getMeanEuGeneration() / (2 * NuclearConstant.MAX_HATCH_EU_PRODUCTION)));
                                } else {
                                    double neutronRate;
                                    double factor = neutronMode == BOTH ? 2 : 1;
                                    if (currentMode == Renderer.Mode.NEUTRON_ABSORPTION) {
                                        neutronRate = 5 * tileData.getMeanNeutronAbsorption(neutronMode);
                                    } else if (currentMode == Renderer.Mode.NEUTRON_FLUX) {
                                        neutronRate = tileData.getMeanNeutronFlux(neutronMode);
                                    } else if (currentMode == Renderer.Mode.NEUTRON_GENERATION) {
                                        if (neutronMode == THERMAL) {
                                            neutronRate = 0;
                                        } else {
                                            neutronRate = tileData.getMeanNeutronGeneration();
                                            factor = 1;
                                        }
                                    } else {
                                        neutronRate = 0;
                                    }
                                    v = 0;
                                    u = (int) (299 * NuclearReactorGui.neutronColorScheme(factor * neutronRate));
                                }

                                RenderSystem.disableDepthTest();
                                RenderSystem.enableBlend();

                                guiGraphics.pose().translate(px, py, 0);
                                guiGraphics.pose().scale(18, 18, 1);
                                guiGraphics.blit(COLORBAR, 0, 0, u, v, 1, 1, 300, 60);
                                guiGraphics.pose().scale(1 / 18f, 1 / 18f, 1);
                                guiGraphics.pose().translate(-px, -py, 0);

                            }

                            if (currentMode == Renderer.Mode.TEMPERATURE) {
                                if (!variant.isBlank() && variant instanceof ItemVariant itemVariant) {
                                    if (itemVariant.getItem() instanceof NuclearComponentItem item) {
                                        if (tileData.getTemperature() + 100 > item.getMaxTemperature()) {
                                            RenderSystem.enableBlend();
                                            RenderSystem.disableDepthTest();
                                            if (System.currentTimeMillis() % 1000 > 500) {
                                                guiGraphics.blit(MachineScreen.SLOT_ATLAS, px + 1, py + 1, 22, 58, 16, 16);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (data.euFuelConsumption() > 0 && currentMode == Renderer.Mode.EU_GENERATION) {
                    Font font = Minecraft.getInstance().font;
                    guiGraphics.pose().translate(0, 0, 256);
                    guiGraphics.drawString(font, getEfficiencyText(), x + 8, y + 16, 0xFFFFFF, false);
                    guiGraphics.pose().translate(0, 0, -256);
                }

            } else {
                Font font = Minecraft.getInstance().font;
                Component text = MIText.MultiblockShapeInvalid.text().setStyle(TextHelper.RED.withBold(true));
                int width = font.width(text);
                guiGraphics.drawString(font, text, x + centerX - width / 2, y + centerY, 0xFFFFFF, false);
            }
        }

        @Override
        public void renderTooltip(MachineScreen screen, Font font, GuiGraphics guiGraphics, int x, int y, int cursorX, int cursorY) {
            int i = (cursorX - (x + centerX - data.gridSizeX() * 9)) / 18;
            int j = (cursorY - (y + centerY - data.gridSizeY() * 9)) / 18;

            if (data.valid()) {
                if (i >= 0 && j >= 0 && i < data.gridSizeX() && j < data.gridSizeY()) {
                    int index = data.toIndex(i, j);
                    Optional<INuclearTileData> tile = data.tilesData()[index];
                    if (tile.isPresent()) {
                        INuclearTileData tileData = tile.get();
                        TransferVariant<?> variant = tileData.getVariant();
                        if (currentMode == Renderer.Mode.NUCLEAR_FUEL) {
                            long variantAmount = tile.get().getVariantAmount();
                            if (variantAmount > 0 & !variant.isBlank()) {
                                if (variant instanceof ItemVariant itemVariant) {
                                    guiGraphics.renderTooltip(font, itemVariant.toStack((int) variantAmount), cursorX, cursorY);
                                } else if (variant instanceof FluidVariant fluidVariant) {
                                    guiGraphics.renderTooltip(font,
                                            FluidHelper.getTooltipForFluidStorage(fluidVariant, variantAmount, NuclearHatch.capacity, false),
                                            Optional.empty(), cursorX, cursorY);
                                }
                            }

                        } else if (currentMode == Renderer.Mode.TEMPERATURE) {
                            int temperature = (int) tileData.getTemperature();
                            List<Component> tooltip = new ArrayList<>();

                            tooltip.add(MIText.Temperature.text(temperature));

                            if (!variant.isBlank() && variant instanceof ItemVariant itemVariant) {
                                if (itemVariant.getItem() instanceof NuclearComponentItem item) {
                                    tooltip.add(MIText.MaxTemp.text(item.getMaxTemperature()).setStyle(TextHelper.YELLOW));
                                }
                            }

                            guiGraphics.renderTooltip(font, tooltip, Optional.empty(), cursorX, cursorY);
                            return;

                        } else if (currentMode == Renderer.Mode.EU_GENERATION) {
                            double euGeneration = tileData.getMeanEuGeneration();
                            guiGraphics.renderTooltip(font, TextHelper.getEuTextTick(euGeneration, true), cursorX, cursorY);
                            return;
                        } else {
                            double neutronRateFast;
                            double neutronRateThermal;

                            if (currentMode == Renderer.Mode.NEUTRON_ABSORPTION) {

                                neutronRateFast = tileData.getMeanNeutronAbsorption(FAST);
                                neutronRateThermal = tileData.getMeanNeutronAbsorption(THERMAL);

                            } else if (currentMode == Renderer.Mode.NEUTRON_FLUX) {

                                neutronRateFast = tileData.getMeanNeutronFlux(FAST);
                                neutronRateThermal = tileData.getMeanNeutronFlux(THERMAL);

                            } else if (currentMode == Renderer.Mode.NEUTRON_GENERATION) {

                                neutronRateFast = tileData.getMeanNeutronGeneration();
                                neutronRateThermal = 0;
                            } else {
                                neutronRateFast = 0;
                                neutronRateThermal = 0;
                            }

                            double neutronRate = 0;

                            if (neutronMode == BOTH) {
                                neutronRate = neutronRateFast + neutronRateThermal;
                            } else if (neutronMode == FAST) {
                                neutronRate = neutronRateFast;
                            } else if (neutronMode == THERMAL) {
                                neutronRate = neutronRateThermal;
                            }
                            String neutronRateString = String.format("%.1f", neutronRate);
                            List<Component> tooltips = new ArrayList<>();

                            tooltips.add(MIText.NeutronsRate.text(neutronRateString));

                            if (neutronMode == BOTH && neutronRate > 0 && currentMode != Renderer.Mode.NEUTRON_GENERATION) {
                                String neutronRateFastString = String.format("%.1f", neutronRateFast);
                                String neutronRateThermalString = String.format("%.1f", neutronRateThermal);
                                String neutronRateFastFractionString = String.format(" (%.1f %%)", 100 * neutronRateFast / neutronRate);
                                String neutronRateThermalFractionString = String.format(" (%.1f %%)", 100 * neutronRateThermal / neutronRate);

                                if (neutronRateFast > 0) {
                                    tooltips.add(Component
                                            .literal(MIText.FastNeutron.text().getString() + " : "
                                                    + MIText.NeutronsRate.text(neutronRateFastString).getString() + neutronRateFastFractionString)
                                            .setStyle(TextHelper.GRAY_TEXT));
                                }
                                if (neutronRateThermal > 0) {
                                    tooltips.add(Component.literal(MIText.ThermalNeutron.text().getString() + " : "
                                            + MIText.NeutronsRate.text(neutronRateThermalString).getString() + neutronRateThermalFractionString)
                                            .setStyle(TextHelper.GRAY_TEXT));
                                }
                            }

                            if (currentMode == Renderer.Mode.NEUTRON_GENERATION) {
                                @Nullable
                                INuclearComponent<?> component = tileData.getComponent();
                                if (component instanceof NuclearFuel fuel) {
                                    double efficiencyFactor = fuel.efficiencyFactor(tileData.getTemperature());
                                    tooltips.add(MIText.ThermalEfficiency.text(String.format("%.1f", efficiencyFactor * 100))
                                            .setStyle(TextHelper.YELLOW));
                                }
                            }

                            guiGraphics.renderTooltip(font, tooltips, Optional.empty(), cursorX, cursorY);
                            return;
                        }

                    }
                }
            }

            if (data.euFuelConsumption() > 0 && currentMode == Renderer.Mode.EU_GENERATION) {
                Font renderer = Minecraft.getInstance().font;
                int width = renderer.width(getEfficiencyText().getString());
                if (cursorX > x + 8 && cursorX < x + 8 + width && cursorY >= y + 15 && cursorY < y + 24) {

                    Component euProduction = TextHelper.getEuTextTick(data.euProduction(), true);
                    Component euFuelConsumption = TextHelper.getEuTextTick(data.euFuelConsumption(), true);

                    Component tooltip = MIText.NuclearFuelEfficiencyTooltip.text(euProduction, euFuelConsumption);

                    guiGraphics.renderTooltip(font, tooltip, cursorX, cursorY);
                }
            }
        }

        public Component getEfficiencyText() {
            String eff = String.format("%.1f", 100 * data.euProduction() / data.euFuelConsumption());
            return MIText.EfficiencyNuclear.text(eff).setStyle(TextHelper.RED);
        }

        private boolean drawButton() {
            return data.valid();
        }

        private boolean drawNeutronButton() {
            return data.valid() && (currentMode == Renderer.Mode.NEUTRON_FLUX || currentMode == Renderer.Mode.NEUTRON_ABSORPTION);
        }

        private NeutronType nextNeutronMode() {
            return NeutronType.TYPES[(neutronMode.index + 1) % NeutronType.TYPES.length];
        }

        private NeutronType previousNeutronMode() {
            return NeutronType.TYPES[(neutronMode.index - 1 + NeutronType.TYPES.length) % NeutronType.TYPES.length];
        }

        private Mode nextMode() {
            return Renderer.Mode.values()[(currentMode.index + 1) % Renderer.Mode.values().length];
        }

        private Mode previousMode() {
            return Renderer.Mode.values()[(currentMode.index - 1 + Renderer.Mode.values().length) % Renderer.Mode.values().length];
        }

        private Mode circulateMode() {
            if (CommonProxy.INSTANCE.hasShiftDown()) {
                return previousMode();
            } else {
                return nextMode();
            }
        }

        private NeutronType circulateNeutronMode() {
            if (CommonProxy.INSTANCE.hasShiftDown()) {
                return previousNeutronMode();
            } else {
                return nextNeutronMode();
            }
        }

        @Override
        public void addButtons(ButtonContainer container) {
            container.addButton(centerX + 64, 4, 20, 20,
                    (i) -> currentMode = circulateMode(),
                    () -> List.of(modeTooltip[currentMode.index], MIText.ClickToSwitch
                            .text(modeTooltip[nextMode().index]).setStyle(TextHelper.GRAY_TEXT),
                            MIText.ShiftClickToSwitch
                                    .text(modeTooltip[previousMode().index])
                                    .setStyle(TextHelper.GRAY_TEXT)),
                    (screen, button, guiGraphics, mouseX, mouseY, delta) -> {
                        button.renderVanilla(guiGraphics, mouseX, mouseY, delta);
                        if (currentMode == Renderer.Mode.NUCLEAR_FUEL) {
                            RenderHelper.renderAndDecorateItem(guiGraphics, fuelStack, button.getX() + 1, button.getY() + 1);
                        } else if (currentMode == Renderer.Mode.EU_GENERATION) {
                            guiGraphics.blit(MachineScreen.SLOT_ATLAS, button.getX() + 4, button.getY() + 2, 243, 1, 13, 17);
                        } else {
                            guiGraphics.blit(MachineScreen.SLOT_ATLAS, button.getX(), button.getY(), 124 + currentMode.index * 20, 0, 20, 20);
                        }
                    }, this::drawButton);

            container.addButton(centerX + 64, 150, 20, 20, (i) -> neutronMode = circulateNeutronMode(),
                    () -> List.of(neutronModeTooltip[neutronMode.index],
                            MIText.ClickToSwitch.text(neutronModeTooltip[nextNeutronMode().index]).setStyle(TextHelper.GRAY_TEXT),
                            MIText.ShiftClickToSwitch.text(neutronModeTooltip[previousNeutronMode().index]).setStyle(TextHelper.GRAY_TEXT)),
                    (screen, button, guiGraphics, mouseX, mouseY, delta) -> {

                        button.renderVanilla(guiGraphics, mouseX, mouseY, delta);

                        if (neutronMode == FAST) {
                            guiGraphics.blit(TEXTURE_ATLAS, button.getX() + 2, button.getY() + 2, 0, 240, 16, 16);
                        } else if (neutronMode == NeutronType.THERMAL) {
                            guiGraphics.blit(TEXTURE_ATLAS, button.getX() + 2, button.getY() + 2, 160, 240, 16, 16);
                        } else if (neutronMode == BOTH) {
                            guiGraphics.blit(TEXTURE_ATLAS, button.getX() + 2, button.getY() + 2, 80, 240, 16, 16);
                        }
                    }, this::drawNeutronButton);
        }

    }

}

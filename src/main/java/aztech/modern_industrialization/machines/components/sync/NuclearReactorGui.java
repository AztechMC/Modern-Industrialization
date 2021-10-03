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
package aztech.modern_industrialization.machines.components.sync;

import static aztech.modern_industrialization.nuclear.NeutronType.*;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.compat.rei.nuclear.NeutronInteractionCategory;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.SyncedComponents;
import aztech.modern_industrialization.machines.blockentities.hatches.NuclearHatch;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.nuclear.*;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class NuclearReactorGui {

    public record Server(Supplier<Data> dataSupplier) implements SyncedComponent.Server<Data> {

        @Override
        public Data copyData() {
            return dataSupplier.get();
        }

        @Override
        public boolean needsSync(Data cachedData) {
            Data data = copyData();
            if (data.valid != cachedData.valid || data.gridSizeX != cachedData.gridSizeX || data.gridSizeY != cachedData.gridSizeY
                    || data.euProduction != cachedData.euProduction || data.euFuelConsumption != cachedData.euFuelConsumption) {
                return true;
            } else {
                for (int i = 0; i < data.gridSizeY * data.gridSizeX; i++) {
                    if (INuclearTileData.areEquals(data.tilesData[i], cachedData.tilesData[i])) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void writeInitialData(PacketByteBuf buf) {
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(PacketByteBuf buf) {
            Data data = copyData();
            buf.writeBoolean(data.valid);
            if (data.valid) {
                buf.writeInt(data.gridSizeX);
                buf.writeInt(data.gridSizeY);
                for (Optional<INuclearTileData> tiles : data.tilesData) {
                    INuclearTileData.write(tiles, buf);
                }
                buf.writeDouble(data.euProduction);
                buf.writeDouble(data.euFuelConsumption);
            }

        }

        @Override
        public Identifier getId() {
            return SyncedComponents.NUCLEAR_REACTOR_GUI;
        }
    }

    public static class Client implements SyncedComponent.Client {

        private Data data;

        public Client(PacketByteBuf buf) {
            read(buf);
        }

        @Override
        public void read(PacketByteBuf buf) {
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
                data = new Data(true, sizeX, sizeY, tilesData, euProduction, euFuelConsumption);

            } else {
                data = new Data(false, 0, 0, null, 0, 0);
            }
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        public class Renderer implements ClientComponentRenderer {

            final int centerX = 88, centerY = 88;
            Mode currentMode = Mode.NUCLEAR_FUEL;
            NeutronType neutronMode = BOTH;

            ItemStack fuelStack = new ItemStack(Registry.ITEM.get(new MIIdentifier("uranium_fuel_rod")), 1);

            private final Identifier COLORBAR = new MIIdentifier("textures/gui/colorbar.png");

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

            Text[] modeTooltip = new Text[] { new TranslatableText("text.modern_industrialization.nuclear_fuel_mode"),
                    new TranslatableText("text.modern_industrialization.temperature_mode"),
                    new TranslatableText("text.modern_industrialization.neutron_absorption_mode"),
                    new TranslatableText("text.modern_industrialization.neutron_flux_mode"),
                    new TranslatableText("text.modern_industrialization.neutron_generation_mode"),
                    new TranslatableText("text.modern_industrialization.eu_generation_mode") };

            Text[] neutronModeTooltip = new Text[] { new TranslatableText("text.modern_industrialization.fast_neutron"),
                    new TranslatableText("text.modern_industrialization.thermal_neutron"),
                    new TranslatableText("text.modern_industrialization.both") };

            @Override
            public void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y) {

                if (data.valid) {
                    for (int i = 0; i < data.gridSizeX; i++) {
                        for (int j = 0; j < data.gridSizeY; j++) {
                            int index = data.toIndex(i, j);
                            Optional<INuclearTileData> tile = data.tilesData()[index];
                            if (tile.isPresent()) {
                                INuclearTileData tileData = tile.get();
                                RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                                int px = x + centerX - data.gridSizeX * 9 + i * 18;
                                int py = y + centerY - data.gridSizeY * 9 + j * 18;

                                if (tileData.isFluid()) {
                                    helper.drawTexture(matrices, px, py, 18, 0, 18, 18);
                                } else {
                                    helper.drawTexture(matrices, px, py, 0, 0, 18, 18);
                                }

                                TransferVariant variant = tile.get().getVariant();
                                long variantAmount = tile.get().getVariantAmount();

                                if (variantAmount > 0 & !variant.isBlank()) {
                                    if (variant instanceof ItemVariant itemVariant) {
                                        ((MachineScreenHandlers.ClientScreen) helper).renderItemInGui(itemVariant.toStack((int) variantAmount),
                                                px + 1, py + 1);
                                    } else if (variant instanceof FluidVariant fluidVariant) {
                                        RenderSystem.disableBlend();
                                        RenderHelper.drawFluidInGui(matrices, fluidVariant, px + 1, py + 1);
                                    }
                                }

                                if (currentMode != Mode.NUCLEAR_FUEL) {
                                    int u;
                                    int v;

                                    if (currentMode == Mode.TEMPERATURE) {
                                        v = 30;
                                        u = (int) (299 * tileData.getTemperature() / NuclearConstant.MAX_TEMPERATURE);
                                    } else if (currentMode == Mode.EU_GENERATION) {
                                        v = 30;
                                        u = (int) (299
                                                * Math.min(1.0, tileData.getMeanEuGeneration() / (2 * NuclearConstant.MAX_HATCH_EU_PRODUCTION)));
                                    } else {
                                        double neutronRate;
                                        double factor = neutronMode == BOTH ? 2 : 1;
                                        if (currentMode == Mode.NEUTRON_ABSORPTION) {
                                            neutronRate = 5 * tileData.getMeanNeutronAbsorption(neutronMode);
                                        } else if (currentMode == Mode.NEUTRON_FLUX) {
                                            neutronRate = tileData.getMeanNeutronFlux(neutronMode);
                                        } else if (currentMode == Mode.NEUTRON_GENERATION) {
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
                                        u = (int) (299 * neutronColorScheme(factor * neutronRate));
                                    }

                                    RenderSystem.setShaderTexture(0, COLORBAR);
                                    RenderSystem.disableDepthTest();
                                    RenderSystem.enableBlend();

                                    matrices.translate(px, py, 0);
                                    matrices.scale(18, 18, 1);
                                    DrawableHelper.drawTexture(matrices, 0, 0, u, v, 1, 1, 300, 60);
                                    matrices.scale(1 / 18f, 1 / 18f, 1);
                                    matrices.translate(-px, -py, 0);

                                }

                                if (currentMode == Mode.TEMPERATURE) {
                                    if (!variant.isBlank() && variant instanceof ItemVariant itemVariant) {
                                        if (itemVariant.getItem() instanceof NuclearComponentItem item) {
                                            if (tileData.getTemperature() + 100 > item.getMaxTemperature()) {
                                                RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                                                RenderSystem.enableBlend();
                                                RenderSystem.disableDepthTest();
                                                if (System.currentTimeMillis() % 1000 > 500) {
                                                    helper.drawTexture(matrices, px + 1, py + 1, 22, 58, 16, 16);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (data.euFuelConsumption > 0 && currentMode == Mode.EU_GENERATION) {
                        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
                        matrices.translate(0, 0, 256);
                        renderer.draw(matrices, getEfficiencyText(), x + 8, y + 16, 0xFFFFFF);
                        matrices.translate(0, 0, -256);
                    }

                } else {
                    TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
                    Text text = new TranslatableText("text.modern_industrialization.multiblock_shape_invalid")
                            .setStyle(TextHelper.RED.withBold(true));
                    int width = renderer.getWidth(text);
                    renderer.draw(matrices, text, x + centerX - width / 2f, y + centerY, 0xFFFFFF);
                }
            }

            @Override
            public void renderTooltip(MachineScreenHandlers.ClientScreen screen, MatrixStack matrices, int x, int y, int cursorX, int cursorY) {
                int i = (cursorX - (x + centerX - data.gridSizeX * 9)) / 18;
                int j = (cursorY - (y + centerY - data.gridSizeY * 9)) / 18;

                if (data.valid) {
                    if (i >= 0 && j >= 0 && i < data.gridSizeX && j < data.gridSizeY) {
                        int index = data.toIndex(i, j);
                        Optional<INuclearTileData> tile = data.tilesData[index];
                        if (tile.isPresent()) {
                            INuclearTileData tileData = tile.get();
                            TransferVariant variant = tileData.getVariant();
                            if (currentMode == Mode.NUCLEAR_FUEL) {
                                long variantAmount = tile.get().getVariantAmount();
                                if (variantAmount > 0 & !variant.isBlank()) {
                                    if (variant instanceof ItemVariant itemVariant) {
                                        screen.renderTooltip(matrices, screen.getTooltipFromItem(itemVariant.toStack((int) variantAmount)), cursorX,
                                                cursorY);
                                    } else if (variant instanceof FluidVariant fluidVariant) {
                                        screen.renderTooltip(matrices,
                                                FluidHelper.getTooltipForFluidStorage(fluidVariant, variantAmount, NuclearHatch.capacity, false),
                                                cursorX, cursorY);
                                    }
                                }

                            } else if (currentMode == Mode.TEMPERATURE) {
                                int temperature = (int) tileData.getTemperature();
                                List<Text> tooltip = new ArrayList<>();

                                tooltip.add(new TranslatableText("text.modern_industrialization.temperature", temperature));

                                if (!variant.isBlank() && variant instanceof ItemVariant itemVariant) {
                                    if (itemVariant.getItem() instanceof NuclearComponentItem item) {
                                        tooltip.add(new TranslatableText("text.modern_industrialization.max_temp", item.getMaxTemperature())
                                                .setStyle(TextHelper.YELLOW));
                                    }
                                }

                                screen.renderTooltip(matrices, tooltip, cursorX, cursorY);
                                return;

                            } else if (currentMode == Mode.EU_GENERATION) {
                                double euGeneration = tileData.getMeanEuGeneration();
                                screen.renderTooltip(matrices, new TranslatableText("text.modern_industrialization.base_eu_t", (int) euGeneration)
                                        .setStyle(TextHelper.EU_TEXT), cursorX, cursorY);
                                return;
                            }

                            else {
                                double neutronRateFast;
                                double neutronRateThermal;

                                if (currentMode == Mode.NEUTRON_ABSORPTION) {

                                    neutronRateFast = tileData.getMeanNeutronAbsorption(FAST);
                                    neutronRateThermal = tileData.getMeanNeutronAbsorption(THERMAL);

                                } else if (currentMode == Mode.NEUTRON_FLUX) {

                                    neutronRateFast = tileData.getMeanNeutronFlux(FAST);
                                    neutronRateThermal = tileData.getMeanNeutronFlux(THERMAL);

                                } else if (currentMode == Mode.NEUTRON_GENERATION) {

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
                                List<Text> tooltips = new ArrayList<>();
                                tooltips.add(new TranslatableText("text.modern_industrialization.neutrons_rate", neutronRateString));

                                if (neutronMode == BOTH && neutronRate > 0 && currentMode != Mode.NEUTRON_GENERATION) {
                                    String neutronRateFastString = String.format("%.1f", neutronRateFast);
                                    String neutronRateThermalString = String.format("%.1f", neutronRateThermal);
                                    String neutronRateFastFractionString = String.format(" (%.1f %%)", 100 * neutronRateFast / neutronRate);
                                    String neutronRateThermalFractionString = String.format(" (%.1f %%)", 100 * neutronRateThermal / neutronRate);

                                    if (neutronRateFast > 0) {
                                        tooltips.add(
                                                new LiteralText(
                                                        new TranslatableText("text.modern_industrialization.fast_neutron").getString() + " : "
                                                                + new TranslatableText("text.modern_industrialization.neutrons_rate",
                                                                        neutronRateFastString).getString()
                                                                + neutronRateFastFractionString).setStyle(TextHelper.GRAY_TEXT));
                                    }
                                    if (neutronRateThermal > 0) {
                                        tooltips.add(new LiteralText(
                                                new TranslatableText("text.modern_industrialization.thermal_neutron").getString() + " : "
                                                        + new TranslatableText("text.modern_industrialization.neutrons_rate",
                                                                neutronRateThermalString).getString()
                                                        + neutronRateThermalFractionString).setStyle(TextHelper.GRAY_TEXT));
                                    }
                                }

                                if (currentMode == Mode.NEUTRON_GENERATION) {
                                    if (tileData.getComponent().isPresent()) {
                                        if (tileData.getComponent().get() instanceof NuclearFuel fuel) {
                                            double efficiencyFactor = fuel.efficiencyFactor(tileData.getTemperature());
                                            tooltips.add(new TranslatableText("text.modern_industrialization.thermal_efficiency",
                                                    String.format("%.1f", efficiencyFactor * 100)).setStyle(TextHelper.YELLOW));
                                        }
                                    }
                                }

                                screen.renderTooltip(matrices, tooltips, cursorX, cursorY);
                                return;
                            }

                        }
                    }
                }

                if (data.euFuelConsumption > 0 && currentMode == Mode.EU_GENERATION) {
                    TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
                    int width = renderer.getWidth(getEfficiencyText().getString());
                    if (cursorX > x + 8 && cursorX < x + 8 + width && cursorY >= y + 15 && cursorY < y + 24) {

                        Text euProduction = new LiteralText(TextHelper.getEuString(data.euProduction) + " " + TextHelper.getEuUnit(data.euProduction))
                                .setStyle(TextHelper.EU_TEXT);
                        Text euFuelConsumption = new LiteralText(
                                TextHelper.getEuString(data.euFuelConsumption) + " " + TextHelper.getEuUnit(data.euFuelConsumption))
                                        .setStyle(TextHelper.EU_TEXT);

                        Text tooltip = new TranslatableText("text.modern_industrialization.nuclear_fuel_efficiency_tooltip", euProduction,
                                euFuelConsumption);

                        screen.renderTooltip(matrices, tooltip, cursorX, cursorY);
                        return;
                    }
                }
            }

            public Text getEfficiencyText() {
                String eff = String.format("%.1f", 100 * data.euProduction / data.euFuelConsumption);
                return new TranslatableText("text.modern_industrialization.efficiency_nuclear", eff).setStyle(TextHelper.RED);
            }

            private boolean drawButton() {
                return data.valid;
            }

            private boolean drawNeutronButton() {
                return data.valid && (currentMode == Mode.NEUTRON_FLUX || currentMode == Mode.NEUTRON_ABSORPTION);
            }

            private NeutronType nextNeutronMode() {
                return NeutronType.TYPES[(neutronMode.index + 1) % NeutronType.TYPES.length];
            }

            @Override
            public void addButtons(ButtonContainer container) {

                if (drawButton()) {

                    container.addButton(centerX + 64, 4, 20, 20, new LiteralText(""),
                            (i) -> currentMode = Mode.values()[(currentMode.index + 1) % Mode.values().length],
                            () -> List.of(modeTooltip[currentMode.index],

                                    new TranslatableText("text.modern_industrialization.click_to_switch",
                                            modeTooltip[(currentMode.index + 1) % Mode.values().length]).setStyle(TextHelper.GRAY_TEXT)),
                            (screen, button, matrices, mouseX, mouseY, delta) -> {
                                button.renderVanilla(matrices, mouseX, mouseY, delta);
                                if (currentMode == Mode.NUCLEAR_FUEL) {
                                    ((MachineScreenHandlers.ClientScreen) screen).renderItemInGui(fuelStack, button.x + 1, button.y + 1);
                                } else if (currentMode == Mode.EU_GENERATION) {
                                    RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                                    screen.drawTexture(matrices, button.x + 4, button.y + 2, 243, 1, 13, 17);
                                } else {
                                    RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                                    screen.drawTexture(matrices, button.x, button.y, 124 + currentMode.index * 20, 0, 20, 20);
                                }
                                if (button.isHovered()) {
                                    button.renderTooltip(matrices, mouseX, mouseY);
                                }

                            });

                    container
                            .addButton(centerX + 64, 150, 20, 20, new LiteralText(""), (i) -> neutronMode = nextNeutronMode(),
                                    () -> List.of(neutronModeTooltip[neutronMode.index],
                                            new TranslatableText("text.modern_industrialization.click_to_switch",
                                                    neutronModeTooltip[nextNeutronMode().index]).setStyle(TextHelper.GRAY_TEXT)),
                                    (screen, button, matrices, mouseX, mouseY, delta) -> {

                                        button.renderVanilla(matrices, mouseX, mouseY, delta);
                                        RenderSystem.setShaderTexture(0, NeutronInteractionCategory.TEXTURE_ATLAS);

                                        if (neutronMode == FAST) {
                                            screen.drawTexture(matrices, button.x + 2, button.y + 2, 0, 240, 16, 16);
                                        } else if (neutronMode == NeutronType.THERMAL) {
                                            screen.drawTexture(matrices, button.x + 2, button.y + 2, 160, 240, 16, 16);
                                        } else if (neutronMode == BOTH) {
                                            screen.drawTexture(matrices, button.x + 2, button.y + 2, 80, 240, 16, 16);
                                        }

                                        if (button.isHovered()) {
                                            button.renderTooltip(matrices, mouseX, mouseY);
                                        }
                                    }, this::drawNeutronButton);
                }
            }

        }

    }

    final private static double neutronsMax = 8192;

    public static double neutronColorScheme(double neutronNumber) {
        neutronNumber = Math.min(neutronNumber, neutronsMax);
        return Math.log(1 + 10 * neutronNumber) / Math.log(1 + 10 * neutronsMax);
    }

    public record Data(boolean valid, int gridSizeX, int gridSizeY, Optional<INuclearTileData>[] tilesData, double euProduction,
            double euFuelConsumption) {

        public int toIndex(int x, int y) {
            return toIndex(x, y, gridSizeY);
        }

        public static int toIndex(int x, int y, int sizeY) {
            return x * sizeY + y;
        }
    }

}

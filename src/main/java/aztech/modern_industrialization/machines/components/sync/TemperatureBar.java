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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.SyncedComponents;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TemperatureBar {
    public static class Server implements SyncedComponent.Server<Integer> {
        private final Parameters params;
        private final Supplier<Integer> temperatureSupplier;

        public Server(Parameters params, Supplier<Integer> temperatureSupplier) {
            this.params = params;
            this.temperatureSupplier = temperatureSupplier;
        }

        @Override
        public Integer copyData() {
            return temperatureSupplier.get();
        }

        @Override
        public boolean needsSync(Integer cachedData) {
            return !cachedData.equals(temperatureSupplier.get());
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buf) {
            buf.writeInt(params.renderX);
            buf.writeInt(params.renderY);
            buf.writeInt(params.temperatureMax);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(FriendlyByteBuf buf) {
            buf.writeInt(temperatureSupplier.get());
        }

        @Override
        public ResourceLocation getId() {
            return SyncedComponents.TEMPERATURE_BAR;
        }
    }

    public static class Client implements SyncedComponent.Client {
        public final Parameters params;
        public int temperature;

        public Client(FriendlyByteBuf buf) {
            this.params = new Parameters(buf.readInt(), buf.readInt(), buf.readInt());
            read(buf);
        }

        @Override
        public void read(FriendlyByteBuf buf) {
            this.temperature = buf.readInt();
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        public class Renderer implements ClientComponentRenderer {

            private final MIIdentifier TEXTURE = new MIIdentifier("textures/gui/efficiency_bar.png");
            private final int WIDTH = 100, HEIGHT = 2;

            @Override
            public void renderBackground(GuiComponent helper, PoseStack matrices, int x, int y) {
                RenderSystem.setShaderTexture(0, TEXTURE);
                // background
                GuiComponent.blit(matrices, x + params.renderX - 1, y + params.renderY - 1, helper.getBlitOffset(), 0, 2, WIDTH + 2, HEIGHT + 2,
                        102, 6);
                int barPixels = (int) ((float) temperature / params.temperatureMax * WIDTH);
                GuiComponent.blit(matrices, x + params.renderX, y + params.renderY, helper.getBlitOffset(), 0, 0, barPixels, HEIGHT, 102, 6);
                RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                helper.blit(matrices, x + params.renderX - 22, y + params.renderY + HEIGHT / 2 - 10, 144, 0, 20, 20);

            }

            @Override
            public void renderTooltip(MachineScreenHandlers.ClientScreen screen, PoseStack matrices, int x, int y, int cursorX, int cursorY) {
                if (aztech.modern_industrialization.util.RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT,
                        cursorX - x, cursorY - y)) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(MIText.Temperature.text(temperature));
                    screen.renderComponentTooltip(matrices, tooltip, cursorX, cursorY);
                }
            }
        }
    }

    public static class Parameters {
        public final int renderX, renderY;
        public final int temperatureMax;

        public Parameters(int renderX, int renderY, int temperatureMax) {
            this.renderX = renderX;
            this.renderY = renderY;
            this.temperatureMax = temperatureMax;
        }
    }
}

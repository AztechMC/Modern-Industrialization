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

import static aztech.modern_industrialization.machines.components.sync.EnergyBar.Client.Renderer.HEIGHT;
import static aztech.modern_industrialization.machines.components.sync.EnergyBar.Client.Renderer.WIDTH;

import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.SyncedComponents;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class EnergyBar {
    public static class Server implements SyncedComponent.Server<Data> {
        public final Parameters params;
        public final Supplier<Long> euSupplier, maxEuSupplier;

        public Server(Parameters params, Supplier<Long> euSupplier, Supplier<Long> maxEuSupplier) {
            this.params = params;
            this.euSupplier = euSupplier;
            this.maxEuSupplier = maxEuSupplier;
        }

        @Override
        public Data copyData() {
            return new Data(euSupplier.get(), maxEuSupplier.get());
        }

        @Override
        public boolean needsSync(Data cachedData) {
            return cachedData.eu != euSupplier.get() || cachedData.maxEu != maxEuSupplier.get();
        }

        @Override
        public void writeInitialData(PacketByteBuf buf) {
            buf.writeInt(params.renderX);
            buf.writeInt(params.renderY);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(PacketByteBuf buf) {
            buf.writeLong(euSupplier.get());
            buf.writeLong(maxEuSupplier.get());
        }

        @Override
        public Identifier getId() {
            return SyncedComponents.ENERGY_BAR;
        }
    }

    public static class Client implements SyncedComponent.Client {
        final Parameters params;
        long eu, maxEu;

        public Client(PacketByteBuf buf) {
            this.params = new Parameters(buf.readInt(), buf.readInt());
            read(buf);
        }

        @Override
        public void read(PacketByteBuf buf) {
            eu = buf.readLong();
            maxEu = buf.readLong();
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        public class Renderer implements ClientComponentRenderer {
            public static final int WIDTH = 13;
            public static final int HEIGHT = 18;

            public static void renderEnergy(DrawableHelper helper, MatrixStack matrices, int px, int py, float fill) {
                RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                helper.drawTexture(matrices, px, py, 230, 0, WIDTH, HEIGHT);
                int fillPixels = (int) (fill * HEIGHT * 0.9 + HEIGHT * 0.1);
                if (fill > 0.95)
                    fillPixels = HEIGHT;
                helper.drawTexture(matrices, px, py + HEIGHT - fillPixels, 243, HEIGHT - fillPixels, WIDTH, fillPixels);
            }

            @Override
            public void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y) {
                renderEnergy(helper, matrices, x + params.renderX, y + params.renderY, (float) eu / maxEu);
            }

            @Override
            public void renderTooltip(MachineScreenHandlers.ClientScreen screen, MatrixStack matrices, int x, int y, int cursorX, int cursorY) {
                if (RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT, cursorX - x, cursorY - y)) {
                    Text tooltip;
                    if (Screen.hasShiftDown()) {
                        tooltip = new TranslatableText("text.modern_industrialization.energy_bar", eu, maxEu);
                    } else {
                        if (maxEu > 1e12) {
                            String eus = String.format("%.2f", ((double) eu) / 1e12);
                            String maxEus = String.format("%.2f", ((double) maxEu) / 1e12);
                            tooltip = new TranslatableText("text.modern_industrialization.energy_bar_double", eus, maxEus, "TEU");
                        } else if (maxEu > 1e9) {
                            String eus = String.format("%.2f", ((double) eu) / 1e9);
                            String maxEus = String.format("%.2f", ((double) maxEu) / 1e9);
                            tooltip = new TranslatableText("text.modern_industrialization.energy_bar_double", eus, maxEus, "GEU");

                        } else if (maxEu > 1e6) {
                            String eus = String.format("%.2f", ((double) eu) / 1e6);
                            String maxEus = String.format("%.2f", ((double) maxEu) / 1e6);
                            tooltip = new TranslatableText("text.modern_industrialization.energy_bar_double", eus, maxEus, "MEU");

                        } else if (maxEu > 1e4) {
                            String eus = String.format("%.2f", ((double) eu) / 1e3);
                            String maxEus = String.format("%.2f", ((double) maxEu) / 1e3);
                            tooltip = new TranslatableText("text.modern_industrialization.energy_bar_double", eus, maxEus, "kEU");
                        } else {
                            tooltip = new TranslatableText("text.modern_industrialization.energy_bar", eu, maxEu);
                        }
                    }
                    screen.renderTooltip(matrices, Collections.singletonList(tooltip), cursorX, cursorY);
                }
            }
        }
    }

    private static class Data {
        final long eu;
        final long maxEu;

        Data(long eu, long maxEu) {
            this.eu = eu;
            this.maxEu = maxEu;
        }
    }

    public static class Parameters {
        public final int renderX, renderY;

        public Parameters(int renderX, int renderY) {
            this.renderX = renderX;
            this.renderY = renderY;
        }
    }
}

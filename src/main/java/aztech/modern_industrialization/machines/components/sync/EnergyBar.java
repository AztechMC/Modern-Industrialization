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

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.SyncedComponents;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
        public void writeInitialData(FriendlyByteBuf buf) {
            buf.writeInt(params.renderX);
            buf.writeInt(params.renderY);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(FriendlyByteBuf buf) {
            buf.writeLong(euSupplier.get());
            buf.writeLong(maxEuSupplier.get());
        }

        @Override
        public ResourceLocation getId() {
            return SyncedComponents.ENERGY_BAR;
        }
    }

    public static class Client implements SyncedComponent.Client {
        final Parameters params;
        long eu, maxEu;

        public Client(FriendlyByteBuf buf) {
            this.params = new Parameters(buf.readInt(), buf.readInt());
            read(buf);
        }

        @Override
        public void read(FriendlyByteBuf buf) {
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

            public static void renderEnergy(GuiComponent helper, PoseStack matrices, int px, int py, float fill) {
                RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                helper.blit(matrices, px, py, 230, 0, WIDTH, HEIGHT);
                int fillPixels = (int) (fill * HEIGHT * 0.9 + HEIGHT * 0.1);
                if (fill > 0.95)
                    fillPixels = HEIGHT;
                helper.blit(matrices, px, py + HEIGHT - fillPixels, 243, HEIGHT - fillPixels, WIDTH, fillPixels);
            }

            @Override
            public void renderBackground(GuiComponent helper, PoseStack matrices, int x, int y) {
                renderEnergy(helper, matrices, x + params.renderX, y + params.renderY, (float) eu / maxEu);
            }

            @Override
            public void renderTooltip(MachineScreenHandlers.ClientScreen screen, PoseStack matrices, int x, int y, int cursorX, int cursorY) {
                if (RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT, cursorX - x, cursorY - y)) {
                    Component tooltip;
                    if (Screen.hasShiftDown()) {
                        tooltip = MIText.EuMaxed.text(eu, maxEu, "");
                    } else {
                        TextHelper.MaxedAmount maxedAmount = TextHelper.getMaxedAmount(eu, maxEu);
                        tooltip = MIText.EuMaxed.text(maxedAmount.digit(), maxedAmount.maxDigit(), maxedAmount.unit());
                    }
                    screen.renderComponentTooltip(matrices, Collections.singletonList(tooltip), cursorX, cursorY);
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

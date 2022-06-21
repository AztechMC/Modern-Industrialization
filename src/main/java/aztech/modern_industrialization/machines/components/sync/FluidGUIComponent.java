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

import aztech.modern_industrialization.machines.MachineGuis;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.SyncedComponents;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class FluidGUIComponent {

    public static class Server implements SyncedComponent.Server<Data> {

        public final Supplier<Data> fluidDataSupplier;

        public Server(Supplier<Data> fluidDataSupplier) {
            this.fluidDataSupplier = fluidDataSupplier;
        }

        @Override
        public Data copyData() {
            return fluidDataSupplier.get();
        }

        @Override
        public boolean needsSync(Data cachedData) {
            Data newFluidData = fluidDataSupplier.get();
            return !cachedData.equals(newFluidData);
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buf) {
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(FriendlyByteBuf buf) {
            Data fluidData = fluidDataSupplier.get();
            fluidData.fluid.toPacket(buf);
            buf.writeLong(fluidData.amount);
            buf.writeLong(fluidData.capacity);
        }

        @Override
        public ResourceLocation getId() {
            return SyncedComponents.FLUID_STORAGE_GUI;
        }
    }

    public static class Client implements SyncedComponent.Client {

        Data fluidData;

        public Client(FriendlyByteBuf buf) {
            read(buf);
        }

        @Override
        public void read(FriendlyByteBuf buf) {
            fluidData = new Data(FluidVariant.fromPacket(buf), buf.readLong(), buf.readLong());
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        public class Renderer implements ClientComponentRenderer {

            private static final int posX = 70, posY = 12;

            @Override
            public void renderBackground(GuiComponent helper, PoseStack matrices, int x, int y) {
                FluidVariant fluid = fluidData.fluid;
                float fracFull = (float) fluidData.amount / fluidData.capacity;

                RenderSystem.setShaderTexture(0, MachineGuis.SLOT_ATLAS);
                helper.blit(matrices, x + posX, y + posY, 92, 38, 46, 62);

                if (!fluid.isBlank()) {
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 3; j++) {
                            float localFullness = Math.min(Math.max(3 * fracFull - (2 - j), 0), 1);
                            RenderHelper.drawFluidInGui(matrices, fluid, x + posX + 7 + i * 16, y + posY + 7 + j * 16 + (1 - localFullness) * 16, 16,
                                    localFullness);
                        }
                    }
                }
                RenderSystem.setShaderTexture(0, MachineGuis.SLOT_ATLAS);
                helper.blit(matrices, x + posX + 7, y + posY + 7, 60, 38, 32, 48);

            }

            @Override
            public void renderTooltip(MachineGuis.ClientScreen screen, PoseStack matrices, int x, int y, int cursorX, int cursorY) {
                if (RenderHelper.isPointWithinRectangle(posX + 7, posY + 7, 32, 48, cursorX - x, cursorY - y)) {
                    screen.renderComponentTooltip(matrices,
                            FluidHelper.getTooltipForFluidStorage(fluidData.fluid, fluidData.amount, fluidData.capacity),
                            cursorX, cursorY);
                }
            }
        }

    }

    public static record Data(FluidVariant fluid, long amount, long capacity) {
    }

}

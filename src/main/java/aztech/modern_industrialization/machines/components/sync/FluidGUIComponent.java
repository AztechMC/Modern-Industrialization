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

import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.SyncedComponents;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
        public void writeInitialData(PacketByteBuf buf) {
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(PacketByteBuf buf) {
            Data fluidData = fluidDataSupplier.get();
            fluidData.fluid.toPacket(buf);
            buf.writeLong(fluidData.amount);
            buf.writeLong(fluidData.capacity);
        }

        @Override
        public Identifier getId() {
            return SyncedComponents.FLUID_STORAGE_GUI;
        }
    }

    public static class Client implements SyncedComponent.Client {

        Data fluidData;

        public Client(PacketByteBuf buf) {
            read(buf);
        }

        @Override
        public void read(PacketByteBuf buf) {
            fluidData = new Data(FluidVariant.fromPacket(buf), buf.readLong(), buf.readLong());
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        public class Renderer implements ClientComponentRenderer {

            private static final int posX = 64, posY = 20, scale = 48;

            @Override
            public void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y) {
                FluidVariant fluid = fluidData.fluid;
                float fracFull = (float) fluidData.amount / fluidData.capacity;
                RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                matrices.push();
                matrices.translate(x + posX, y + posY - 1, 0);
                matrices.scale(2, 2, 2);
                helper.drawTexture(matrices, 0, 0, 85, 38, 25, 25);
                matrices.pop();

                if (!fluid.isBlank()) {
                    RenderHelper.drawFluidInGui(matrices, fluid, x + posX, y + posY + ((1f - fracFull) * scale), scale, fracFull);
                }

                RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
                matrices.push();
                matrices.translate(x + posX, y + posY - 1, 0);
                matrices.scale(2, 2, 2);
                helper.drawTexture(matrices, 0, 0, 60, 38, 25, 25);
                matrices.pop();

            }

            @Override
            public void renderTooltip(MachineScreenHandlers.ClientScreen screen, MatrixStack matrices, int x, int y, int cursorX, int cursorY) {
                if (RenderHelper.isPointWithinRectangle(posX, posY, 50, 50, cursorX - x, cursorY - y)) {
                    List<Text> tooltip = FluidHelper.getTooltip(fluidData.fluid, true);
                    tooltip.add(FluidHelper.getFluidAmount(fluidData.amount, fluidData.capacity));
                    screen.renderTooltip(matrices, tooltip, cursorX, cursorY);
                }
            }
        }

    }

    public static record Data(FluidVariant fluid, long amount, long capacity) {
    }

}

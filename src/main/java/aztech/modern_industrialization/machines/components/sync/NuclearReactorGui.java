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
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class NuclearReactorGui {

    public record Server(Supplier<Data> dataSupplier) implements SyncedComponent.Server<Data> {

        @Override
        public Data copyData() {
            return dataSupplier.get();
        }

        @Override
        public boolean needsSync(Data cachedData) {
            Data data = copyData();
            if (data.valid != cachedData.valid || data.gridSizeX != cachedData.gridSizeX || data.gridSizeY != cachedData.gridSizeY) {
                return true;
            } else {
                for (int i = 0; i < data.gridSizeY * data.gridSizeX; i++) {
                    if (data.temperature[i] != cachedData.temperature[i]) {
                        return true;
                    }
                    if (data.hasHatch[i] != cachedData.hasHatch[i]) {
                        return true;
                    }
                    if (!ItemStack.areEqual(data.stacksInHatch[i], cachedData.stacksInHatch[i])) {
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
                for (Double temp : data.temperature) {
                    buf.writeDouble(temp);
                }
                for (Boolean hasHatch : data.hasHatch) {
                    buf.writeBoolean(hasHatch);
                }
                for (ItemStack is : data.stacksInHatch) {
                    buf.writeItemStack(Objects.requireNonNullElse(is, ItemStack.EMPTY));
                }
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
                double[] temperature = new double[sizeX * sizeY];
                boolean[] hasHatch = new boolean[sizeX * sizeY];
                ItemStack[] stack = new ItemStack[sizeX * sizeY];

                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < sizeX * sizeY; i++) {

                        if (j == 0) {
                            temperature[i] = buf.readDouble();
                        } else if (j == 1) {
                            hasHatch[i] = buf.readBoolean();
                        } else {
                            stack[i] = buf.readItemStack();
                        }

                    }
                }
                data = new Data(true, sizeX, sizeY, temperature, hasHatch, stack);
            } else {
                data = new Data(false, 0, 0, null, null, null);
            }
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        public class Renderer implements ClientComponentRenderer {

            @Override
            public void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y) {
                if (data.valid) {
                    int centerX = 88, centerY = 88;
                    RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);

                    for (int i = 0; i < data.gridSizeX; i++) {
                        for (int j = 0; j < data.gridSizeY; j++) {
                            int index = data.toIndex(i, j);
                            if (data.hasHatch[index]) {
                                helper.drawTexture(matrices, x + centerX - data.gridSizeX * 9 + i * 18, y + centerY - data.gridSizeY * 9 + j * 18, 0,
                                        0, 18, 18);

                            }
                        }
                    }

                    for (int i = 0; i < data.gridSizeX; i++) {
                        for (int j = 0; j < data.gridSizeY; j++) {
                            int index = data.toIndex(i, j);
                            if (data.hasHatch[index]) {
                                if (!data.stacksInHatch[index].isEmpty()) {
                                    if (!data.stacksInHatch[index].isEmpty()) {
                                        MinecraftClient.getInstance().getItemRenderer().renderInGui(data.stacksInHatch[index],
                                                x + centerX - data.gridSizeX * 9 + i * 18 + 1, y + centerY - data.gridSizeY * 9 + j * 18 + 1

                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public record Data(boolean valid, int gridSizeX, int gridSizeY, double[] temperature, boolean[] hasHatch, ItemStack[] stacksInHatch) {

        public int toIndex(int x, int y) {
            return toIndex(x, y, gridSizeY);
        }

        public static int toIndex(int x, int y, int sizeY) {
            return x * sizeY + y;
        }
    }

}

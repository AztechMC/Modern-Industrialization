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
import aztech.modern_industrialization.nuclear.INuclearTileData;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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
                data = new Data(true, sizeX, sizeY, tilesData);

            } else {
                data = new Data(false, 0, 0, null);
            }
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        public class Renderer implements ClientComponentRenderer {

            int centerX = 88, centerY = 88;

            @Override
            public void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y) {

                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

                if (data.valid) {
                    RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);

                    for (int i = 0; i < data.gridSizeX; i++) {
                        for (int j = 0; j < data.gridSizeY; j++) {
                            int index = data.toIndex(i, j);
                            if (data.tilesData[index].isPresent()) {
                                helper.drawTexture(matrices, x + centerX - data.gridSizeX * 9 + i * 18, y + centerY - data.gridSizeY * 9 + j * 18, 0,
                                        0, 18, 18);
                            }
                        }
                    }

                    for (int i = 0; i < data.gridSizeX; i++) {
                        for (int j = 0; j < data.gridSizeY; j++) {
                            int index = data.toIndex(i, j);
                            Optional<INuclearTileData> tile = data.tilesData[index];
                            if (tile.isPresent()) {
                                ItemStack stack = tile.get().getStack();
                                if (!stack.isEmpty()) {

                                    int px = x + centerX - data.gridSizeX * 9 + i * 18 + 1;
                                    int py = y + centerY - data.gridSizeY * 9 + j * 18 + 1;

                                    MinecraftClient.getInstance().getItemRenderer().renderInGui(stack, px, py);
                                    MinecraftClient.getInstance().getItemRenderer().renderGuiItemOverlay(textRenderer, stack, px, py);

                                }
                            }
                        }
                    }
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
                            ItemStack stack = tile.get().getStack();
                            if (!stack.isEmpty()) {
                                screen.renderTooltip(matrices, screen.getTooltipFromItem(stack), cursorX, cursorY);
                            }
                        }
                    }

                }
            }
        }
    }

    public record Data(boolean valid, int gridSizeX, int gridSizeY, Optional<INuclearTileData>[] tilesData) {

        public int toIndex(int x, int y) {
            return toIndex(x, y, gridSizeY);
        }

        public static int toIndex(int x, int y, int sizeY) {
            return x * sizeY + y;
        }
    }

}

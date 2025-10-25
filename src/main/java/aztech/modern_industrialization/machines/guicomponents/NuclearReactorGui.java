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

import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import aztech.modern_industrialization.nuclear.*;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class NuclearReactorGui {
    public record Server(Supplier<Data> dataSupplier) implements GuiComponent.Server<Data> {
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
        public void writeInitialData(RegistryFriendlyByteBuf buf) {
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(RegistryFriendlyByteBuf buf) {
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
        public ResourceLocation getId() {
            return GuiComponents.NUCLEAR_REACTOR_GUI;
        }
    }

    final private static double neutronsMax = 8192;

    public static double neutronColorScheme(double neutronNumber) {
        neutronNumber = Math.min(neutronNumber, neutronsMax);
        return Math.log(1 + 10 * neutronNumber) / Math.log(1 + 10 * neutronsMax);
    }

    public record Data(boolean valid, int gridSizeX, int gridSizeY, Optional<INuclearTileData>[] tilesData,
            double euProduction,
            double euFuelConsumption) {
        public int toIndex(int x, int y) {
            return toIndex(x, y, gridSizeY);
        }

        public static int toIndex(int x, int y, int sizeY) {
            return x * sizeY + y;
        }
    }
}

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
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TemperatureBar {
    public static class Server implements GuiComponent.Server<Integer> {
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
        public void writeInitialData(RegistryFriendlyByteBuf buf) {
            buf.writeInt(params.renderX);
            buf.writeInt(params.renderY);
            buf.writeInt(params.temperatureMax);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(RegistryFriendlyByteBuf buf) {
            buf.writeInt(temperatureSupplier.get());
        }

        @Override
        public ResourceLocation getId() {
            return GuiComponents.TEMPERATURE_BAR;
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

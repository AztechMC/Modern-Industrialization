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
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Supports both auto-extract and auto-insert. Auto-insert is just a GUI change,
 * but the logic stays the same.
 */
public class AutoExtract {
    public static class Server implements GuiComponent.Server<Data> {
        private final OrientationComponent orientation;
        private final boolean displayAsInsert; // true for auto-insert

        public Server(OrientationComponent orientation, boolean displayAsInsert) {
            this.orientation = orientation;
            this.displayAsInsert = displayAsInsert;
        }

        public Server(OrientationComponent orientation) {
            this(orientation, false);
        }

        @Override
        public Data copyData() {
            return new Data(orientation.extractItems, orientation.extractFluids);
        }

        @Override
        public boolean needsSync(Data cachedData) {
            return cachedData.extractItems != orientation.extractItems || cachedData.extractFluids != orientation.extractFluids;
        }

        @Override
        public void writeInitialData(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(displayAsInsert);
            buf.writeBoolean(orientation.params.hasExtractItems);
            buf.writeBoolean(orientation.params.hasExtractFluids);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(orientation.extractItems);
            buf.writeBoolean(orientation.extractFluids);
        }

        @Override
        public ResourceLocation getId() {
            return GuiComponents.AUTO_EXTRACT;
        }

        public OrientationComponent getOrientation() {
            return orientation;
        }
    }

    private static class Data {
        public final boolean extractItems;
        public final boolean extractFluids;

        private Data(boolean extractItems, boolean extractFluids) {
            this.extractItems = extractItems;
            this.extractFluids = extractFluids;
        }
    }
}

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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class LargeTankFluidDisplay {

    public static class Server implements GuiComponent.Server<Data> {

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
            return GuiComponents.LARGE_TANK_FLUID_DISPLAY;
        }
    }

    public static record Data(FluidVariant fluid, long amount, long capacity) {
    }

}

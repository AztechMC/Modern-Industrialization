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
package aztech.modern_industrialization.nuclear;

import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

public interface INuclearTileData {

    double getTemperature();

    double getHeatTransferCoeff();

    double getMeanNeutronAbsorption();

    default Optional<NuclearComponent> getComponent() {
        ItemStack stack = getStack();
        if (!stack.isEmpty() && stack.getItem() instanceof NuclearComponent) {
            return Optional.of((NuclearComponent) stack.getItem());
        }
        return Optional.empty();
    }

    ItemStack getStack();

    static void write(Optional<INuclearTileData> maybeData, PacketByteBuf buf) {

        if (maybeData.isPresent()) {
            INuclearTileData tile = maybeData.get();
            buf.writeBoolean(true);
            buf.writeDouble(tile.getTemperature());
            buf.writeDouble(tile.getMeanNeutronAbsorption());
            buf.writeDouble(tile.getHeatTransferCoeff());

            ItemStack stack = tile.getStack();
            if (stack == null) {
                stack = ItemStack.EMPTY;
            }
            buf.writeItemStack(stack);

        } else {
            buf.writeBoolean(false);
        }

    }

    static Optional<INuclearTileData> read(PacketByteBuf buf) {
        boolean isPresent = buf.readBoolean();
        if (isPresent) {

            final double temperature = buf.readDouble();
            final double meanNeutronAbsorption = buf.readDouble();
            final double heatTransferCoeff = buf.readDouble();
            final ItemStack stack = buf.readItemStack();

            return Optional.of(new INuclearTileData() {

                @Override
                public double getTemperature() {
                    return temperature;
                }

                @Override
                public double getHeatTransferCoeff() {
                    return heatTransferCoeff;
                }

                @Override
                public double getMeanNeutronAbsorption() {
                    return meanNeutronAbsorption;
                }

                @Override
                public ItemStack getStack() {
                    return stack;
                }
            });

        } else {
            return Optional.empty();
        }
    }

    static boolean areEquals(Optional<INuclearTileData> a, Optional<INuclearTileData> b) {
        if (a.isPresent() != b.isPresent()) {
            return false;
        } else if (a.isPresent()) {
            INuclearTileData A = a.get();
            INuclearTileData B = b.get();
            return A.getTemperature() == B.getTemperature() && A.getHeatTransferCoeff() == B.getTemperature()
                    && A.getMeanNeutronAbsorption() == B.getMeanNeutronAbsorption() && ItemStack.areEqual(A.getStack(), B.getStack());
        } else {
            return true;
        }

    }

}

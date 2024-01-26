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

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public interface INuclearTileData {

    double getTemperature();

    double getHeatTransferCoeff();

    double getMeanNeutronAbsorption(NeutronType type);

    double getMeanNeutronFlux(NeutronType type);

    double getMeanNeutronGeneration();

    double getMeanEuGeneration();

    TransferVariant getVariant();

    long getVariantAmount();

    boolean isFluid();

    @Nullable
    default INuclearComponent<?> getComponent() {
        TransferVariant<?> variant = getVariant();

        if (variant instanceof ItemVariant resource) {
            if (!variant.isBlank() && getVariantAmount() > 0 && resource.getItem() instanceof INuclearComponent<?>comp) {
                return comp;
            }

        } else if (variant instanceof FluidVariant resource) {
            if (!resource.isBlank() && getVariantAmount() > 0) {
                return FluidNuclearComponent.get(resource.getFluid());
            }
        }

        return null;
    }

    static void write(Optional<INuclearTileData> maybeData, FriendlyByteBuf buf) {

        if (maybeData.isPresent()) {
            INuclearTileData tile = maybeData.get();
            buf.writeBoolean(true);

            buf.writeDouble(tile.getTemperature());

            buf.writeDouble(tile.getMeanNeutronAbsorption(NeutronType.FAST));
            buf.writeDouble(tile.getMeanNeutronAbsorption(NeutronType.THERMAL));

            buf.writeDouble(tile.getMeanNeutronFlux(NeutronType.FAST));
            buf.writeDouble(tile.getMeanNeutronFlux(NeutronType.THERMAL));

            buf.writeDouble(tile.getMeanNeutronGeneration());

            buf.writeDouble(tile.getHeatTransferCoeff());
            buf.writeDouble(tile.getMeanEuGeneration());

            buf.writeBoolean(!tile.isFluid());
            buf.writeNbt(tile.getVariant().toNbt());
            buf.writeLong(tile.getVariantAmount());

        } else {
            buf.writeBoolean(false);
        }

    }

    static Optional<INuclearTileData> read(FriendlyByteBuf buf) {
        boolean isPresent = buf.readBoolean();
        if (isPresent) {

            final double temperature = buf.readDouble();

            final double meanFastNeutronAbsorption = buf.readDouble();
            final double meanThermalNeutronAbsorption = buf.readDouble();

            final double meanFastNeutronFlux = buf.readDouble();
            final double meanThermalNeutronFlux = buf.readDouble();

            final double meanNeutronGeneration = buf.readDouble();

            final double heatTransferCoeff = buf.readDouble();
            final double euGeneration = buf.readDouble();

            final boolean isItem = buf.readBoolean();
            final TransferVariant variant = isItem ? ItemVariant.fromNbt(buf.readNbt()) : FluidVariant.fromNbt(buf.readNbt());
            final long amount = buf.readLong();

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
                public double getMeanNeutronAbsorption(NeutronType type) {
                    if (type == NeutronType.FAST)
                        return meanFastNeutronAbsorption;
                    else if (type == NeutronType.THERMAL)
                        return meanThermalNeutronAbsorption;

                    return meanThermalNeutronAbsorption + meanFastNeutronAbsorption;
                }

                @Override
                public double getMeanNeutronFlux(NeutronType type) {
                    if (type == NeutronType.FAST)
                        return meanFastNeutronFlux;
                    else if (type == NeutronType.THERMAL)
                        return meanThermalNeutronFlux;

                    return meanFastNeutronFlux + meanThermalNeutronFlux;
                }

                @Override
                public double getMeanNeutronGeneration() {
                    return meanNeutronGeneration;
                }

                @Override
                public double getMeanEuGeneration() {
                    return euGeneration;
                }

                @Override
                public TransferVariant getVariant() {
                    return variant;
                }

                @Override
                public long getVariantAmount() {
                    return amount;
                }

                @Override
                public boolean isFluid() {
                    return !isItem;
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
            for (NeutronType type : NeutronType.TYPES) {
                if (A.getMeanNeutronAbsorption(type) != B.getMeanNeutronAbsorption(type)) {
                    return false;
                }
                if (A.getMeanNeutronFlux(type) != B.getMeanNeutronFlux(type)) {
                    return false;
                }
            }
            return A.getTemperature() == B.getTemperature() && A.getHeatTransferCoeff() == B.getTemperature()
                    && A.getVariantAmount() == B.getVariantAmount() && A.getMeanNeutronGeneration() == B.getMeanNeutronGeneration()
                    && A.getVariant().equals(B.getVariant()) && A.getMeanEuGeneration() == B.getMeanEuGeneration();
        } else {
            return true;
        }

    }

}

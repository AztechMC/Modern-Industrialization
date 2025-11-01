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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import aztech.modern_industrialization.nuclear.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;

public record NuclearReactorGui(Supplier<Data> dataSupplier) implements GuiComponentServer<Unit, NuclearReactorGui.Data> {
    public static final Type<Unit, Data> TYPE = new Type<>(MI.id("nuclear_reactor_gui"), StreamCodec.unit(Unit.INSTANCE), Data.STREAM_CODEC);

    @Override
    public Unit getParams() {
        return Unit.INSTANCE;
    }

    @Override
    public Data extractData() {
        return dataSupplier.get();
    }

    @Override
    public Type<Unit, Data> getType() {
        return TYPE;
    }

    final private static double neutronsMax = 8192;

    public static double neutronColorScheme(double neutronNumber) {
        neutronNumber = Math.min(neutronNumber, neutronsMax);
        return Math.log(1 + 10 * neutronNumber) / Math.log(1 + 10 * neutronsMax);
    }

    public record Data(
            boolean valid,
            int gridSizeX, int gridSizeY,
            List<TileData> tilesData,
            double euProduction, double euFuelConsumption) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                Data::valid,
                ByteBufCodecs.VAR_INT,
                Data::gridSizeX,
                ByteBufCodecs.VAR_INT,
                Data::gridSizeY,
                TileData.STREAM_CODEC.apply(ByteBufCodecs.list()),
                Data::tilesData,
                ByteBufCodecs.DOUBLE,
                Data::euProduction,
                ByteBufCodecs.DOUBLE,
                Data::euFuelConsumption,
                Data::new);

        public int toIndex(int x, int y) {
            return toIndex(x, y, gridSizeY);
        }

        public static int toIndex(int x, int y, int sizeY) {
            return x * sizeY + y;
        }
    }

    /**
     * Wrapper record for {@link NuclearTileData} to have a suitable equals implementation.
     */
    public record TileData(Optional<NuclearTileData> data) {
        public static final StreamCodec<RegistryFriendlyByteBuf, TileData> STREAM_CODEC = StreamCodec.ofMember(NuclearTileData::write, NuclearTileData::read)
                .map(TileData::new, TileData::data);

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TileData(var odata) && NuclearTileData.areEquals(this.data, odata);
        }
    }
}

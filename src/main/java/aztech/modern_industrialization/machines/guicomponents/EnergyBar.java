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
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class EnergyBar implements GuiComponentServer<EnergyBar.Params, EnergyBar.Data> {
    public static final Type<Params, Data> TYPE = new Type<>(MI.id("energy_bar"), Params.STREAM_CODEC, Data.STREAM_CODEC);

    public final Params params;
    public final Supplier<Long> euSupplier, maxEuSupplier;

    public EnergyBar(Params params, Supplier<Long> euSupplier, Supplier<Long> maxEuSupplier) {
        this.params = params;
        this.euSupplier = euSupplier;
        this.maxEuSupplier = maxEuSupplier;
    }

    @Override
    public Params getParams() {
        return params;
    }

    @Override
    public Data extractData() {
        return new Data(euSupplier.get(), maxEuSupplier.get());
    }

    @Override
    public Type<Params, Data> getType() {
        return TYPE;
    }

    public record Params(int renderX, int renderY) {
        public static final StreamCodec<FriendlyByteBuf, Params> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                Params::renderX,
                ByteBufCodecs.VAR_INT,
                Params::renderY,
                Params::new);
    }

    public record Data(long eu, long maxEu) {
        public static final StreamCodec<FriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG,
                Data::eu,
                ByteBufCodecs.VAR_LONG,
                Data::maxEu,
                Data::new);
    }
}

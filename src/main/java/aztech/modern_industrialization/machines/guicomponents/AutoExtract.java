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
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

/**
 * Supports both auto-extract and auto-insert. Auto-insert is just a GUI change,
 * but the logic stays the same.
 */
public class AutoExtract implements GuiComponentServer<AutoExtract.Params, AutoExtract.Data> {
    public static final Type<Params, Data> TYPE = new Type<>(MI.id("auto_extract"), Params.STREAM_CODEC, Data.STREAM_CODEC);

    private final OrientationComponent orientation;
    private final Display display;

    public AutoExtract(OrientationComponent orientation, Display display) {
        this.orientation = orientation;
        this.display = display;
    }

    public AutoExtract(OrientationComponent orientation, boolean displayAsInsert) {
        this(orientation, displayAsInsert ? Display.INSERT : Display.EXTRACT);
    }

    public AutoExtract(OrientationComponent orientation) {
        this(orientation, false);
    }

    @Override
    public Params getParams() {
        return new Params(display, orientation.params.hasExtractItems, orientation.params.hasExtractFluids);
    }

    @Override
    public Data extractData() {
        return new Data(orientation.extractItems, orientation.extractFluids);
    }

    @Override
    public Type<Params, Data> getType() {
        return TYPE;
    }

    public OrientationComponent getOrientation() {
        return orientation;
    }

    public enum Display {
        EXTRACT,
        INSERT,
        BOTH,
    }

    public record Params(Display display, boolean hasExtractItems, boolean hasExtractFluids) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Params> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(Display.class),
                Params::display,
                ByteBufCodecs.BOOL,
                Params::hasExtractItems,
                ByteBufCodecs.BOOL,
                Params::hasExtractFluids,
                Params::new);
    }

    public record Data(boolean extractItems, boolean extractFluids) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                Data::extractItems,
                ByteBufCodecs.BOOL,
                Data::extractFluids,
                Data::new);
    }
}

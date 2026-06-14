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
import aztech.modern_industrialization.util.Rectangle;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class ProgressBar implements GuiComponentServer<ProgressBar.Params, Float> {
    public static final Type<Params, Float> TYPE = new Type<>(MI.id("progress_bar"), Params.STREAM_CODEC, ByteBufCodecs.FLOAT);

    private final Params params;
    private final Supplier<Float> progressSupplier;

    public ProgressBar(Params params, Supplier<Float> progressSupplier) {
        this.params = params;
        this.progressSupplier = progressSupplier;
    }

    @Override
    public Params getParams() {
        return params;
    }

    @Override
    public Float extractData() {
        return progressSupplier.get();
    }

    @Override
    public Type<Params, Float> getType() {
        return TYPE;
    }

    /**
     * @param progressBarType The real path will be
     *                        {@code modern_industrialization:textures/gui/progress_bar/<progressBarType>.png}.
     *                        Must have a size of width x 2*height.
     */
    public record Params(int renderX, int renderY, String progressBarType, int width, int height, boolean isVertical) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Params> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                Params::renderX,
                ByteBufCodecs.VAR_INT,
                Params::renderY,
                ByteBufCodecs.STRING_UTF8,
                Params::progressBarType,
                ByteBufCodecs.VAR_INT,
                Params::width,
                ByteBufCodecs.VAR_INT,
                Params::height,
                ByteBufCodecs.BOOL,
                Params::isVertical,
                Params::new);

        public Params(int renderX, int renderY, String progressBarType) {
            this(renderX, renderY, progressBarType, false);
        }

        public Params(int renderX, int renderY, String progressBarType, boolean isVertical) {
            this(renderX, renderY, progressBarType, 20, 20, isVertical);
        }

        public Params {
            if (width < 2 || height < 2) {
                throw new IllegalArgumentException("Width and height must be at least 2, currently " + width + " and " + height);
            }
        }

        public ResourceLocation getTextureId() {
            return MI.id("textures/gui/progress_bar/" + progressBarType + ".png");
        }

        public int textureHeight() {
            return 2 * height;
        }

        public Rectangle toRectangle() {
            return new Rectangle(renderX, renderY, width, height);
        }
    }
}

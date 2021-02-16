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
package aztech.modern_industrialization.textures.coloramp;

import aztech.modern_industrialization.textures.TextureHelper;
import java.util.function.Function;

public class MixedColoramp implements Coloramp {

    private final Coloramp coloramp1, coloramp2;
    private final Function<Double, Double> mixer;
    private final double meanMix;

    public MixedColoramp(Coloramp coloramp1, Coloramp coloramp2, Function<Double, Double> mixer, double meanMix) {
        this.coloramp1 = coloramp1;
        this.coloramp2 = coloramp2;
        this.mixer = mixer;
        this.meanMix = meanMix;
    }

    @Override
    public int getRGB(double luminance) {
        return TextureHelper.mixRGB(coloramp1.getRGB(luminance), coloramp2.getRGB(luminance), mixer.apply(luminance));
    }

    @Override
    public int getMeanRGB() {
        return TextureHelper.mixRGB(coloramp1.getMeanRGB(), coloramp2.getMeanRGB(), meanMix);
    }
}

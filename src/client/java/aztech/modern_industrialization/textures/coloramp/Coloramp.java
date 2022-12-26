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

import static aztech.modern_industrialization.textures.TextureHelper.*;

import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.property.ColorampParameters;
import aztech.modern_industrialization.textures.TextureManager;
import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;

public interface Coloramp {

    public int getRGB(double luminance);

    public int getMeanRGB();

    @Nullable
    static Coloramp of(TextureManager mtm, ColorampParameters parameters, Material material) {
        if (parameters instanceof ColorampParameters.Unspecified) {
            return null;
        } else if (parameters instanceof ColorampParameters.Uniform uniform) {
            return new DefaultColoramp(uniform.rgb());
        } else if (parameters instanceof ColorampParameters.Bakable bakable) {
            return new BakableTargetColoramp(mtm, bakable.meanRGB(), bakable.from(), bakable.target());
        } else if (parameters instanceof ColorampParameters.GradientMap gradientMap) {
            return new GradientMapColoramp(mtm, gradientMap.meanRGB(), material.name);
        } else {
            throw new IllegalArgumentException("Unkown coloramp parameter class: " + parameters.getClass().getSimpleName());
        }
    }

    default NativeImage bakeAsImage() {
        NativeImage image = new NativeImage(256, 256, true);
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                double luminance = (i) / 255.0;
                int rgb = getRGB(luminance);
                int r = getRrgb(rgb);
                int g = getGrgb(rgb);
                int b = getBrgb(rgb);
                image.setPixelRGBA(i, j, fromArgb(255, r, g, b));
            }
        }
        return image;
    }

}

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

package aztech.modern_industrialization.client.textures.coloramp;

import static aztech.modern_industrialization.client.textures.TextureHelper.*;

import aztech.modern_industrialization.client.textures.TextureHelper;
import aztech.modern_industrialization.client.textures.TextureManager;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;

public class Coloramp implements IColoramp {
    private final int[] colors = new int[256];
    private final int meanRGB;

    public Coloramp(TextureManager mtm, int meanRGB, String name) {
        this.meanRGB = meanRGB;

        var gradientMapPath = "modern_industrialization:textures/gradient_maps/" + name + ".png";

        if (mtm.hasAsset(gradientMapPath)) {
            try (NativeImage gradientMap = mtm.getAssetAsTexture(gradientMapPath)) {
                for (int i = 0; i < 256; i++) {
                    int color = gradientMap.getPixelRGBA(i, 0);
                    int r = getR(color);
                    int g = getG(color);
                    int b = getB(color);
                    colors[i] = r << 16 | g << 8 | b;

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            fillUniform();
        }
    }

    public Coloramp(int meanRGB) {
        this.meanRGB = meanRGB;
        fillUniform();
    }

    private void fillUniform() {
        // Use uniform coloramp
        int meanR = TextureHelper.getRrgb(meanRGB);
        int meanG = TextureHelper.getGrgb(meanRGB);
        int meanB = TextureHelper.getBrgb(meanRGB);

        for (int i = 0; i < 256; ++i) {
            colors[i] = TextureHelper.toRGB(meanR * i / 255, meanG * i / 255, meanB * i / 255);
        }
    }

    @Override
    public int getRGB(double luminance) {
        int i = (int) (luminance * 255);
        return colors[i];
    }

    @Override
    public int getMeanRGB() {
        return meanRGB;
    }
}

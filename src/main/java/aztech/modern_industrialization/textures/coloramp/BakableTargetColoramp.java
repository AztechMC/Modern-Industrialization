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
import aztech.modern_industrialization.textures.TextureManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import net.minecraft.client.texture.NativeImage;

public class BakableTargetColoramp implements Coloramp {

    public static final ArrayList<BakableTargetColoramp> bakableTargetColoramps = new ArrayList<>();

    private final int meanRGB;
    private boolean isBaked = false;
    private String from, target;

    private final ArrayList<Double> fromLum = new ArrayList<>();
    private final ArrayList<Integer> toRgb = new ArrayList<>();

    private ArrayList<Double> virtualIndex;
    private ArrayList<Double> virtualIndexRgb;

    public BakableTargetColoramp(int meanRGB, String from, String target) {
        this.meanRGB = meanRGB;
        this.from = from;
        this.target = target;
        bakableTargetColoramps.add(this);
    }

    public BakableTargetColoramp(int meanRGB, String from) {
        this(meanRGB, from, from);
    }

    @Override
    public int getRGB(double luminance) {
        if (!isBaked) {
            throw new IllegalStateException(String.format("Unbaked coloramp with mean RGB of %05X", meanRGB));
        }
        if (luminance > 1.0 || luminance < 0.0) {
            throw new IllegalStateException(String.format("Invalid luminence %f", luminance));
        }

        if (luminance == 0.0) {
            return 0x000000;
        } else if (luminance == 1.0) {
            return 0xFFFFFF;
        }

        int index = Collections.binarySearch(fromLum, luminance);
        if (index < 0) {
            index = -(index + 1);
        }

        double vup = fromLum.get(index);
        double xup = virtualIndex.get(index);
        double vdown = fromLum.get(index - 1);
        double xdown = virtualIndex.get(index - 1);

        // lum = vdown + ((x - xdown)/(xup - xdown))*(vup - vdow)
        // => x = (lum - vdown)*(xup - xdown)/ (vup - vdown) + xdown

        double interpolation = (luminance - vdown) * (xup - xdown) / (vup - vdown) + xdown;

        int r1 = Collections.binarySearch(virtualIndexRgb, interpolation);
        if (r1 < 0) {
            r1 = -(r1 + 1);
        }

        if (r1 == 0) {
            return 0x000000;
        }

        double iup = virtualIndexRgb.get(r1);
        double idown = virtualIndexRgb.get(r1 - 1);

        int r, g, b;

        double frac = (interpolation - idown) / (iup - idown);
        r = (int) (TextureHelper.getR(toRgb.get(r1 - 1)) * (1 - frac) + frac * (TextureHelper.getR(toRgb.get(r1))));
        g = (int) (TextureHelper.getG(toRgb.get(r1 - 1)) * (1 - frac) + frac * (TextureHelper.getG(toRgb.get(r1))));
        b = (int) (TextureHelper.getB(toRgb.get(r1 - 1)) * (1 - frac) + frac * (TextureHelper.getB(toRgb.get(r1))));

        return TextureHelper.toRGB(r, g, b);
    }

    @Override
    public int getMeanRGB() {
        return meanRGB;
    }

    public void baked(TextureManager textureManager) {
        try {
            NativeImage from = textureManager.getAssetAsTexture(this.from);
            NativeImage to = textureManager.getAssetAsTexture(this.target);

            double maxFromLum = 0.0;

            for (int i = 0; i < from.getWidth(); i++) {
                for (int j = 0; j < from.getHeight(); j++) {
                    int argb = from.getColor(i, j);
                    int a = TextureHelper.getA(argb);
                    if (a >= 127) {
                        double lum = TextureHelper.getLuminance(argb);
                        maxFromLum = Math.max(lum, maxFromLum);
                        fromLum.add(lum);
                    }
                }
            }

            fromLum.add(0.0);
            fromLum.add(1.0);

            Collections.sort(fromLum);

            virtualIndex = new ArrayList<>();
            ArrayList<Double> fromLum2 = new ArrayList<>();

            {
                int i = 0;
                while (i < fromLum.size()) {

                    double s = fromLum.get(i);
                    double index = ((double) i) / (fromLum.size() - 1);

                    int j = i;

                    while (j + 1 < fromLum.size() && fromLum.get(j + 1) == s) {
                        j++;
                        index += 0.5 / (fromLum.size() - 1);
                    }

                    fromLum2.add(s);
                    virtualIndex.add(index);
                    i = j + 1;
                }
            }

            fromLum.clear();
            fromLum.addAll(fromLum2);

            int rgbMaxLum = 0;
            double maxToLum = 0.0;

            for (int i = 0; i < to.getWidth(); i++) {
                for (int j = 0; j < to.getHeight(); j++) {
                    int argb = to.getColor(i, j);
                    int a = TextureHelper.getA(argb);
                    if (a >= 127) {
                        toRgb.add(argb);
                        double lum = TextureHelper.getLuminance(argb);
                        if (lum > maxToLum) {
                            maxToLum = lum;
                            rgbMaxLum = argb;
                        }
                    }
                }
            }

            int r = Math.min((int) (1 / maxFromLum * TextureHelper.getR(rgbMaxLum)), 255);
            int g = Math.min((int) (1 / maxFromLum * TextureHelper.getG(rgbMaxLum)), 255);
            int b = Math.min((int) (1 / maxFromLum * TextureHelper.getB(rgbMaxLum)), 255);

            toRgb.add(0xFF000000);
            toRgb.add(TextureHelper.fromArgb(255, r, g, b));

            Collections.sort(toRgb, Comparator.comparingDouble(TextureHelper::getLuminance));

            virtualIndexRgb = new ArrayList<>();
            ArrayList<Integer> toRgb2 = new ArrayList<>();

            {
                int i = 0;
                while (i < toRgb.size()) {

                    int rgb = toRgb.get(i);
                    double index = ((double) i) / (toRgb.size() - 1);

                    int j = i;

                    while (j + 1 < toRgb.size() && toRgb.get(j + 1) == rgb) {
                        j++;
                        index += 0.5 / (toRgb.size() - 1);
                    }

                    toRgb2.add(rgb);
                    virtualIndexRgb.add(index);
                    i = j + 1;
                }
            }

            toRgb.clear();
            toRgb.addAll(toRgb2);

            double index0 = virtualIndexRgb.get(0);

            for (int i = 0; i < virtualIndexRgb.size(); i++) {
                virtualIndexRgb.set(i, virtualIndexRgb.get(i) - index0);
            }

            double indexEnd = virtualIndexRgb.get(virtualIndexRgb.size() - 1);

            for (int i = 0; i < virtualIndexRgb.size(); i++) {
                virtualIndexRgb.set(i, virtualIndexRgb.get(i) / indexEnd);
            }

            if (fromLum.size() <= 1 || toRgb.size() <= 1) {
                throw new IllegalStateException(
                        String.format("Could not correctly processed the coloramp from = %s, to = %s", this.from, this.target));
            }
            isBaked = true;
            from.close();
            to.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

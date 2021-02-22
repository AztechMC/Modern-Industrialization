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
package aztech.modern_industrialization.textures;

import aztech.modern_industrialization.textures.coloramp.Coloramp;
import aztech.modern_industrialization.textures.coloramp.DefaultColoramp;
import java.awt.*;
import java.util.function.BiFunction;
import net.minecraft.client.texture.NativeImage;

public class TextureHelper {

    public static void colorize(NativeImage image, Coloramp colorramp) {
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                int color = image.getPixelColor(i, j);
                double l = getLuminance(color);
                int rgb = colorramp.getRGB(l);
                int r = getRrgb(rgb);
                int g = getGrgb(rgb);
                int b = getBrgb(rgb);
                image.setPixelColor(i, j, fromArgb(getA(color), r, g, b));
            }
        }
    }

    public static void increaseBrightness(NativeImage image, float minBrightness) {
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                int color = image.getPixelColor(i, j);
                double l = getLuminance(color);
                int r = getR(color);
                int g = getG(color);
                int b = getB(color);

                int rgb = inecreaseBrightness(toRGB(r, g, b), minBrightness);
                r = getRrgb(rgb);
                g = getGrgb(rgb);
                b = getBrgb(rgb);

                image.setPixelColor(i, j, fromArgb(getA(color), r, g, b));
            }
        }
    }

    public static double getLuminance(int color) {
        return (0.2126 * getR(color) + 0.7152 * getG(color) + 0.0722 * getB(color)) / 255;
    }

    public static int mixRGB(int rgb1, int rgb2, double fact) {
        int r1 = getRrgb(rgb1);
        int r2 = getRrgb(rgb2);
        int g1 = getGrgb(rgb1);
        int g2 = getGrgb(rgb2);
        int b1 = getBrgb(rgb1);
        int b2 = getBrgb(rgb2);

        return toRGB((int) (fact * r1 + (1 - fact) * r2), ((int) (fact * g1 + (1 - fact) * g2)), ((int) (fact * b1 + (1 - fact) * b2)));
    }

    public static int setHue(int rgb, float hue) {
        float[] hsbval = new float[3];
        Color.RGBtoHSB(getRrgb(rgb), getGrgb(rgb), getBrgb(rgb), hsbval);
        return 0xFFFFFF & Color.HSBtoRGB(hue, hsbval[1], hsbval[2]);
    }

    public static int inecreaseBrightness(int rgb, float minBrightness) {
        float[] hsbval = new float[3];
        Color.RGBtoHSB(getRrgb(rgb), getGrgb(rgb), getBrgb(rgb), hsbval);
        return 0xFFFFFF & Color.HSBtoRGB(hsbval[0], hsbval[1], minBrightness + (1 - minBrightness) * hsbval[2]);
    }

    public static int toRGB(int r, int g, int b) {
        return (r << 16) + (g << 8) + b;
    }

    public static int getRrgb(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int getGrgb(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int getBrgb(int rgb) {
        return rgb & 0xFF;
    }

    public static void colorize(NativeImage image, int rgb) {
        colorize(image, new DefaultColoramp(rgb));
    }

    /**
     * Blend top on top of source.
     */
    public static void blend(NativeImage source, NativeImage top) {
        if (source.getWidth() != top.getWidth()) {
            throw new RuntimeException(
                    "Textures have mismatched widths. Source has width " + source.getWidth() + " and top has width " + top.getWidth());
        }
        if (source.getHeight() != top.getHeight()) {
            throw new RuntimeException(
                    "Textures have mismatched heights. Source has height " + source.getHeight() + " and top has height " + top.getHeight());
        }
        for (int i = 0; i < source.getWidth(); ++i) {
            for (int j = 0; j < source.getHeight(); ++j) {
                int sourceColor = source.getPixelColor(i, j);
                int topColor = top.getPixelColor(i, j);
                double alphaSource = getA(sourceColor) / 255.0;
                double alphaTop = getA(topColor) / 255.0;
                double alphaOut = alphaTop + alphaSource * (1 - alphaTop);
                BiFunction<Integer, Integer, Integer> mergeAlpha = (sourceValue,
                        topValue) -> (int) ((topValue * alphaTop + sourceValue * alphaSource * (1 - alphaTop)) / alphaOut);
                source.setPixelColor(i, j, fromArgb((int) (alphaOut * 255), mergeAlpha.apply(getR(sourceColor), getR(topColor)),
                        mergeAlpha.apply(getG(sourceColor), getG(topColor)), mergeAlpha.apply(getB(sourceColor), getB(topColor))));
            }
        }
    }

    public static void doubleIngot(NativeImage image) {
        // Copy and shift down
        NativeImage lowerIngot = new NativeImage(image.getWidth(), image.getHeight(), true);
        lowerIngot.copyFrom(image);
        int shiftDown = lowerIngot.getHeight() * 2 / 16;
        for (int x = 0; x < lowerIngot.getWidth(); ++x) {
            for (int y = lowerIngot.getHeight(); y-- > 0;) {
                if (y >= shiftDown) {
                    lowerIngot.setPixelColor(x, y, lowerIngot.getPixelColor(x, y - shiftDown));
                } else {
                    lowerIngot.setPixelColor(x, y, 0);
                }
            }
        }
        // Copy and shift up
        NativeImage upperIngot = new NativeImage(image.getWidth(), image.getHeight(), true);
        upperIngot.copyFrom(image);
        int shiftUp = upperIngot.getHeight() * 2 / 16;
        for (int x = 0; x < upperIngot.getWidth(); ++x) {
            for (int y = 0; y < upperIngot.getHeight(); ++y) {
                if (y + shiftUp < upperIngot.getHeight()) {
                    upperIngot.setPixelColor(x, y, upperIngot.getPixelColor(x, y + shiftUp));
                } else {
                    upperIngot.setPixelColor(x, y, 0);
                }
            }
        }
        blend(lowerIngot, upperIngot);
        image.copyFrom(lowerIngot);
        lowerIngot.close();
        upperIngot.close();
    }

    public static int getA(int color) {
        return (color >> 24) & 0xff;
    }

    public static int getR(int color) {
        return color & 0xff;
    }

    public static int getG(int color) {
        return (color >> 8) & 0xff;
    }

    public static int getB(int color) {
        return (color >> 16) & 0xff;
    }

    // double values are from 0 to 255!!!!!
    private static int fromArgb(int a, double r, double g, double b) {
        return fromArgb(a, (int) r, (int) g, (int) b);
    }

    private static int fromArgb(int a, int r, int g, int b) {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static void flip(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int flipped[][] = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                flipped[i][height - j - 1] = image.getPixelColor(i, j);
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                image.setPixelColor(i, j, flipped[i][j]);
            }
        }

    }

    public static NativeImage copy(NativeImage image) {
        NativeImage copy = new NativeImage(image.getWidth(), image.getHeight(), true);
        copy.copyFrom(image);
        return copy;
    }
}

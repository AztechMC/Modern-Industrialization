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

package aztech.modern_industrialization.client.textures;

import aztech.modern_industrialization.client.textures.coloramp.IColoramp;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class TextureHelper {
    public static void colorize(NativeImage image, IColoramp colorramp) {
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                int color = image.getPixelRGBA(i, j);
                double l = getLuminance(color);
                int rgb = colorramp.getRGB(l);
                int r = getRrgb(rgb);
                int g = getGrgb(rgb);
                int b = getBrgb(rgb);
                image.setPixelRGBA(i, j, fromArgb(getA(color), r, g, b));
            }
        }
    }

    public static void setAlpha(NativeImage image, int alpha) {
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                int color = image.getPixelRGBA(i, j);
                int r = getR(color);
                int g = getG(color);
                int b = getB(color);
                image.setPixelRGBA(i, j, fromArgb(alpha, r, g, b));
            }
        }
    }

    public static void increaseBrightness(NativeImage image, float minBrightness) {
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                int color = image.getPixelRGBA(i, j);
                double l = getLuminance(color);
                int r = getR(color);
                int g = getG(color);
                int b = getB(color);

                int rgb = inecreaseBrightness(toRGB(r, g, b), minBrightness);
                r = getRrgb(rgb);
                g = getGrgb(rgb);
                b = getBrgb(rgb);

                image.setPixelRGBA(i, j, fromArgb(getA(color), r, g, b));
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

    public static int setSaturation(int rgb, float sat) {
        float[] hsbval = new float[3];
        Color.RGBtoHSB(getRrgb(rgb), getGrgb(rgb), getBrgb(rgb), hsbval);
        return 0xFFFFFF & Color.HSBtoRGB(hsbval[0], sat, hsbval[2]);
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

    /**
     * Adjust images to the largest one's resolution.
     */
    public static void adjustDimensions(List<NativeImage> images) {
        int maxWidth = 0;
        int maxHeight = 0;

        for (NativeImage image : images) {
            maxWidth = Math.max(maxWidth, image.getWidth());
            maxHeight = Math.max(maxHeight, image.getHeight());
        }

        for (int imageIndex = 0; imageIndex < images.size(); ++imageIndex) {
            NativeImage image = images.get(imageIndex);

            if (maxWidth % image.getWidth() != 0 || maxHeight % image.getHeight() != 0) {
                String error = String.format("Mismatched dimensions, can't adjust. Max: (%d, %d). Current image: (%d, %d).", maxWidth, maxHeight,
                        image.getWidth(), image.getHeight());
                throw new IllegalArgumentException(error);
            }

            int wFactor = maxWidth / image.getWidth();
            int hFactor = maxHeight / image.getHeight();

            if (wFactor == 1 && hFactor == 1) {
                // Correct size, nothing to do!
                continue;
            }

            // Create a new image.
            NativeImage newImage = new NativeImage(maxWidth, maxHeight, false);

            for (int i = 0; i < maxWidth; ++i) {
                for (int j = 0; j < maxHeight; ++j) {
                    newImage.setPixelRGBA(i, j, image.getPixelRGBA(i / wFactor, j / hFactor));
                }
            }

            images.set(imageIndex, newImage);
        }
    }

    /**
     * Blend top on top of source, return the result.
     */
    public static NativeImage blend(NativeImage originalSource, NativeImage originalTop) {
        // Adjust dimensions
        List<NativeImage> images = Arrays.asList(originalSource, originalTop);
        adjustDimensions(images);
        NativeImage source = images.get(0), top = images.get(1);

        NativeImage output = new NativeImage(source.getWidth(), source.getHeight(), false);

        for (int i = 0; i < source.getWidth(); ++i) {
            for (int j = 0; j < source.getHeight(); ++j) {
                int sourceColor = source.getPixelRGBA(i, j);
                int topColor = top.getPixelRGBA(i, j);
                double alphaSource = getA(sourceColor) / 255.0;
                double alphaTop = getA(topColor) / 255.0;
                double alphaOut = alphaTop + alphaSource * (1 - alphaTop);
                BiFunction<Integer, Integer, Integer> mergeAlpha = (sourceValue,
                        topValue) -> (int) ((topValue * alphaTop + sourceValue * alphaSource * (1 - alphaTop)) / alphaOut);
                output.setPixelRGBA(i, j, fromArgb((int) (alphaOut * 255), mergeAlpha.apply(getR(sourceColor), getR(topColor)),
                        mergeAlpha.apply(getG(sourceColor), getG(topColor)), mergeAlpha.apply(getB(sourceColor), getB(topColor))));
            }
        }

        return output;
    }

    public static NativeImage tripleTexture(NativeImage im1, NativeImage im2, NativeImage im3) {
        return tripleTexture(im1, im2, im3, 2, 2);
    }

    public static NativeImage tripleTexture(NativeImage im1, NativeImage im2, NativeImage im3, int up, int down) {
        // Copy and shift down
        NativeImage lowerIngot = new NativeImage(im1.getWidth(), im1.getHeight(), true);
        lowerIngot.copyFrom(im1);
        int shiftDown = lowerIngot.getHeight() * up / 16;
        for (int x = 0; x < lowerIngot.getWidth(); ++x) {
            for (int y = lowerIngot.getHeight(); y-- > 0;) {
                if (y >= shiftDown) {
                    lowerIngot.setPixelRGBA(x, y, lowerIngot.getPixelRGBA(x, y - shiftDown));
                } else {
                    lowerIngot.setPixelRGBA(x, y, 0);
                }
            }
        }
        // Copy and shift up
        NativeImage upperIngot = new NativeImage(im3.getWidth(), im3.getHeight(), true);
        upperIngot.copyFrom(im3);
        int shiftUp = upperIngot.getHeight() * down / 16;
        for (int x = 0; x < upperIngot.getWidth(); ++x) {
            for (int y = 0; y < upperIngot.getHeight(); ++y) {
                if (y + shiftUp < upperIngot.getHeight()) {
                    upperIngot.setPixelRGBA(x, y, upperIngot.getPixelRGBA(x, y + shiftUp));
                } else {
                    upperIngot.setPixelRGBA(x, y, 0);
                }
            }
        }
        lowerIngot = blend(lowerIngot, im2);
        lowerIngot = blend(lowerIngot, upperIngot);
        upperIngot.close();
        return lowerIngot;
    }

    public static void doubleIngot(NativeImage image) {
        // Copy and shift down
        NativeImage lowerIngot = new NativeImage(image.getWidth(), image.getHeight(), true);
        lowerIngot.copyFrom(image);
        int shiftDown = lowerIngot.getHeight() * 2 / 16;
        for (int x = 0; x < lowerIngot.getWidth(); ++x) {
            for (int y = lowerIngot.getHeight(); y-- > 0;) {
                if (y >= shiftDown) {
                    lowerIngot.setPixelRGBA(x, y, lowerIngot.getPixelRGBA(x, y - shiftDown));
                } else {
                    lowerIngot.setPixelRGBA(x, y, 0);
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
                    upperIngot.setPixelRGBA(x, y, upperIngot.getPixelRGBA(x, y + shiftUp));
                } else {
                    upperIngot.setPixelRGBA(x, y, 0);
                }
            }
        }
        lowerIngot = blend(lowerIngot, upperIngot);
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
    public static int fromArgb(int a, double r, double g, double b) {
        return fromArgb(a, (int) r, (int) g, (int) b);
    }

    public static int fromArgb(int a, int r, int g, int b) {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static void flip(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] flipped = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                flipped[i][height - j - 1] = image.getPixelRGBA(i, j);
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                image.setPixelRGBA(i, j, flipped[i][j]);
            }
        }
    }

    public static NativeImage copy(NativeImage image) {
        NativeImage copy = new NativeImage(image.getWidth(), image.getHeight(), true);
        copy.copyFrom(image);
        return copy;
    }

    public static int getOverlayTextColor(int rgb) {
        double luminance = getLuminance(rgb);
        if (luminance < 0.5) {
            return 0xFFFFFF;
        } else {
            return 0x000000;
        }
    }
}

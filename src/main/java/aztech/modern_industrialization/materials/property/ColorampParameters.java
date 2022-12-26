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
package aztech.modern_industrialization.materials.property;

/**
 * Defines how the coloramp for a material should behave
 */
public sealed interface ColorampParameters {
    record Unspecified() implements ColorampParameters {
        @Override
        public int getMeanRGB() {
            return 0;
        }
    }

    record Uniform(int rgb) implements ColorampParameters {
        @Override
        public int getMeanRGB() {
            return rgb;
        }
    }

    record Bakable(int meanRGB, String from, String target) implements ColorampParameters {
        @Override
        public int getMeanRGB() {
            return meanRGB;
        }
    }

    record GradientMap(int meanRGB) implements ColorampParameters {
        @Override
        public int getMeanRGB() {
            return meanRGB;
        }
    }

    int getMeanRGB();
}

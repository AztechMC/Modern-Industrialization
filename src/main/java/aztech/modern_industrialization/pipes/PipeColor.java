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
package aztech.modern_industrialization.pipes;

public enum PipeColor {
    REGULAR("", 14599002),
    WHITE("white_", 16383998),
    ORANGE("orange_", 16351261),
    MAGENTA("magenta_", 13061821),
    LIGHT_BLUE("light_blue_", 3847130),
    YELLOW("yellow_", 16701501),
    LIME("lime_", 8439583),
    PINK("pink_", 15961002),
    GRAY("gray_", 4673362),
    LIGHT_GRAY("light_gray_", 10329495),
    CYAN("cyan_", 1481884),
    PURPLE("purple_", 8991416),
    BLUE("blue_", 3949738),
    BROWN("brown_", 8606770),
    GREEN("green_", 6192150),
    RED("red_", 11546150),
    BLACK("black_", 1908001);

    public final String prefix;
    public final int color;

    PipeColor(String prefix, int color) {
        this.prefix = prefix;
        this.color = color;
    }
}

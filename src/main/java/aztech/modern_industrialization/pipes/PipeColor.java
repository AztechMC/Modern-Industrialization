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
    REGULAR(14599002),
    WHITE("White", "white", 16383998),
    LIGHT_GRAY("Light Gray", "light_gray", 10329495),
    GRAY("Gray", "gray", 4673362),
    BLACK("Black", "black", 1908001),
    BROWN("Brown", "brown", 8606770),
    RED("Red", "red", 11546150),
    ORANGE("Orange", "orange", 16351261),
    YELLOW("Yellow", "yellow", 16701501),
    LIME("Lime", "lime", 8439583),
    GREEN("Green", "green", 6192150),
    CYAN("Cyan", "cyan", 1481884),
    LIGHT_BLUE("Light Blue", "light_blue", 3847130),
    BLUE("Blue", "blue", 3949738),
    PURPLE("Purple", "purple", 8991416),
    MAGENTA("Magenta", "magenta", 13061821),
    PINK("Pink", "pink", 15961002);

    public final int color;
    public final String englishName;
    public final String name;
    public final String englishNamePrefix;
    public final String prefix;

    PipeColor(String englishName, String name, int color) {
        this.englishName = englishName;
        this.name = name;
        this.color = color;
        this.englishNamePrefix = englishName + " ";
        this.prefix = name + "_";
    }

    PipeColor(int color) {
        this.englishName = "";
        this.name = "";
        this.englishNamePrefix = "";
        this.prefix = "";
        this.color = color;
    }
}

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
package aztech.modern_industrialization.util;

import aztech.modern_industrialization.textures.TextureHelper;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class TextHelper {
    public static final Style GRAY_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
    public static final Style UPGRADE_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xc3ff9c));
    public static final Style EU_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xffde7d));
    public static final Style WATER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0x3264ff));
    public static final Style WARNING_TEXT = Style.EMPTY.withColor(Formatting.RED);
    public static final Style MAX_TEMP_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xd94a1e));
    public static final Style HEAT_CONDUCTION = Style.EMPTY.withColor(TextColor.fromRgb(0x0073ba));
    public static final Style NEUTRONS = Style.EMPTY.withColor(TextColor.fromRgb(0x29a329));
    public static final Style YELLOW_BOLD = Style.EMPTY.withColor(Formatting.YELLOW).withBold(true);
    public static final Style YELLOW = Style.EMPTY.withColor(Formatting.YELLOW);
    public static final Style FAQ_HEADER_TOOLTIP = Style.EMPTY.withColor(TextColor.fromRgb(0xf5c42d)).withBold(true);
    public static final Style FAQ_TOOLTIP = Style.EMPTY.withColor(TextColor.fromRgb(0xf7d25e)).withItalic(true);

    public static final Style RED = Style.EMPTY.withColor(Formatting.RED);
    public static final Style GREEN = Style.EMPTY.withColor(Formatting.GREEN);

    public static String formatEu(double eu) {
        return getEuString(eu) + " " + getEuUnit(eu);
    }

    public static String getEuUnit(double eu) {
        if (eu > 1e12) {
            return "TEU";
        } else if (eu > 1e9) {
            return "GEU";
        } else if (eu > 1e6) {
            return "MEU";
        } else if (eu > 1e4) {
            return "kEU";
        } else {
            return "EU";
        }
    }

    public static int getOverlayTextColor(int rgb) {
        double luminance = TextureHelper.getLuminance(rgb);
        if (luminance < 0.5) {
            return 0xFFFFFF;
        } else {
            return 0x000000;
        }
    }

    public static String getEuString(long eu) {
        double div = 1;
        if (eu > 1e12) {
            div = 1e12;
        } else if (eu > 1e9) {
            div = 1e9;
        } else if (eu > 1e6) {
            div = 1e6;
        } else if (eu > 1e4) {
            div = 1e3;
        } else {
            return "" + eu;
        }
        return String.format("%.1f", ((double) eu) / div);
    }

    public static String getEuString(double eu) {
        double div = 1;
        if (eu > 1e12) {
            div = 1e12;
        } else if (eu > 1e9) {
            div = 1e9;
        } else if (eu > 1e6) {
            div = 1e6;
        } else if (eu > 1e4) {
            div = 1e3;
        }
        return String.format("%.1f", eu / div);
    }
}

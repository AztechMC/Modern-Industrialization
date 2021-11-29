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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class TextHelper {
    public static final Style GRAY_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
    public static final Style GRAY_TEXT_NOT_ITALIC = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false);
    public static final Style UPGRADE_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xc3ff9c));
    public static final Style NUMBER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xffde7d)).withItalic(false);
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

    public static int getOverlayTextColor(int rgb) {
        double luminance = TextureHelper.getLuminance(rgb);
        if (luminance < 0.5) {
            return 0xFFFFFF;
        } else {
            return 0x000000;
        }
    }

    public record Amount(String digit, String unit) {
    };

    public record MaxedAmount(String digit, String maxDigit, String unit) {
    };

    public static final String[] units = new String[] { "k", "M", "G", "T", "P", "E" };
    public static final long[] nums = new long[] { 1000L, 1000_000L, 1000_000_000L, 1000_000_000_000L, 1000_000_000_000_000L,
            1000_000_000_000_000_000L };

    public static String getAmount(double amount, long num) {
        double fract = amount / num;
        if (fract < 10) {
            return String.format("%.3f", fract);
        } else if (fract < 100) {
            return String.format("%.2f", fract);
        } else {
            return String.format("%.1f", fract);
        }

    }

    public static Amount getAmount(double amount) {
        if (amount < 10000) {
            return new Amount(getAmount(amount, 1), "");
        } else {
            int i = 0;
            while (amount / nums[i] >= 1000) {
                i++;
            }
            return new Amount(getAmount(amount, nums[i]), units[i]);
        }
    }

    public static MaxedAmount getMaxedAmount(double amount, double max) {
        if (max < 10000) {
            return new MaxedAmount(getAmount(amount, 1), getAmount(max, 1), "");
        } else {
            int i = 0;
            while (max / nums[i] >= 1000) {
                i++;
            }
            return new MaxedAmount(getAmount(amount, nums[i]), getAmount(max, nums[i]), units[i]);
        }
    }

    public static Amount getAmount(long amount) {
        if (amount < 10000) {
            return new Amount(String.valueOf(amount), "");
        } else {
            int i = 0;
            while (amount / nums[i] >= 1000) {
                i++;
            }
            return new Amount(getAmount(amount, nums[i]), units[i]);
        }
    }

    public static MaxedAmount getMaxedAmount(long amount, long max) {
        if (max < 10000) {
            return new MaxedAmount(String.valueOf(amount), String.valueOf(max), "");
        } else {
            int i = 0;
            while (max / nums[i] >= 1000) {
                i++;
            }
            return new MaxedAmount(getAmount(amount, nums[i]), getAmount(max, nums[i]), units[i]);
        }
    }

    public static MutableText getEuTextMaxed(long eu, long max) {
        var amount = getMaxedAmount(eu, max);
        return new TranslatableText("text.modern_industrialization.eu_maxed", amount.digit(), amount.maxDigit(), amount.unit());
    }

    public static MutableText getEuText(double eu) {
        var amount = getAmount(eu);
        return new TranslatableText("text.modern_industrialization.eu", amount.digit(),
                amount.unit());
    }

    public static MutableText getEuTextTick(double eu) {
        var amount = getAmount(eu);
        return new TranslatableText("text.modern_industrialization.eu_t", amount.digit(),
                amount.unit());
    }

    public static MutableText getEuText(long eu) {
        var amount = getAmount(eu);
        return new TranslatableText("text.modern_industrialization.eu", amount.digit(),
                amount.unit());
    }

    public static MutableText getEuTextTick(long eu) {
        var amount = getAmount(eu);
        return new TranslatableText("text.modern_industrialization.eu_t", amount.digit(),
                amount.unit());
    }

    public static Text getEuText(long eu, boolean style) {
        MutableText text = getEuText(eu);
        if (style) {
            text.setStyle(TextHelper.NUMBER_TEXT);
        }
        return text;
    }

    public static Text getEuTextTick(long eu, boolean style) {
        MutableText text = getEuTextTick(eu);
        if (style) {
            text.setStyle(TextHelper.NUMBER_TEXT);
        }
        return text;
    }

    public static Text getEuTextTick(double eu, boolean style) {
        MutableText text = getEuTextTick(eu);
        if (style) {
            text.setStyle(TextHelper.NUMBER_TEXT);
        }
        return text;
    }

    public static MutableText getEuTextMaxed(long eu, long max, boolean style) {
        MutableText text = getEuTextMaxed(eu, max);
        if (style) {
            text.setStyle(TextHelper.NUMBER_TEXT);
        }
        return text;
    }

    public static MutableText formatWithNumber(String translationKey, long... numbers) {
        List<Text> numberText = Arrays.stream(numbers).mapToObj(n -> new LiteralText("" + n).setStyle(TextHelper.NUMBER_TEXT))
                .collect(Collectors.toList());
        return new TranslatableText(translationKey, numberText.toArray());
    }

    public static Text getEuStorageTooltip(long totalEu) {
        return new TranslatableText("text.modern_industrialization.base_eu_total_stored", getEuText(totalEu, true)).setStyle(TextHelper.GRAY_TEXT);
    }

}

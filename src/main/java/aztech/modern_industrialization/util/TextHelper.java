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

import aztech.modern_industrialization.MIText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class TextHelper {
    public static final Style GRAY_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
    public static final Style NUMBER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xffde7d)).withItalic(false);
    public static final Style WATER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0x3264ff));
    public static final Style WARNING_TEXT = Style.EMPTY.withColor(ChatFormatting.RED);
    public static final Style MAX_TEMP_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xd94a1e));
    public static final Style HEAT_CONDUCTION = Style.EMPTY.withColor(TextColor.fromRgb(0x0073ba));
    public static final Style NEUTRONS = Style.EMPTY.withColor(TextColor.fromRgb(0x29a329));
    public static final Style YELLOW_BOLD = Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true);
    public static final Style YELLOW = Style.EMPTY.withColor(ChatFormatting.YELLOW);

    public static final Style RED = Style.EMPTY.withColor(ChatFormatting.RED);
    public static final Style GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);

    public record Amount(String digit, String unit) {
    }

    ;

    public record MaxedAmount(String digit, String maxDigit, String unit) {
    }

    ;

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

    public static Amount getAmountGeneric(Number number) {
        if (number instanceof Long l) {
            return getAmount(l);
        } else if (number instanceof Integer i) {
            return getAmount(i);
        } else if (number instanceof Double d) {
            return getAmount(d);
        } else if (number instanceof Float f) {
            return getAmount(f);
        }
        throw new IllegalArgumentException("Number " + number + " is neither long, int, double or float");
    }

    public static Amount getAmount(double amount) {
        if (amount < 100000) {
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
        if (max < 1000) {
            return new MaxedAmount(getAmount(amount, 1), getAmount(max, 1), "");
        } else {
            int i = 0;
            while (max / nums[i] >= 1000) {
                i++;
            }
            return new MaxedAmount(getAmount(amount, nums[i]), getAmount(max, nums[i]), units[i]);
        }
    }

    public static MaxedAmount getMaxedAmountGeneric(Number amount, Number max) {
        if (amount instanceof Long l && max instanceof Long m) {
            return getMaxedAmount(l, m);
        } else if (amount instanceof Integer i && max instanceof Integer m) {
            return getMaxedAmount(i, m);
        } else if (amount instanceof Double d && max instanceof Double m) {
            return getMaxedAmount(d, m);
        } else if (amount instanceof Float f && max instanceof Float m) {
            return getMaxedAmount(f, m);
        }
        throw new IllegalArgumentException("Number " + amount + " or " + max + " is neither long, int, double or float");
    }

    public static Amount getAmount(long amount) {
        if (amount < 100000) {
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
        if (max < 100000) {
            return new MaxedAmount(String.valueOf(amount), String.valueOf(max), "");
        } else {
            int i = 0;
            while (max / nums[i] >= 1000) {
                i++;
            }
            return new MaxedAmount(getAmount(amount, nums[i]), getAmount(max, nums[i]), units[i]);
        }
    }

    public static MutableComponent getEuTextMaxed(Number eu, Number max) {
        var amount = getMaxedAmountGeneric(eu, max);
        return MIText.EuMaxed.text(amount.digit(), amount.maxDigit(), amount.unit());
    }

    public static MutableComponent getEuText(double eu) {
        var amount = getAmount(eu);
        return MIText.Eu.text(amount.digit(),
                amount.unit());
    }

    public static MutableComponent getEuTextTick(double eu) {
        var amount = getAmount(eu);
        return MIText.EuT.text(amount.digit(),
                amount.unit());
    }

    public static MutableComponent getEuText(long eu) {
        var amount = getAmount(eu);
        return MIText.Eu.text(amount.digit(),
                amount.unit());
    }

    public static MutableComponent getEuTextTick(long eu) {
        var amount = getAmount(eu);
        return MIText.EuT.text(amount.digit(),
                amount.unit());
    }

    public static Component getEuTextTick(double eu, boolean style) {
        MutableComponent text = getEuTextTick(eu);
        if (style) {
            text.setStyle(TextHelper.NUMBER_TEXT);
        }
        return text;
    }

}

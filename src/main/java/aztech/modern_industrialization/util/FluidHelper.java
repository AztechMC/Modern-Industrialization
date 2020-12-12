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

import dev.technici4n.fasttransferlib.api.fluid.FluidTextHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;

public class FluidHelper {
    public static Text getFluidName(Fluid fluid, boolean grayIfEmpty) {
        if (fluid == Fluids.EMPTY) {
            Style style = grayIfEmpty ? Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true) : Style.EMPTY;
            return new TranslatableText("text.modern_industrialization.fluid_slot_empty").setStyle(style);
        } else {
            return fluid.getDefaultState().getBlockState().getBlock().getName();
        }
    }

    @Environment(EnvType.CLIENT)
    public static Text getFluidAmount(long amount, long capacity) {
        String text = FluidTextHelper.getUnicodeMillibuckets(amount, false) + " / " + capacity / 81;
        return new TranslatableText("text.modern_industrialization.fluid_slot_quantity", text);
    }

    private static final char[] SUPERSCRIPT = new char[] { '\u2070', '\u00b9', '\u00b2', '\u00b3', '\u2074', '\u2075', '\u2076', '\u2077', '\u2078',
            '\u2079' };
    private static final char FRACTION_BAR = '\u2044';
    private static final char[] SUBSCRIPT = new char[] { '\u2080', '\u2081', '\u2082', '\u2083', '\u2084', '\u2085', '\u2086', '\u2087', '\u2088',
            '\u2089' };

    public static String makeFraction(long num, long denom) {
        StringBuilder numString = new StringBuilder();
        while (num > 0) {
            numString.append(SUPERSCRIPT[(int) (num % 10)]);
            num /= 10;
        }
        StringBuilder denomString = new StringBuilder();
        while (denom > 0) {
            denomString.append(SUBSCRIPT[(int) (denom % 10)]);
            denom /= 10;
        }
        return numString.reverse().toString() + FRACTION_BAR + denomString.reverse().toString();
    }
}

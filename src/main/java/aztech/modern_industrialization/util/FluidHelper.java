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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidKeyRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.minecraft.text.*;

public class FluidHelper {
    public static Text getFluidName(FluidKey fluid, boolean grayIfEmpty) {
        if (fluid.isEmpty()) {
            Style style = grayIfEmpty ? Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true) : Style.EMPTY;
            return new TranslatableText("text.modern_industrialization.empty").setStyle(style);
        } else {
            return FluidKeyRendering.getName(fluid);
        }
    }

    @Environment(EnvType.CLIENT)
    public static MutableText getFluidAmount(long amount, long capacity) {
        String text = FluidTextHelper.getUnicodeMillibuckets(amount, false) + " / " + capacity / 81;
        return new TranslatableText("text.modern_industrialization.fluid_slot_quantity", text);
    }

    public static int getColorMinLuminance(int color) {
        int r = (color & 0xFF);
        int g = (color & 0xFF00) >> 8;
        int b = (color & 0xFF0000) >> 16;
        double lum = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255d;
        if (lum < 0.3) {
            if (lum == 0) {
                return 0x4C4C4C;
            } else {
                r = Math.min((int) (r * 0.3 / lum), 255);
                g = Math.min((int) (g * 0.3 / lum), 255);
                b = Math.min((int) (b * 0.3 / lum), 255);
                return r + (g << 8) + (b << 16);
            }
        } else {
            return color;
        }
    }
}

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

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.*;

public class FluidHelper {

    public static Text getFluidName(FluidVariant fluid, boolean grayIfEmpty) {
        if (fluid.isBlank()) {
            Style style = grayIfEmpty ? Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true) : Style.EMPTY;
            return new TranslatableText("text.modern_industrialization.empty").setStyle(style);
        } else {
            return FluidVariantRendering.getName(fluid);
        }
    }

    public static List<Text> getTooltip(FluidVariant fluid, boolean grayIfEmpty) {

        if (fluid.isBlank()) {
            ArrayList<Text> list = new ArrayList();
            list.add(getFluidName(fluid, grayIfEmpty));
            return list;
        }
        return FluidVariantRendering.getTooltip(fluid,
                MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
    }

    public static List<Text> getTooltipForFluidStorage(FluidVariant fluid, long amount, long capacity, boolean grayIfEmpty) {
        List<Text> tooltip = FluidHelper.getTooltip(fluid, grayIfEmpty);
        tooltip.add(FluidHelper.getFluidAmount(amount, capacity));
        return tooltip;
    }

    public static List<Text> getTooltipForFluidStorage(FluidVariant fluid, long amount, long capacity) {
        return getTooltipForFluidStorage(fluid, amount, capacity, true);
    }

    @Environment(EnvType.CLIENT)
    public static MutableText getFluidAmount(long amount, long capacity) {
        if (capacity < 100 * FluidConstants.BUCKET || Screen.hasShiftDown()) {
            String text = FluidTextHelper.getUnicodeMillibuckets(amount, false) + " / " + capacity / 81;
            return new LiteralText(text + " mB");
        } else {
            RessourceTextHelper.MaxedAmount maxedAmount = RessourceTextHelper.getMaxedAmount((double) amount / FluidConstants.BUCKET,
                    (double) capacity / FluidConstants.BUCKET);
            return new LiteralText(maxedAmount.digit() + " / " + maxedAmount.maxDigit() + " " + maxedAmount.unit() + "B");
        }

    }

    @Environment(EnvType.CLIENT)
    public static MutableText getFluidAmount(long amount) {
        if (amount < 100 * FluidConstants.BUCKET || Screen.hasShiftDown()) {
            String text = FluidTextHelper.getUnicodeMillibuckets(amount, false);
            return new LiteralText(text + " mB");
        } else {
            RessourceTextHelper.Amount amountUnit = RessourceTextHelper.getAmount((double) amount / FluidConstants.BUCKET);
            return new LiteralText(amountUnit.digit() + " " + amountUnit.unit() + "B");
        }
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

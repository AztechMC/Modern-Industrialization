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

package aztech.modern_industrialization.compat.viewer.impl;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.util.TextHelper;
import java.text.DecimalFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

public class ViewerUtil {
    private static final DecimalFormat PROBABILITY_FORMAT = new DecimalFormat("#.#");

    @Nullable
    public static Component getProbabilityTooltip(float probability, boolean input) {
        if (probability == 1) {
            return null;
        } else {
            MutableComponent text;
            if (probability == 0) {
                text = MIText.NotConsumed.text();
            } else {
                if (input) {
                    text = MIText.ChanceConsumption.text(PROBABILITY_FORMAT.format(probability * 100));
                } else {
                    text = MIText.ChanceProduction.text(PROBABILITY_FORMAT.format(probability * 100));
                }

            }
            text.setStyle(TextHelper.YELLOW);
            return text;
        }
    }
}

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
package aztech.modern_industrialization.misc.tooltips;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.util.TextHelper;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FaqTooltips {
    private static final Map<Identifier, String[]> TOOLTIPS = new HashMap<>();

    private static void add(Identifier id, String tooltipId, int lineCount) {
        Preconditions.checkArgument(lineCount > 0);

        String[] lines = IntStream.range(0, lineCount).mapToObj(l -> tooltipId + "_" + l).toArray(String[]::new);

        if (TOOLTIPS.put(id, lines) != null) {
            throw new IllegalStateException("Duplicate tooltip registration.");
        }
    }

    public static void init() {
        // init static
    }

    static {
        add(new MIIdentifier("forge_hammer"), "forge_hammer", 2);
        add(new MIIdentifier("stainless_steel_dust"), "stainless_steel_dust", 1);

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            Identifier itemId = Registry.ITEM.getId(stack.getItem());
            String[] tooltipLines = TOOLTIPS.get(itemId);

            if (tooltipLines != null) {
                lines.add(new LiteralText(""));
                if (Screen.hasShiftDown()) {
                    lines.add(new TranslatableText("text.modern_industrialization.additional_tips").setStyle(TextHelper.FAQ_HEADER_TOOLTIP));
                    for (String line : tooltipLines) {
                        TranslatableText text = new TranslatableText("item_tooltip.modern_industrialization." + line);
                        text.setStyle(TextHelper.FAQ_TOOLTIP);
                        lines.add(text);
                    }
                } else {
                    lines.add(new TranslatableText("text.modern_industrialization.additional_tips_shift").setStyle(TextHelper.FAQ_HEADER_TOOLTIP));
                }
            }
        });
    }
}

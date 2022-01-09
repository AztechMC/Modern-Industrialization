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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class FaqTooltips {
    private static final Map<ResourceLocation, String[]> TOOLTIPS = new HashMap<>();

    private static void add(String item, int lineCount) {
        Preconditions.checkArgument(lineCount > 0);

        String[] lines = IntStream.range(0, lineCount).mapToObj(l -> item + "_" + l).toArray(String[]::new);

        if (TOOLTIPS.put(new MIIdentifier(item), lines) != null) {
            throw new IllegalStateException("Duplicate tooltip registration.");
        }
    }

    public static void init() {
        // init static
    }

    static {
        add("forge_hammer", 2);
        add("kanthal_coil", 2);
        add("stainless_steel_dust", 1);
        add("steam_blast_furnace", 2);

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            ResourceLocation itemId = Registry.ITEM.getKey(stack.getItem());
            String[] tooltipLines = TOOLTIPS.get(itemId);

            if (tooltipLines != null) {
                lines.add(new TextComponent(""));
                if (Screen.hasShiftDown()) {
                    lines.add(new TranslatableComponent("text.modern_industrialization.additional_tips").setStyle(TextHelper.FAQ_HEADER_TOOLTIP));
                    for (String line : tooltipLines) {
                        TranslatableComponent text = new TranslatableComponent("item_tooltip.modern_industrialization." + line);
                        text.setStyle(TextHelper.FAQ_TOOLTIP);
                        lines.add(text);
                    }
                } else {
                    lines.add(
                            new TranslatableComponent("text.modern_industrialization.additional_tips_shift").setStyle(TextHelper.FAQ_HEADER_TOOLTIP));
                }
            }
        });
    }
}

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

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MITooltips;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.ItemLike;

public class FaqTooltips {

    public static final Map<String, String> TOOLTIPS_ENGLISH_TRANSLATION = new HashMap<>();

    private static void add(String itemId, String... englishTooltipsLine) {
        int lineCount = englishTooltipsLine.length;

        Preconditions.checkArgument(lineCount > 0);

        String[] translationKey = IntStream.range(0, lineCount).mapToObj(l -> "item_tooltip.modern_industrialization." + itemId + "_" + l)
                .toArray(String[]::new);

        for (int i = 0; i < lineCount; i++) {
            TOOLTIPS_ENGLISH_TRANSLATION.put(translationKey[i], englishTooltipsLine[i]);
        }

        MITooltips.TooltipAttachment.ofMultiline(
                item -> item == Registry.ITEM.get(new MIIdentifier(itemId)),
                itemStack -> Arrays.stream(translationKey).map(s -> new TranslatableComponent(s).withStyle(MITooltips.DEFAULT_STYLE))
                        .collect(Collectors.toList()));
    }

    private static void add(ItemLike item, String... englishTooltipsLine) {
        add(Registry.ITEM.getKey(item.asItem()).getPath(), englishTooltipsLine);
    }

    public static void init() {
        setupAllTooltips();
    }

    private static void setupAllTooltips() {
        add(MIBlock.FORGE_HAMMER, "Use it to increase the yield of your ore blocks early game!",
                "(Use the Steam Mining Drill for an easy to get Silk Touch.)");
        add("kanthal_coil", "Right-click the EBF with a Screwdriver", "to change the coils to Kanthal");
        add("stainless_steel_dust", "Use Slot-Locking with REI to differentiate its recipe from the invar dust");
        add("steam_blast_furnace", "Needs at least one Steel or higher tier", "hatch for 3 and 4 EU/t recipes");
    }
}

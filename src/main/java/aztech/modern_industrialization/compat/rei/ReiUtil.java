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
package aztech.modern_industrialization.compat.rei;

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.util.FluidTextHelper;
import aztech.modern_industrialization.util.TextHelper;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ReiUtil {

    private static final DecimalFormat PROBABILITY_FORMAT = new DecimalFormat("#.#");

    private ReiUtil() {
    }

    public static EntryIngredient createInputEntries(MachineRecipe.ItemInput input) {
        return EntryIngredient.of(input.getInputItems().stream().map(i -> EntryStacks.of(new ItemStack(i, input.amount))).toList());
    }

    public static EntryStack<?> createItemEntryStack(Item item, int amount, float probability) {
        return EntryStacks.of(new ItemStack(item, amount)).setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilitySetting(probability));
    }

    public static EntryStack<?> createFluidEntryStack(Fluid fluid, long amount, float probability) {
        @Nullable
        Text probabilityText = getProbabilityTooltip(probability);
        return EntryStacks.of(fluid, amount).setting(EntryStack.Settings.TOOLTIP_PROCESSOR, (stack, oldTooltip) -> {
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(FluidVariantRendering.getName(FluidVariant.of(fluid)));
            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_quantity_millibuckets",
                    FluidTextHelper.getUnicodeMillibuckets(amount, false)));
            if (probabilityText != null) {
                tooltip.add(probabilityText);
            }
            return Tooltip.create(tooltip);
        });
    }

    @Nullable
    public static Text getProbabilityTooltip(float probability) {
        if (probability == 1) {
            return null;
        } else {
            TranslatableText text;
            if (probability == 0) {
                text = new TranslatableText("text.modern_industrialization.probability_zero");
            } else {
                text = new TranslatableText("text.modern_industrialization.probability", PROBABILITY_FORMAT.format(probability * 100));
            }

            text.setStyle(TextHelper.YELLOW);
            return text;
        }
    }

    public static Function<EntryStack<?>, List<Text>> getProbabilitySetting(float probability) {
        @Nullable
        Text tooltip = getProbabilityTooltip(probability);
        return es -> tooltip == null ? List.of() : List.of(tooltip);
    }

    public static EntryStack<?> createFluidEntryStack(Fluid fluid) {
        return EntryStacks.of(fluid, 81000).setting(EntryStack.Settings.TOOLTIP_PROCESSOR, (stack, oldTooltip) -> {
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(FluidVariantRendering.getName(FluidVariant.of(fluid)));
            return Tooltip.create(tooltip);
        });
    }
}

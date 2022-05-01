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
import aztech.modern_industrialization.util.FluidHelper;
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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

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
        Component probabilityText = getProbabilityTooltip(probability);
        return EntryStacks.of(fluid, amount).setting(EntryStack.Settings.TOOLTIP_PROCESSOR, (stack, oldTooltip) -> {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(FluidVariantAttributes.getName(FluidVariant.of(fluid)));
            tooltip.add(FluidHelper.getFluidAmount(amount));
            if (probabilityText != null) {
                tooltip.add(probabilityText);
            }
            return Tooltip.create(tooltip);
        });
    }

    @Nullable
    public static Component getProbabilityTooltip(float probability) {
        if (probability == 1) {
            return null;
        } else {
            TranslatableComponent text;
            if (probability == 0) {
                text = new TranslatableComponent("text.modern_industrialization.probability_zero");
            } else {
                text = new TranslatableComponent("text.modern_industrialization.probability", PROBABILITY_FORMAT.format(probability * 100));
            }

            text.setStyle(TextHelper.YELLOW);
            return text;
        }
    }

    public static Function<EntryStack<?>, List<Component>> getProbabilitySetting(float probability) {
        @Nullable
        Component tooltip = getProbabilityTooltip(probability);
        return es -> tooltip == null ? List.of() : List.of(tooltip);
    }

    public static EntryStack<?> createFluidEntryStack(Fluid fluid) {
        return EntryStacks.of(fluid, 81000).setting(EntryStack.Settings.TOOLTIP_PROCESSOR, (stack, oldTooltip) -> {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(FluidVariantAttributes.getName(FluidVariant.of(fluid)));
            return Tooltip.create(tooltip);
        });
    }
}

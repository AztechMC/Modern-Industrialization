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
package aztech.modern_industrialization.compat.rei.machines;

import aztech.modern_industrialization.compat.rei.ReiUtil;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.util.FluidTextHelper;
import aztech.modern_industrialization.util.TextHelper;
import dev.architectury.fluid.FluidStack;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidKeyRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class MachineRecipeDisplay implements Display {
    final MachineRecipe recipe;
    private final Identifier category;
    private static final DecimalFormat PROBABILITY_FORMAT = new DecimalFormat("#.#");
    private static final BiFunction<EntryStack<?>, Tooltip, Tooltip> FLUID_TOOLTIP = (stack, tooltip) -> {
        FluidStack fs = stack.castValue();
        long amount = stack.<FluidStack>cast().getValue().getAmount();
        return Tooltip.create(FluidKeyRendering.getHandlerOrDefault(fs.getFluid()).getName(FluidKey.of(fs.getFluid())),
                new TranslatableText("text.modern_industrialization.fluid_slot_quantity", FluidTextHelper.getUnicodeMillibuckets(amount, false)));
    };

    public MachineRecipeDisplay(Identifier categoryId, MachineRecipe recipe) {
        this.recipe = recipe;
        this.category = categoryId;
    }

    private static Function<EntryStack<?>, List<Text>> getProbabilityTooltip(float probability) {
        return stack -> {
            if (probability == 1) {
                return Collections.emptyList();
            } else {
                TranslatableText text;
                if (probability == 0) {
                    text = new TranslatableText("text.modern_industrialization.probability_zero");
                } else {
                    text = new TranslatableText("text.modern_industrialization.probability", PROBABILITY_FORMAT.format(probability * 100));
                }
                text.setStyle(TextHelper.GRAY_TEXT);
                return Collections.singletonList(text);
            }
        };
    }

    private static EntryStack<?> createFluidEntryStack(Fluid fluid, long amount) {
        return EntryStacks.of(fluid, amount).setting(EntryStack.Settings.TOOLTIP_PROCESSOR, FLUID_TOOLTIP);
    }

    public Stream<EntryIngredient> getItemInputs() {
        return recipe.itemInputs.stream().map(i -> ReiUtil.createInputEntries(i)
                .map(e -> e.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    public Stream<EntryIngredient> getFluidInputs() {
        return recipe.fluidInputs.stream().map(i -> EntryIngredient.of(
                createFluidEntryStack(i.fluid, i.amount).setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return Stream.concat(getItemInputs(), getFluidInputs()).collect(Collectors.toList());
    }

    public Stream<EntryIngredient> getItemOutputs() {
        return recipe.itemOutputs.stream().map(i -> EntryIngredient.of(EntryStacks.of(new ItemStack(i.item, i.amount))
                .setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    public Stream<EntryIngredient> getFluidOutputs() {
        return recipe.fluidOutputs.stream().map(i -> EntryIngredient.of(
                createFluidEntryStack(i.fluid, i.amount).setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Stream.concat(getItemOutputs(), getFluidOutputs()).collect(Collectors.toList());
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return CategoryIdentifier.of(category);
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.of(recipe.getId());
    }

    public double getSeconds() {
        return recipe.duration / 20.0;
    }

    public int getEu() {
        return recipe.eu;
    }

    public int getTicks() {
        return recipe.duration;
    }
}

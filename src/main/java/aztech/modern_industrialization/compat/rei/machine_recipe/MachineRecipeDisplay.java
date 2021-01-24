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
package aztech.modern_industrialization.compat.rei.machine_recipe;

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.FluidTextHelper;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.fractions.Fraction;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MachineRecipeDisplay implements RecipeDisplay {
    final MachineRecipe recipe;
    private final Identifier category;
    private static final DecimalFormat PROBABILITY_FORMAT = new DecimalFormat("#.#");
    private static final Function<EntryStack, String> FLUID_TOOLTIP = stack -> {
        long amount = stack.getAccurateAmount().multiply(Fraction.ofWhole(81000)).longValue();
        return I18n.translate("text.modern_industrialization.fluid_slot_quantity", FluidTextHelper.getUnicodeMillibuckets(amount, false));
    };

    public MachineRecipeDisplay(MachineRecipeType type, MachineRecipe recipe) {
        this.recipe = recipe;
        this.category = type.getId();
    }

    private static Function<EntryStack, List<Text>> getProbabilityTooltip(float probability) {
        return (stack) -> probability == 1 ? Collections.emptyList()
                : Collections.singletonList(
                        new TranslatableText("text.modern_industrialization.probability", PROBABILITY_FORMAT.format(probability * 100)));
    }

    private static EntryStack createFluidEntryStack(Fluid fluid, long amount) {
        return EntryStack.create(fluid, Fraction.of(amount, 81000)).addSetting(EntryStack.Settings.Fluid.AMOUNT_TOOLTIP, FLUID_TOOLTIP);
    }

    public Stream<List<EntryStack>> getItemInputs() {
        return recipe.itemInputs.stream().map(i -> createInputEntries(i).stream()
                .map(e -> e.addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))).collect(Collectors.toList()));
    }

    private static List<EntryStack> createInputEntries(MachineRecipe.ItemInput input) {
        return input.getInputItems().stream().map(i -> EntryStack.create(new ItemStack(i, input.amount))).collect(Collectors.toList());
    }

    public Stream<List<EntryStack>> getFluidInputs() {
        return recipe.fluidInputs.stream().map(i -> Collections.singletonList(
                createFluidEntryStack(i.fluid, i.amount).addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return Stream.concat(getItemInputs(), getFluidInputs()).collect(Collectors.toList());
    }

    public Stream<List<EntryStack>> getItemOutputs() {
        return recipe.itemOutputs.stream().map(i -> Collections.singletonList(EntryStack.create(new ItemStack(i.item, i.amount))
                .addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    public Stream<List<EntryStack>> getFluidOutputs() {
        return recipe.fluidOutputs.stream().map(i -> Collections.singletonList(
                createFluidEntryStack(i.fluid, i.amount).addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Stream.concat(getItemOutputs(), getFluidOutputs()).collect(Collectors.toList());
    }

    @Override
    public @NotNull Identifier getRecipeCategory() {
        return category;
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

    @Override
    public @NotNull Optional<Identifier> getRecipeLocation() {
        return Optional.of(recipe.getId());
    }
}

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
import aztech.modern_industrialization.util.FluidHelper;
import dev.architectury.fluid.FluidStack;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.resources.ResourceLocation;

public class MachineRecipeDisplay implements Display {
    final MachineRecipe recipe;
    private final ResourceLocation category;

    private static final BiFunction<EntryStack<?>, Tooltip, Tooltip> FLUID_TOOLTIP = (stack, tooltip) -> {
        FluidStack fs = stack.castValue();
        long amount = stack.<FluidStack>cast().getValue().getAmount();
        return Tooltip.create(FluidVariantAttributes.getName(FluidVariant.of(fs.getFluid())), FluidHelper.getFluidAmount(amount));
    };

    public MachineRecipeDisplay(ResourceLocation categoryId, MachineRecipe recipe) {
        this.recipe = recipe;
        this.category = categoryId;
    }

    public Stream<EntryIngredient> getItemInputs() {
        return recipe.itemInputs.stream().map(i -> ReiUtil.createInputEntries(i)
                .map(e -> e.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, ReiUtil.getProbabilitySetting(i.probability))));
    }

    public Stream<EntryIngredient> getFluidInputs() {
        return recipe.fluidInputs.stream().map(i -> EntryIngredient.of(ReiUtil.createFluidEntryStack(i.fluid, i.amount, i.probability)));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return Stream.concat(getItemInputs(), getFluidInputs()).collect(Collectors.toList());
    }

    public Stream<EntryIngredient> getItemOutputs() {
        return recipe.itemOutputs.stream().map(i -> EntryIngredient.of(ReiUtil.createItemEntryStack(i.item, i.amount, i.probability)));
    }

    public Stream<EntryIngredient> getFluidOutputs() {
        return recipe.fluidOutputs.stream().map(i -> EntryIngredient.of(ReiUtil.createFluidEntryStack(i.fluid, i.amount, i.probability)));
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
    public Optional<ResourceLocation> getDisplayLocation() {
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

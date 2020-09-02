package aztech.modern_industrialization.rei.machine_recipe;

import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.fractions.Fraction;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MachineRecipeDisplay implements RecipeDisplay {
    private final MachineRecipe recipe;
    private final Identifier category;
    private static final DecimalFormat PROBABILITY_FORMAT = new DecimalFormat("#.#");

    public MachineRecipeDisplay(MachineRecipeType type, MachineRecipe recipe) {
        this.recipe = recipe;
        this.category = type.getId();
    }

    private static Function<EntryStack, List<Text>> getProbabilityTooltip(float probability) {
        return (stack) -> probability == 1 ? Collections.emptyList() : Collections.singletonList(new TranslatableText("text.modern_industrialization.probability", PROBABILITY_FORMAT.format(probability*100)));
    }

    public Stream<List<EntryStack>> getItemInputs() {
        return recipe.itemInputs.stream().map(i -> Collections.singletonList(EntryStack.create(new ItemStack(i.item, i.amount)).addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    public Stream<List<EntryStack>> getFluidInputs() {
        return recipe.fluidInputs.stream().map(i -> Collections.singletonList(EntryStack.create(i.fluid, Fraction.of(i.amount, FluidUnit.DROPS_PER_BUCKET)).addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return Stream.concat(
                getItemInputs(),
                getFluidInputs()
        ).collect(Collectors.toList());
    }

    public Stream<List<EntryStack>> getItemOutputs() {
        return recipe.itemOutputs.stream().map(i -> Collections.singletonList(EntryStack.create(new ItemStack(i.item, i.amount)).addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    public Stream<List<EntryStack>> getFluidOutputs() {
        return recipe.fluidOutputs.stream().map(i -> Collections.singletonList(EntryStack.create(i.fluid, Fraction.of(i.amount, FluidUnit.DROPS_PER_BUCKET)).addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
    }

    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Stream.concat(
                getItemOutputs(),
                getFluidOutputs()
        ).collect(Collectors.toList());
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
}

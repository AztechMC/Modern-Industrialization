package aztech.modern_industrialization.compat.rei.machine_recipe;

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
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

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MachineRecipeDisplay implements RecipeDisplay {
    final MachineRecipe recipe;
    private final Identifier category;
    private static final DecimalFormat PROBABILITY_FORMAT = new DecimalFormat("#.#");
    private static Function<EntryStack, String> FLUID_TOOLTIP = stack -> I18n.translate("text.modern_industrialization.fluid_slot_quantity", stack.getAccurateAmount().multiply(Fraction.ofWhole(1000)).intValue());

    public MachineRecipeDisplay(MachineRecipeType type, MachineRecipe recipe) {
        this.recipe = recipe;
        this.category = type.getId();
    }

    private static Function<EntryStack, List<Text>> getProbabilityTooltip(float probability) {
        return (stack) -> probability == 1 ? Collections.emptyList() : Collections.singletonList(new TranslatableText("text.modern_industrialization.probability", PROBABILITY_FORMAT.format(probability * 100)));
    }

    private static EntryStack createFluidEntryStack(Fluid fluid, int amount) {
        return EntryStack.create(fluid, Fraction.of(amount, 1000)).addSetting(EntryStack.Settings.Fluid.AMOUNT_TOOLTIP, FLUID_TOOLTIP);
    }

    public Stream<List<EntryStack>> getItemInputs() {
        return recipe.itemInputs.stream().map(i -> createInputEntries(i).stream().map(e -> e.addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))).collect(Collectors.toList()));
    }

    private static List<EntryStack> createInputEntries(MachineRecipe.ItemInput input) {
        return input.item == null
                ? input.tag.values().stream().map(i -> EntryStack.create(new ItemStack(i, input.amount))).collect(Collectors.toList())
                : Collections.singletonList(EntryStack.create(new ItemStack(input.item, input.amount)));
    }

    public Stream<List<EntryStack>> getFluidInputs() {
        return recipe.fluidInputs.stream().map(i -> Collections.singletonList(createFluidEntryStack(i.fluid, i.amount).addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
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
        return recipe.fluidOutputs.stream().map(i -> Collections.singletonList(createFluidEntryStack(i.fluid, i.amount).addSetting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, getProbabilityTooltip(i.probability))));
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

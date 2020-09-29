package aztech.modern_industrialization.compat.rei.fluid_fuels;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FluidFuelsCategory implements RecipeCategory<FluidFuelDisplay> {
    @Override
    public @NotNull Identifier getIdentifier() {
        return FluidFuelsPlugin.CATEGORY;
    }

    @Override
    public @NotNull String getCategoryName() {
        return I18n.translate(FluidFuelsPlugin.CATEGORY.toString());
    }

    @Override
    public @NotNull EntryStack getLogo() {
        return EntryStack.create(MIFluids.DIESEL.bucketItem);
    }

    @Override
    public @NotNull List<Widget> setupDisplay(FluidFuelDisplay recipeDisplay, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createSlot(new Point(bounds.x+66, bounds.y+10)).entry(EntryStack.create(recipeDisplay.fluid.getRawFluid())));
        int totalEnergy = FluidFuelRegistry.getBurnTicks(recipeDisplay.fluid) * 32;
        widgets.add(Widgets.createLabel(new Point(bounds.x+10, bounds.y+35), new TranslatableText("text.modern_industrialization.eu_in_diesel_generator", totalEnergy)).leftAligned());
        return widgets;
    }
}

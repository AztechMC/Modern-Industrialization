package aztech.modern_industrialization.compat.rei.fluid_fuels;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import java.util.Collections;
import java.util.List;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

class FluidFuelDisplay implements RecipeDisplay {
    final FluidKey fluid;

    FluidFuelDisplay(FluidKey fluid) {
        this.fluid = fluid;
    }

    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return Collections.singletonList(Collections.singletonList(EntryStack.create(fluid.getRawFluid())));
    }

    @Override
    public @NotNull Identifier getRecipeCategory() {
        return FluidFuelsPlugin.CATEGORY;
    }
}

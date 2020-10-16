package aztech.modern_industrialization.compat.rei.fluid_fuels;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.machines.MIMachines;
import java.util.Arrays;
import java.util.List;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class FluidFuelsPlugin implements REIPluginV0 {
    static final Identifier CATEGORY = new MIIdentifier("fluid_fuels");

    @Override
    public Identifier getPluginIdentifier() {
        return new MIIdentifier("fluid_fuels");
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategory(new FluidFuelsCategory());
    }

    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        for (FluidKey fluid : FluidFuelRegistry.getRegisteredFluids()) {
            recipeHelper.registerDisplay(new FluidFuelDisplay(fluid));
        }
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        List<Item> workstations = Arrays.asList(MIMachines.DIESEL_GENERATOR.item, MIMachines.LARGE_STEAM_BOILER.item,
                ModernIndustrialization.ITEM_JETPACK);
        for (Item item : workstations) {
            recipeHelper.registerWorkingStations(CATEGORY, EntryStack.create(item));
        }

        recipeHelper.removeAutoCraftButton(CATEGORY);
    }
}

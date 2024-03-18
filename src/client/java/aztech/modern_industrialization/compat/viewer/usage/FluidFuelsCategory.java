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
package aztech.modern_industrialization.compat.viewer.usage;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.datamaps.FluidFuel;
import aztech.modern_industrialization.api.datamaps.MIDataMaps;
import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;

public class FluidFuelsCategory extends ViewerCategory<Fluid> {
    public FluidFuelsCategory() {
        super(Fluid.class, new MIIdentifier("fluid_fuels"), MIText.FluidFuels.text(), MIFluids.DIESEL.getBucket().getDefaultInstance(), 150, 35);
    }

    @Override
    public void buildWorkstations(WorkstationConsumer consumer) {
        consumer.accept("lv_diesel_generator", "mv_diesel_generator", "hv_diesel_generator", "large_diesel_generator", "large_steam_boiler",
                "advanced_large_steam_boiler",
                "high_pressure_large_steam_boiler",
                "high_pressure_advanced_large_steam_boiler");
        consumer.accept(MIItem.DIESEL_JETPACK, MIItem.DIESEL_CHAINSAW, MIItem.DIESEL_MINING_DRILL);
    }

    @Override
    public void buildRecipes(RecipeManager recipeManager, RegistryAccess registryAccess, Consumer<Fluid> consumer) {
        for (var fluid : registryAccess.registryOrThrow(Registries.FLUID)) {
            if (fluid.builtInRegistryHolder().getData(MIDataMaps.FLUID_FUELS) != null) {
                consumer.accept(fluid);
            }
        }
    }

    @Override
    public void buildLayout(Fluid recipe, LayoutBuilder builder) {
        builder.inputSlot(15, 10).variant(FluidVariant.of(recipe));
    }

    @Override
    public void buildWidgets(Fluid recipe, WidgetList widgets) {
        int totalEnergy = FluidFuel.getEu(recipe);
        Component text = MIText.EuInDieselGenerator.text(totalEnergy);
        widgets.secondaryText(text, 40, 14);
    }
}

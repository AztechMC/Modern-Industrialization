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
package aztech.modern_industrialization.compat.rei.fluid_fuels;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

public class FluidFuelsCategory implements DisplayCategory<FluidFuelDisplay> {
    @Override
    public CategoryIdentifier<? extends FluidFuelDisplay> getCategoryIdentifier() {
        return FluidFuelsPlugin.CATEGORY;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(MIFluids.DIESEL.getBucket());
    }

    @Override
    public Component getTitle() {
        return MIText.FluidFuels.text();
    }

    @Override
    public List<Widget> setupDisplay(FluidFuelDisplay recipeDisplay, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createSlot(new Point(bounds.x + 15, bounds.y + 10)).entry(EntryStacks.of(recipeDisplay.fluid)));
        int totalEnergy = FluidFuelRegistry.getEu(recipeDisplay.fluid);

        Component text = MIText.EuInDieselGenerator.text(totalEnergy);

        widgets.add(Widgets
                .createLabel(new Point(bounds.x + 50, bounds.y + 14),
                        text)
                .leftAligned().noShadow().color(0xFF404040, 0xFFBBBBBB));
        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 35;
    }
}

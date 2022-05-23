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
package aztech.modern_industrialization.compat.rei.forgehammer_recipe;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ForgeHammerRecipeCategory implements DisplayCategory<ForgeHammerRecipeDisplay> {

    private static final ResourceLocation id = new MIIdentifier("forge_hammer");

    @Override
    public CategoryIdentifier<? extends ForgeHammerRecipeDisplay> getCategoryIdentifier() {
        return CategoryIdentifier.of(id);
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(MIBlock.FORGE_HAMMER.asItem());
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent(MIBlock.FORGE_HAMMER.getTranslationKey());
    }

    @Override
    public @NotNull List<Widget> setupDisplay(ForgeHammerRecipeDisplay recipeDisplay, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 25, bounds.getCenterY() - 18);
        List<Widget> widgets = new ArrayList<>();

        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 27, startPoint.y + 4)));

        widgets.add(Widgets.createTexturedWidget(ForgeHammerScreen.FORGE_HAMMER_GUI,
                startPoint.x - 25, startPoint.y + 4, 7, 32, 18, 18));

        Component text;

        if (recipeDisplay.recipe.eu > 0) {
            widgets.add(Widgets.createSlot(new Point(startPoint.x - 24, startPoint.y + 5)).disableBackground()
                    .entries(recipeDisplay.getInputEntries().get(1)).markInput());
            widgets.add(
                    Widgets.createSlot(new Point(startPoint.x + 4, startPoint.y + 5)).entries(recipeDisplay.getInputEntries().get(0)).markInput());
            text = MIText.DurabilityCost.text(recipeDisplay.recipe.eu);
        } else {
            widgets.add(
                    Widgets.createSlot(new Point(startPoint.x + 4, startPoint.y + 5)).entries(recipeDisplay.getInputEntries().get(0)).markInput());
            text = MIText.NoToolRequired.text();
        }

        widgets.add(Widgets
                .createLabel(new Point(startPoint.x - 24, bounds.y + 28), text)
                .leftAligned().noShadow().color(0xFF404040, 0xFFBBBBBB));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 5)).entries(recipeDisplay.getOutputEntries().get(0)).markOutput());
        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 40;
    }
}

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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MultiblockRecipeCategory implements DisplayCategory<MultiblockRecipeDisplay> {
    public static final ResourceLocation ID = new MIIdentifier("multiblock_shapes");
    private static final int SLOTS = 6;
    private static final int MARGIN = 10;
    private static final int H = 18 + 2 * MARGIN;
    private static final int W = SLOTS * 20 - 2 + 2 * MARGIN;

    @Override
    public CategoryIdentifier<? extends MultiblockRecipeDisplay> getCategoryIdentifier() {
        return CategoryIdentifier.of(ID);
    }

    @Override
    public Component getTitle() {
        return MIText.MultiblockMaterials.text();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(MIItem.ITEM_WRENCH);
    }

    @Override
    public List<Widget> setupDisplay(MultiblockRecipeDisplay recipeDisplay, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        for (int i = 0; i < SLOTS; ++i) {
            EntryIngredient slotStack = i < recipeDisplay.shape.materials.size() ? recipeDisplay.shape.materials.get(i) : EntryIngredient.empty();

            Slot slot = Widgets.createSlot(new Point(bounds.x + MARGIN + i * 20, bounds.y + MARGIN));
            slot.entries(slotStack);
            widgets.add(slot);
        }
        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return H;
    }

    @Override
    public int getDisplayWidth(MultiblockRecipeDisplay display) {
        return W;
    }
}

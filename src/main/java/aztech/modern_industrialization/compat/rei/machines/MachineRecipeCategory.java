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
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machinesv2.MachineScreenHandlers;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public class MachineRecipeCategory implements RecipeCategory<MachineRecipeDisplay> {
    private final Identifier id;
    private final MachineCategoryParams params;

    public MachineRecipeCategory(Identifier id, MachineCategoryParams params) {
        this.id = id;
        this.params = params;
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return id;
    }

    @Override
    public @NotNull String getCategoryName() {
        return I18n.translate("rei_categories.modern_industrialization." + id.getPath());
    }

    @Override
    public @NotNull EntryStack getLogo() {
        return EntryStack.create(Registry.ITEM.get(new MIIdentifier(params.workstations.get(0))));
    }

    @FunctionalInterface
    private interface SlotDrawer {
        void drawSlots(Stream<List<EntryStack>> entries, SlotPositions positions, boolean input, boolean fluid);
    }

    @Override
    public @NotNull List<Widget> setupDisplay(MachineRecipeDisplay recipeDisplay, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        // Compute offset relative to the machine coordinates
        int x = 1000, X = 0, y = 1000, Y = 0;
        for (SlotPositions positions : new SlotPositions[] { params.itemInputs, params.itemOutputs, params.fluidInputs, params.fluidOutputs }) {
            for (int i = 0; i < positions.size(); ++i) {
                x = Math.min(x, positions.getX(i));
                X = Math.max(X, positions.getX(i) + 16);
                y = Math.min(y, positions.getY(i));
                Y = Math.max(Y, positions.getY(i) + 16);
            }
        }
        int oldY = Y;
        Y += 42;
        int xoffset = bounds.x + (bounds.width - X + x) / 2 - x;
        int yoffset = bounds.y + (bounds.height - Y + y) / 2 - y;

        // Draw slots
        SlotDrawer drawer = (entryStream, positions, input, fluid) -> {
            List<List<EntryStack>> entries = entryStream.collect(Collectors.toList());
            for (int i = 0; i < positions.size(); ++i) {
                List<EntryStack> stack = i < entries.size() ? entries.get(i) : Collections.emptyList();
                Point point = new Point(xoffset + positions.getX(i), yoffset + positions.getY(i));
                Slot widget = Widgets.createSlot(point).entries(stack);
                if (input) {
                    widget.markInput();
                } else {
                    widget.markOutput();
                }
                if (fluid) {
                    widgets.add(createFluidSlotBackground(point));
                    widget.disableBackground();
                }
                widgets.add(widget);
            }
        };
        drawer.drawSlots(recipeDisplay.getItemInputs(), params.itemInputs, true, false);
        drawer.drawSlots(recipeDisplay.getItemOutputs(), params.itemOutputs, false, false);
        drawer.drawSlots(recipeDisplay.getFluidInputs(), params.fluidInputs, true, true);
        drawer.drawSlots(recipeDisplay.getFluidOutputs(), params.fluidOutputs, false, true);

        // Draw progress bar
        double recipeMillis = recipeDisplay.getSeconds() * 1000;
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            ProgressBar.RenderHelper.renderProgress(helper, matrices, xoffset, yoffset, params.progressBarParams,
                    (float) (System.currentTimeMillis() / recipeMillis % 1.0));
        }));

        // Draw labels
        widgets.add(Widgets.createLabel(new Point(bounds.x + 5, yoffset + oldY + 3),
                new TranslatableText("text.modern_industrialization.base_eu_t", recipeDisplay.getEu())).leftAligned());
        widgets.add(Widgets.createLabel(new Point(bounds.x + 5, yoffset + oldY + 16),
                new TranslatableText("text.modern_industrialization.base_duration_seconds", recipeDisplay.getSeconds())).leftAligned());
        widgets.add(Widgets
                .createLabel(new Point(bounds.x + 5, yoffset + oldY + 29),
                        new TranslatableText("text.modern_industrialization.base_eu_total", recipeDisplay.getTicks() * recipeDisplay.getEu()))
                .leftAligned());

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        int y = 1000, Y = 0;
        for (SlotPositions positions : new SlotPositions[] { params.itemInputs, params.itemOutputs, params.fluidInputs, params.fluidOutputs }) {
            for (int i = 0; i < positions.size(); ++i) {
                y = Math.min(y, positions.getY(i));
                Y = Math.max(Y, positions.getY(i) + 16);
            }
        }

        // Room for EU text below
        Y += 42;
        return Y - y + 15;
    }

    @Override
    public int getDisplayWidth(MachineRecipeDisplay display) {
        int x = 1000, X = 0;
        for (SlotPositions positions : new SlotPositions[] { params.itemInputs, params.itemOutputs, params.fluidInputs, params.fluidOutputs }) {
            for (int i = 0; i < positions.size(); ++i) {
                x = Math.min(x, positions.getX(i));
                X = Math.max(X, positions.getX(i) + 16);
            }
        }

        return Math.max(X - x + 15, 150);
    }

    private static Widget createFluidSlotBackground(Point point) {
        return Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            MinecraftClient.getInstance().getTextureManager().bindTexture(MachineScreenHandlers.SLOT_ATLAS);
            helper.drawTexture(matrices, point.x - 1, point.y - 1, 18, 0, 18, 18);
        });
    }
}

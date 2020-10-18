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
package aztech.modern_industrialization.compat.rei.machine_recipe;

import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineScreen;
import aztech.modern_industrialization.machines.impl.SteamMachineFactory;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
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
import org.jetbrains.annotations.NotNull;

public class MachineRecipeCategory implements RecipeCategory<MachineRecipeDisplay> {
    private final Identifier id;
    private final MachineFactory factory;
    private final EntryStack logo;

    public MachineRecipeCategory(MachineRecipeType type, MachineFactory factory, EntryStack logo) {
        this.id = type.getId();
        this.factory = factory;
        this.logo = logo;
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return id;
    }

    @Override
    public @NotNull String getCategoryName() {
        return I18n.translate(id.toString());
    }

    @Override
    public @NotNull EntryStack getLogo() {
        return logo;
    }

    @FunctionalInterface
    private interface SlotDrawer {
        void drawSlots(Stream<List<EntryStack>> entries, int[] slots, boolean input, boolean fluid);
    }

    @Override
    public @NotNull List<Widget> setupDisplay(MachineRecipeDisplay recipeDisplay, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        int x = 1000, y = 1000, X = 0, Y = 0;
        for (int i = 0; i < factory.getSlots(); i++) {
            if (i == factory.getInputSlots() && factory instanceof SteamMachineFactory) {
                continue;
            }
            x = Math.min(x, factory.getSlotPosX(i));
            X = Math.max(X, factory.getSlotPosX(i) + 16);
            y = Math.min(y, factory.getSlotPosY(i));
            Y = Math.max(Y, factory.getSlotPosY(i) + 16);
        }

        // Room for EU text below
        int oldY = Y;
        Y += 42;

        int xoffset = bounds.x + (bounds.width - X + x) / 2 - x;
        int yoffset = bounds.y + (bounds.height - Y + y) / 2 - y;

        SlotDrawer drawer = (entryStream, slots, input, fluid) -> {
            List<List<EntryStack>> entries = entryStream.collect(Collectors.toList());
            for (int i = 0; i < slots.length; ++i) {
                List<EntryStack> stack = i < entries.size() ? entries.get(i) : Collections.emptyList();
                Point point = new Point(xoffset + factory.getSlotPosX(slots[i]), yoffset + factory.getSlotPosY(slots[i]));
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

        drawer.drawSlots(recipeDisplay.getItemInputs(), factory.getInputIndices(), true, false);
        drawer.drawSlots(recipeDisplay.getFluidInputs(), factory.getFluidInputIndices(), true, true);
        drawer.drawSlots(recipeDisplay.getItemOutputs(), factory.getOutputIndices(), false, false);
        drawer.drawSlots(recipeDisplay.getFluidOutputs(), factory.getFluidOutputIndices(), false, true);

        if (factory.hasProgressBar()) {
            double recipeMillis = recipeDisplay.getSeconds() * 1000;
            widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                MinecraftClient.getInstance().getTextureManager().bindTexture(factory.getBackgroundIdentifier());
                double progress = (System.currentTimeMillis() / recipeMillis) % 1.0;
                int sx = factory.getProgressBarSizeX();
                int sy = factory.getProgressBarSizeY();

                int px = xoffset + factory.getProgressBarDrawX();
                int py = yoffset + factory.getProgressBarDrawY();

                int u = factory.getProgressBarX();
                int v = factory.getProgressBarY();

                // Base arrow
                helper.drawTexture(matrices, px, py, factory.getProgressBarDrawX(), factory.getProgressBarDrawY(), sx, sy);
                // Overlay
                if (factory.isProgressBarHorizontal()) {
                    int progressPixel = (int) (progress * sx);
                    helper.drawTexture(matrices, px, py, u, v, progressPixel, sy);
                } else if (factory.isProgressBarFlipped()) {
                    int progressPixel = (int) ((1 - progress) * sy);
                    helper.drawTexture(matrices, px, py + progressPixel, u, v + progressPixel, sx, sy - progressPixel);
                } else {
                    int progressPixel = (int) (progress * sy);
                    helper.drawTexture(matrices, px, py, u, v, sx, progressPixel);
                }
            }));
        }

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
        for (int i = 0; i < factory.getSlots(); i++) {
            if (i == factory.getInputSlots() && factory instanceof SteamMachineFactory) {
                continue;
            }
            y = Math.min(y, factory.getSlotPosY(i));
            Y = Math.max(Y, factory.getSlotPosY(i) + 16);
        }

        // Room for EU text below
        Y += 42;
        return Y - y + 15;
    }

    @Override
    public int getDisplayWidth(MachineRecipeDisplay display) {
        int x = 1000, X = 0;
        for (int i = 0; i < factory.getSlots(); i++) {
            if (i == factory.getInputSlots() && factory instanceof SteamMachineFactory) {
                continue;
            }
            x = Math.min(x, factory.getSlotPosX(i));
            X = Math.max(X, factory.getSlotPosX(i) + 16);
        }

        return Math.max(X - x + 15, 150);
    }

    private static Widget createFluidSlotBackground(Point point) {
        return Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            MinecraftClient.getInstance().getTextureManager().bindTexture(MachineScreen.SLOT_ATLAS);
            helper.drawTexture(matrices, point.x - 1, point.y - 1, 18, 0, 18, 18);
        });
    }
}

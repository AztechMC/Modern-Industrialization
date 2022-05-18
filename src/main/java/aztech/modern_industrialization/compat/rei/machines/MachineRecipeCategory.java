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
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.components.sync.EnergyBar;
import aztech.modern_industrialization.machines.components.sync.ProgressBar;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class MachineRecipeCategory implements DisplayCategory<MachineRecipeDisplay> {
    private final ResourceLocation id;
    private final MachineCategoryParams params;

    public MachineRecipeCategory(ResourceLocation id, MachineCategoryParams params) {
        this.id = id;
        this.params = params;
    }

    @Override
    public CategoryIdentifier<? extends MachineRecipeDisplay> getCategoryIdentifier() {
        return CategoryIdentifier.of(id);
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("rei_categories.modern_industrialization." + id.getPath());
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Registry.ITEM.get(new MIIdentifier(params.workstations.get(0))));
    }

    @FunctionalInterface
    private interface SlotDrawer {
        void drawSlots(Stream<EntryIngredient> entries, SlotPositions positions, boolean input, boolean fluid);
    }

    @Override
    public List<Widget> setupDisplay(MachineRecipeDisplay recipeDisplay, Rectangle bounds) {
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
        int xoffset = bounds.x + (bounds.width - X + x) / 2 - x;
        int yoffset = bounds.y + 17 - y;

        // Draw slots
        SlotDrawer drawer = (entryStream, positions, input, fluid) -> {
            List<EntryIngredient> entries = entryStream.collect(Collectors.toList());
            for (int i = 0; i < positions.size(); ++i) {
                EntryIngredient ingredient = i < entries.size() ? entries.get(i) : EntryIngredient.empty();
                Point point = new Point(xoffset + positions.getX(i), yoffset + positions.getY(i));
                Slot widget = Widgets.createSlot(point).entries(ingredient);
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

        Component totalEuTooltip = MIText.BaseEuTotal.text(TextHelper.getEuText((long) recipeDisplay.getTicks() * recipeDisplay.getEu()));

        // Draw filled energy bar
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            matrices.pushPose();
            matrices.translate(bounds.x + 5, bounds.y + 5, 0);
            matrices.scale(0.5f, 0.5f, 0.5f);
            EnergyBar.Client.Renderer.renderEnergy(helper, matrices, 0, 0, 1);
            matrices.popPose();
        }));
        // Draw EU/t and seconds
        widgets.add(Widgets
                .createLabel(new Point(bounds.x + 15, bounds.y + 5),
                        TextHelper.getEuTextTick(recipeDisplay.getEu()))
                .leftAligned().noShadow().color(0xFF404040, 0xFFBBBBBB));
        widgets.add(Widgets
                .createLabel(new Point(bounds.getMaxX() - 5, bounds.y + 5),
                        MIText.BaseDurationSeconds.text(recipeDisplay.getSeconds()))
                .rightAligned().noShadow().color(0xFF404040, 0xFFBBBBBB));
        // Total EU tooltip
        Rectangle tooltipZone = new Rectangle(bounds.x + 2, bounds.y + 5, bounds.width - 10, 12);
        widgets.add(new Widget() {
            @Override
            public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
                if (tooltipZone.contains(mouseX, mouseY)) {
                    Tooltip.create(totalEuTooltip).queue();
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
        });

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

        // Room for text above
        return Y - y + 25;
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

        return Math.max(X - x + 15, 120);
    }

    private static Widget createFluidSlotBackground(Point point) {
        return Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
            helper.blit(matrices, point.x - 1, point.y - 1, 18, 0, 18, 18);
        });
    }
}

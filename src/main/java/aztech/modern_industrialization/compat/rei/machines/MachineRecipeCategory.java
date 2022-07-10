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

import static aztech.modern_industrialization.MITooltips.EU_PER_TICK_PARSER;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

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
        return Component.translatable("rei_categories.modern_industrialization." + id.getPath());
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

        // Draw filled energy bar
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            matrices.pushPose();

            matrices.translate(bounds.x + 5, bounds.y + 5, 0);
            matrices.scale(0.5f, 0.5f, 0.5f);
            switch (params.steamMode) {
            case BOTH -> {
                RenderSystem.setShaderTexture(0, MachineScreen.SLOT_ATLAS);
                helper.blit(matrices, -2, -2, 80, 18, 20, 20);
            }
            case STEAM_ONLY -> {
                RenderSystem.setShaderTexture(0, new MIIdentifier("textures/item/steam_bucket.png"));
                GuiComponent.blit(matrices, 0, 0, helper.getBlitOffset(), 0, 0, 16, 16, 16, 16);
            }
            case ELECTRIC_ONLY -> {
                EnergyBar.Client.Renderer.renderEnergy(helper, matrices, 0, 0, 1);
            }
            }

            matrices.popPose();
        }));
        widgets.add(Widgets
                .createLabel(new Point(bounds.x + 15 + (params.steamMode.steam ? 2 : 0), bounds.y + 5),
                        TextHelper.getEuTextTick(recipeDisplay.getEu()))
                .leftAligned().noShadow().color(0xFF404040, 0xFFBBBBBB));
        widgets.add(Widgets
                .createLabel(new Point(bounds.getMaxX() - 5, bounds.y + 5),
                        MIText.BaseDurationSeconds.text(recipeDisplay.getSeconds()))
                .rightAligned().noShadow().color(0xFF404040, 0xFFBBBBBB));
        // Draw steel hatch or upgrades
        boolean steelHatchRequired = params.steamMode.steam && params.isMultiblock && recipeDisplay.getEu() > MachineTier.BRONZE.getMaxEu();
        int upgradeEuRequired = recipeDisplay.getEu() - (params.isMultiblock ? MachineTier.MULTIBLOCK : MachineTier.LV).getMaxEu();
        // Ugly fusion reactor workaround
        if (upgradeEuRequired > 0 && id.getPath().equals("fusion_reactor")) {
            upgradeEuRequired = 0;
        }
        // Conditions
        boolean conditionsRequired = recipeDisplay.recipe.conditions.size() > 0;
        if (steelHatchRequired || upgradeEuRequired > 0 || conditionsRequired) {
            ItemLike displayedItem;
            if (steelHatchRequired) {
                displayedItem = Registry.ITEM.get(new MIIdentifier("steel_item_input_hatch"));
            } else if (conditionsRequired) {
                displayedItem = MIItem.WRENCH;
            } else {
                displayedItem = MIItem.BASIC_UPGRADE;
            }
            widgets.add(Widgets.createSlot(new Rectangle(bounds.getCenterX() - 3, bounds.y + 3.75, 10.8, 10.8))
                    .entry(EntryStacks.of(displayedItem))
                    .disableTooltips()
                    .disableHighlight()
                    .disableBackground());
        }
        // Tooltips
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(MIText.BaseEuTotal.text(TextHelper.getEuText((long) recipeDisplay.getTicks() * recipeDisplay.getEu())));
        if (params.steamMode.steam) {
            tooltips.add((params.steamMode.electric ? MIText.AcceptsSteamToo : MIText.AcceptsSteam).text().withStyle(ChatFormatting.GRAY));
            if (steelHatchRequired) {
                tooltips.add(MIText.RequiresSteelHatch0.text().setStyle(Style.EMPTY.withUnderlined(true)));
                tooltips.add(MIText.RequiresSteelHatch1.text().withStyle(ChatFormatting.GRAY));
            }
        }
        if (upgradeEuRequired > 0) {
            tooltips.add(new MITooltips.Line(MIText.RequiresUpgrades).arg(upgradeEuRequired, EU_PER_TICK_PARSER).build());
        }
        if (conditionsRequired) {
            for (var condition : recipeDisplay.recipe.conditions) {
                condition.appendDescription(tooltips);
            }
        }
        Rectangle tooltipZone = new Rectangle(bounds.x + 2, bounds.y + 5, bounds.width - 10, 11);
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            if (tooltipZone.contains(mouseX, mouseY)) {
                Tooltip.create(tooltips).queue();
            }
        }));

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
            RenderSystem.setShaderTexture(0, MachineScreen.SLOT_ATLAS);
            helper.blit(matrices, point.x - 1, point.y - 1, 18, 0, 18, 18);
        });
    }
}

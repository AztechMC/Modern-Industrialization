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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static aztech.modern_industrialization.MITooltips.EU_PER_TICK_PARSER;

public class MachineCategoryParams {
    public final String englishName;
    public final ResourceLocation category;
    public final SlotPositions itemInputs;
    public final SlotPositions itemOutputs;
    public final SlotPositions fluidInputs;
    public final SlotPositions fluidOutputs;
    public final ProgressBar.Params progressBarParams;
    public final MachineRecipeType recipeType;
    public final Predicate<MachineRecipe> recipePredicate;
    public final boolean isMultiblock;
    public final SteamMode steamMode;
    public final List<ResourceLocation> workstations = new ArrayList<>();

    public MachineCategoryParams(String englishName, ResourceLocation category, SlotPositions itemInputs, SlotPositions itemOutputs,
            SlotPositions fluidInputs, SlotPositions fluidOutputs, ProgressBar.Params progressBarParams, MachineRecipeType recipeType,
            Predicate<MachineRecipe> recipePredicate, boolean isMultiblock, SteamMode steamMode) {
        this.englishName = englishName;
        this.category = category;
        this.itemInputs = itemInputs;
        this.itemOutputs = itemOutputs;
        this.fluidInputs = fluidInputs;
        this.fluidOutputs = fluidOutputs;
        this.progressBarParams = progressBarParams;
        this.recipeType = recipeType;
        this.recipePredicate = recipePredicate;
        this.isMultiblock = isMultiblock;
        this.steamMode = steamMode;
    }

    public record Dimensions(int minX, int minY, int width, int height) {}

    public Dimensions calculateDimensions() {
        int x = 1000, X = 0;
        int y = 1000, Y = 0;
        for (SlotPositions positions : new SlotPositions[] { itemInputs, itemOutputs, fluidInputs, fluidOutputs }) {
            for (int i = 0; i < positions.size(); ++i) {
                x = Math.min(x, positions.getX(i));
                X = Math.max(X, positions.getX(i) + 16);
                y = Math.min(y, positions.getY(i));
                Y = Math.max(Y, positions.getY(i) + 16);
            }
        }

        int width = X - x;
        int height = Y - y;

        return new Dimensions(x, y, width, height);
    }

    private boolean steelHatchRequired(MachineRecipe recipe) {
        return steamMode.steam && isMultiblock && recipe.eu > MachineTier.BRONZE.getMaxEu();
    }

    private int upgradeEuRequired(MachineRecipe recipe) {
        int upgradeEuRequired = recipe.eu - (isMultiblock ? MachineTier.MULTIBLOCK : MachineTier.LV).getMaxEu();
        // Ugly fusion reactor workaround
        if (upgradeEuRequired > 0 && category.getPath().equals("fusion_reactor")) {
            upgradeEuRequired = 0;
        }
        return upgradeEuRequired;
    }

    public boolean includeRecipeConditionDisplay(MachineRecipe recipe) {
        return steelHatchRequired(recipe) ||
                upgradeEuRequired(recipe) > 0 ||
                !recipe.conditions.isEmpty();
    }

    public List<ItemStack> getRecipeConditionDisplayItems(MachineRecipe recipe) {
        List<ItemStack> items = new ArrayList<>();
        if (steelHatchRequired(recipe)) {
            items.add(BuiltInRegistries.ITEM.get(MI.id("steel_item_input_hatch")).getDefaultInstance());
        }
        if (upgradeEuRequired(recipe) > 0) {
            items.add(MIItem.BASIC_UPGRADE.stack());
        }
        for (var condition : recipe.conditions) {
            ItemStack displayedItem = condition.icon();
            if (!displayedItem.isEmpty()) {
                items.add(displayedItem);
            }
        }
        if (items.isEmpty()) {
            items.add(MIItem.WRENCH.stack());
        }
        return items;
    }

    public List<Component> getRecipeTooltip(MachineRecipe recipe) {
        List<Component> lines = new ArrayList<>();
        if (steamMode != SteamMode.NEITHER) {
            lines.add(MIText.BaseEuTotal.text(TextHelper.getEuText((long) recipe.duration * recipe.eu)));
        }
        if (steamMode.steam) {
            lines.add((steamMode.electric ? MIText.AcceptsSteamToo : MIText.AcceptsSteam).text().withStyle(ChatFormatting.GRAY));
            if (steelHatchRequired(recipe)) {
                lines.add(MIText.RequiresSteelHatch0.text().setStyle(Style.EMPTY.withUnderlined(true)));
                lines.add(MIText.RequiresSteelHatch1.text().withStyle(ChatFormatting.GRAY));
            }
        }
        int upgradeEuRequired = upgradeEuRequired(recipe);
        if (upgradeEuRequired > 0) {
            lines.add(new MITooltips.Line(MIText.RequiresUpgrades).arg(upgradeEuRequired, EU_PER_TICK_PARSER).build());
        }
        for (var condition : recipe.conditions) {
            condition.appendDescription(lines);
        }
        return lines;
    }
}

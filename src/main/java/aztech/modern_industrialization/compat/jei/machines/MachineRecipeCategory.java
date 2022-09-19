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
package aztech.modern_industrialization.compat.jei.machines;

import static aztech.modern_industrialization.MITooltips.EU_PER_TICK_PARSER;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.compat.jei.JeiUtil;
import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public class MachineRecipeCategory implements IRecipeCategory<MachineRecipe> {
    private final RecipeType<MachineRecipe> type;
    private final MachineCategoryParams params;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable itemSlotBackground;
    private final IDrawable fluidSlotBackground;

    private final IDrawable steelHatch, wrench, upgrade;

    public MachineRecipeCategory(IJeiHelpers jeiHelpers,
            RecipeType<MachineRecipe> type,
            MachineCategoryParams params) {

        var guiHelper = jeiHelpers.getGuiHelper();
        this.type = type;
        this.params = params;
        this.background = guiHelper.createBlankDrawable(getDisplayWidth(), getDisplayHeight());
        this.icon = guiHelper.createDrawableItemStack(Registry.ITEM.get(new MIIdentifier(params.workstations.get(0))).getDefaultInstance());
        this.itemSlotBackground = guiHelper.getSlotDrawable();
        this.fluidSlotBackground = guiHelper.createDrawable(MachineScreen.SLOT_ATLAS, 18, 0, 18, 18);

        this.steelHatch = guiHelper.createDrawableItemStack(new ItemStack(Registry.ITEM.get(new MIIdentifier("steel_item_input_hatch"))));
        this.wrench = guiHelper.createDrawableItemStack(MIItem.WRENCH.stack());
        this.upgrade = guiHelper.createDrawableItemStack(MIItem.BASIC_UPGRADE.stack());
    }

    @Override
    public RecipeType<MachineRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("rei_categories.modern_industrialization." + type.getUid().getPath());
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {
        addItemInputs(builder, recipe);
        addItemOutputs(builder, recipe);
        addFluidInputs(builder, recipe);
        addFluidOutputs(builder, recipe);
    }

    private void addItemInputs(IRecipeLayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.itemInputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .setBackground(itemSlotBackground, -1, -1);

            if (i < recipe.itemInputs.size()) {
                var input = recipe.itemInputs.get(i);
                slot.addItemStacks(JeiUtil.getItemStacks(input));
                JeiUtil.customizeTooltip(slot, input.probability);
            }
        }
    }

    private void addItemOutputs(IRecipeLayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.itemOutputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(itemSlotBackground, -1, -1);

            if (i < recipe.itemOutputs.size()) {
                var output = recipe.itemOutputs.get(i);
                slot.addItemStack(JeiUtil.getItemStack(output));
                JeiUtil.customizeTooltip(slot, output.probability);
            }
        }
    }

    private void addFluidInputs(IRecipeLayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.fluidInputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .setBackground(fluidSlotBackground, -1, -1);

            if (i < recipe.fluidInputs.size()) {
                var input = recipe.fluidInputs.get(i);
                slot.addFluidStack(input.fluid, input.amount);
                JeiUtil.overrideFluidRenderer(slot);
                JeiUtil.customizeTooltip(slot, input.probability);
            }
        }
    }

    private void addFluidOutputs(IRecipeLayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.fluidOutputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(fluidSlotBackground, -1, -1);

            if (i < recipe.fluidOutputs.size()) {
                var output = recipe.fluidOutputs.get(i);
                slot.addFluidStack(output.fluid, output.amount);
                JeiUtil.overrideFluidRenderer(slot);
                JeiUtil.customizeTooltip(slot, output.probability);
            }
        }
    }

    @Override
    public void draw(MachineRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {

        var client = Minecraft.getInstance();
        var helper = client.screen;

        // Draw progress bar
        double recipeMillis = getSeconds(recipe) * 1000;

        var offset = getOffset();

        ProgressBar.RenderHelper.renderProgress(helper, stack, offset.x, offset.y, params.progressBarParams,
                (float) (System.currentTimeMillis() / recipeMillis % 1.0));

        // Draw filled energy bar
        stack.pushPose();

        stack.translate(5, 5, 0);
        stack.scale(0.5f, 0.5f, 0.5f);
        switch (params.steamMode) {
        case BOTH -> {
            RenderSystem.setShaderTexture(0, MachineScreen.SLOT_ATLAS);
            helper.blit(stack, -2, -2, 80, 18, 20, 20);
        }
        case STEAM_ONLY -> {
            RenderSystem.setShaderTexture(0, new MIIdentifier("textures/item/steam_bucket.png"));
            GuiComponent.blit(stack, 0, 0, helper.getBlitOffset(), 0, 0, 16, 16, 16, 16);
        }
        case ELECTRIC_ONLY -> {
            EnergyBar.Client.Renderer.renderEnergy(helper, stack, 0, 0, 1);
        }
        }

        stack.popPose();

        var font = client.font;
        font.draw(stack, TextHelper.getEuTextTick(getEu(recipe)), 15 + (params.steamMode.steam ? 2 : 0), 5, 0xFF404040);

        var durationText = MIText.BaseDurationSeconds.text(getSeconds(recipe));
        var durationTextWidth = font.width(durationText);
        font.draw(stack, durationText, background.getWidth() - 5 - durationTextWidth, 5, 0xFF404040);

        // Draw steel hatch or upgrades
        var steelHatchRequired = isSteelHatchRequired(recipe);
        var upgradeEuRequired = isUpgradeEuRequired(recipe);

        // Conditions
        boolean conditionsRequired = isConditionsRequired(recipe);
        if (steelHatchRequired || upgradeEuRequired > 0 || conditionsRequired) {
            IDrawable displayedItem;
            if (steelHatchRequired) {
                displayedItem = steelHatch;
            } else if (conditionsRequired) {
                displayedItem = wrench;
            } else {
                displayedItem = upgrade;
            }

            stack.pushPose();
            stack.translate(background.getWidth() / 2.f - 5, 4, 0);
            stack.scale(0.6f, 0.6f, 1);
            displayedItem.draw(stack);
            stack.popPose();
        }

    }

    @Override
    public List<Component> getTooltipStrings(MachineRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        var tooltipZone = new aztech.modern_industrialization.util.Rectangle(2, 5, background.getWidth() - 10, 11);
        if (!tooltipZone.contains(mouseX, mouseY)) {
            return Collections.emptyList();
        }

        // Tooltips
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(MIText.BaseEuTotal.text(TextHelper.getEuText((long) recipe.duration * recipe.eu)));
        if (params.steamMode.steam) {
            tooltips.add((params.steamMode.electric ? MIText.AcceptsSteamToo : MIText.AcceptsSteam).text().withStyle(ChatFormatting.GRAY));
            var steelHatchRequired = isSteelHatchRequired(recipe);
            if (steelHatchRequired) {
                tooltips.add(MIText.RequiresSteelHatch0.text().setStyle(Style.EMPTY.withUnderlined(true)));
                tooltips.add(MIText.RequiresSteelHatch1.text().withStyle(ChatFormatting.GRAY));
            }
        }

        var upgradeEuRequired = isUpgradeEuRequired(recipe);
        if (upgradeEuRequired > 0) {
            tooltips.add(new MITooltips.Line(MIText.RequiresUpgrades).arg(upgradeEuRequired, EU_PER_TICK_PARSER).build());
        }

        if (isConditionsRequired(recipe)) {
            for (var condition : recipe.conditions) {
                condition.appendDescription(tooltips);
            }
        }

        return tooltips;

    }

    private int isUpgradeEuRequired(MachineRecipe recipe) {
        int upgradeEuRequired = getEu(recipe) - (params.isMultiblock ? MachineTier.MULTIBLOCK : MachineTier.LV).getMaxEu();
        // Ugly fusion reactor workaround
        if (upgradeEuRequired > 0 && type.getUid().getPath().equals("fusion_reactor")) {
            upgradeEuRequired = 0;
        }
        return upgradeEuRequired;
    }

    private boolean isSteelHatchRequired(MachineRecipe recipe) {
        return params.steamMode.steam && params.isMultiblock && getEu(recipe) > MachineTier.BRONZE.getMaxEu();
    }

    private static boolean isConditionsRequired(MachineRecipe recipe) {
        return recipe.conditions.size() > 0;
    }

    private int getDisplayHeight() {
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

    private int getDisplayWidth() {
        int x = 1000, X = 0;
        for (SlotPositions positions : new SlotPositions[] { params.itemInputs, params.itemOutputs, params.fluidInputs, params.fluidOutputs }) {
            for (int i = 0; i < positions.size(); ++i) {
                x = Math.min(x, positions.getX(i));
                X = Math.max(X, positions.getX(i) + 16);
            }
        }

        return Math.max(X - x + 15, 120);
    }

    private double getSeconds(MachineRecipe recipe) {
        return recipe.duration / 20.0;
    }

    private int getEu(MachineRecipe recipe) {
        return recipe.eu;
    }

    private int getTicks(MachineRecipe recipe) {
        return recipe.duration;
    }

    private DrawOffset getOffset() {
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
        int xoffset = (background.getWidth() - X + x) / 2 - x;
        int yoffset = 17 - y;

        return new DrawOffset(xoffset, yoffset);
    }

    private record DrawOffset(int x, int y) {
    }
}

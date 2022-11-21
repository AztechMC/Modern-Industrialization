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
package aztech.modern_industrialization.compat.jei.forgehammer_recipe;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.compat.jei.AbstractCategory;
import aztech.modern_industrialization.compat.jei.JeiUtil;
import aztech.modern_industrialization.items.ForgeTool;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

public class ForgeHammerRecipeCategory extends AbstractCategory implements IRecipeCategory<MachineRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable hammer;
    private final int startPointX;
    private final int startPointY;

    public ForgeHammerRecipeCategory(IGuiHelper helper) {
        super(helper);
        this.background = helper.createBlankDrawable(150, 40);
        this.icon = helper.createDrawableItemStack(MIBlock.FORGE_HAMMER.asItem().getDefaultInstance());
        this.slot = helper.getSlotDrawable();
        this.hammer = helper.createDrawable(ForgeHammerScreen.FORGE_HAMMER_GUI, 7, 32, 18, 18);

        this.startPointX = background.getWidth() / 2 - 25;
        this.startPointY = background.getHeight() / 2 - 18;
    }

    @Override
    public RecipeType<MachineRecipe> getRecipeType() {
        return ForgeHammerRecipePlugin.CATEGORY;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(MIBlock.FORGE_HAMMER.getTranslationKey());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {

        MachineRecipe.ItemInput input = recipe.itemInputs.get(0);
        MachineRecipe.ItemOutput output = recipe.itemOutputs.get(0);

        builder.addSlot(RecipeIngredientRole.INPUT, startPointX + 5, startPointY + 6)
                .addIngredients(VanillaTypes.ITEM_STACK, JeiUtil.getItemStacks(input));

        if (recipe.eu > 0) {
            builder.addSlot(RecipeIngredientRole.INPUT, startPointX - 23, startPointY + 6)
                    .addIngredients(Ingredient.of(ForgeTool.TAG));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, startPointX + 62, startPointY + 6)
                .addItemStack(JeiUtil.getItemStack(output));

    }

    @Override
    public void draw(MachineRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {

        getArrow(recipe.duration).draw(stack, startPointX + 29, startPointY + 6);

        hammer.draw(stack, startPointX - 24, startPointY + 5);

        Component text;

        slot.draw(stack, startPointX + 4, startPointY + 5);
        if (recipe.eu > 0) {
            text = MIText.DurabilityCost.text(recipe.eu);
        } else {
            text = MIText.NoToolRequired.text();
        }
        slot.draw(stack, startPointX + 61, startPointY + 5);

        var font = Minecraft.getInstance().font;
        font.draw(stack, text, startPointX - 24, 28, 0xFF404040);
    }
}

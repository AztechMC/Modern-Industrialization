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
package aztech.modern_industrialization.compat.jei.fluid_fuels;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public class FluidFuelsCategory implements IRecipeCategory<Fluid> {

    private final IDrawable background;
    private final IDrawable icon;

    private final IDrawableStatic slot;

    public FluidFuelsCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(MIFluids.DIESEL.getBucket().getDefaultInstance());
        this.background = guiHelper.createBlankDrawable(150, 35);
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<Fluid> getRecipeType() {
        return FluidFuelsPlugin.CATEGORY;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Fluid recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 13, 10)
                .addFluidStack(recipe, FluidConstants.BUCKET);
    }

    @Override
    public void draw(Fluid recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        this.slot.draw(stack, 12, 9);

        int totalEnergy = FluidFuelRegistry.getEu(recipe);

        Component text = MIText.EuInDieselGenerator.text(totalEnergy);

        var minecraft = Minecraft.getInstance();
        minecraft.font.draw(stack, text, 40, 14, 0xFF404040);
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public Component getTitle() {
        return MIText.FluidFuels.text();
    }
}

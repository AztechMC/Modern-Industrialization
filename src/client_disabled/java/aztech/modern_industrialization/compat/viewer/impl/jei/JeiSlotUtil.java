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
package aztech.modern_industrialization.compat.viewer.impl.jei;

import aztech.modern_industrialization.compat.viewer.impl.ViewerUtil;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.client.fluid.FluidVariantRendering;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;

class JeiSlotUtil {
    private JeiSlotUtil() {
    }

    public static void customizeTooltip(IRecipeSlotBuilder slot, float probability) {
        slot.addTooltipCallback((recipeSlotView, tooltip) -> {
            // Add amounts for fluids to the tooltip
            recipeSlotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK)
                    .ifPresent(fluidIngredient -> {
                        tooltip.add(1, FluidHelper.getFluidAmount(fluidIngredient.getAmount()));
                    });

            var input = recipeSlotView.getRole() == RecipeIngredientRole.INPUT;
            var probabilityLine = ViewerUtil.getProbabilityTooltip(probability, input);
            if (probabilityLine != null) {
                tooltip.add(probabilityLine);
            }
        });
    }

    /**
     * Override the fluid renderer to always display the full sprite (instead of JEI's partial rendering),
     * and also clear any amount tooltip.
     */
    public static void overrideFluidRenderer(IRecipeSlotBuilder slot) {
        slot.setCustomRenderer(NeoForgeTypes.FLUID_STACK, new IIngredientRenderer<>() {
            @Override
            public void render(GuiGraphics guiGraphics, FluidStack ingredient) {
                RenderHelper.drawFluidInGui(guiGraphics, FluidVariant.of(ingredient), 0, 0);
            }

            @Override
            public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
                return FluidVariantRendering.getTooltip(FluidVariant.of(ingredient), tooltipFlag);
            }
        });
    }
}

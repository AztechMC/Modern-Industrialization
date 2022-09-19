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
package aztech.modern_industrialization.compat.jei;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import java.text.DecimalFormat;
import java.util.List;
import javax.annotation.Nullable;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class JeiUtil {

    private static final DecimalFormat PROBABILITY_FORMAT = new DecimalFormat("#.#");

    private JeiUtil() {
    }

    public static List<ItemStack> getItemStacks(MachineRecipe.ItemInput input) {
        return input.getInputItems().stream().map(i -> new ItemStack(i, input.amount)).toList();
    }

    public static ItemStack getItemStack(MachineRecipe.ItemOutput output) {
        return new ItemStack(output.item, output.amount);
    }

    @Nullable
    public static Component getProbabilityTooltip(float probability, boolean input) {
        if (probability == 1) {
            return null;
        } else {
            MutableComponent text;
            if (probability == 0) {
                text = MIText.NotConsumed.text();
            } else {
                if (input) {
                    text = MIText.ChanceConsumption.text(PROBABILITY_FORMAT.format(probability * 100));
                } else {
                    text = MIText.ChanceProduction.text(PROBABILITY_FORMAT.format(probability * 100));
                }

            }
            text.setStyle(TextHelper.YELLOW);
            return text;
        }
    }

    public static void customizeTooltip(IRecipeSlotBuilder slot) {
        customizeTooltip(slot, 1);
    }

    public static void customizeTooltip(IRecipeSlotBuilder slot, float probability) {
        slot.addTooltipCallback((recipeSlotView, tooltip) -> {
            // Add amounts for fluids to the tooltip
            recipeSlotView.getDisplayedIngredient(FabricTypes.FLUID_STACK)
                    .ifPresent(fluidIngredient -> {
                        tooltip.add(1, FluidHelper.getFluidAmount(fluidIngredient.getAmount()));
                    });

            var input = recipeSlotView.getRole() == RecipeIngredientRole.INPUT;
            var probabilityLine = getProbabilityTooltip(probability, input);
            if (probabilityLine != null) {
                tooltip.add(probabilityLine);
            }
        });
    }

    /**
     * Override the fluid renderer to always display the full sprite (instead of JEI's partial rendering).
     */
    public static void overrideFluidRenderer(IRecipeSlotBuilder slot) {
        slot.setCustomRenderer(FabricTypes.FLUID_STACK, new IIngredientRenderer<>() {
            @Override
            public void render(PoseStack stack, IJeiFluidIngredient ingredient) {
                RenderHelper.drawFluidInGui(stack, getVariant(ingredient), 0, 0);
            }

            @Override
            public List<Component> getTooltip(IJeiFluidIngredient ingredient, TooltipFlag tooltipFlag) {
                return FluidVariantRendering.getTooltip(getVariant(ingredient), tooltipFlag);
            }

            private FluidVariant getVariant(IJeiFluidIngredient ingredient) {
                return FluidVariant.of(ingredient.getFluid(), ingredient.getTag().orElse(null));
            }
        });
    }
}

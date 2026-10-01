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

package aztech.modern_industrialization.client.compat.guideme.recipe;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerRecipe;
import aztech.modern_industrialization.client.compat.guideme.recipe.header.LytMachineRecipeHeader;
import aztech.modern_industrialization.client.compat.guideme.recipe.header.LytMachineRecipeHeaderConditionItemImage;
import aztech.modern_industrialization.client.compat.guideme.recipe.header.LytMachineRecipeHeaderEuCostImage;
import aztech.modern_industrialization.client.compat.guideme.recipe.item.LytMIItemSlot;
import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.items.ForgeTool;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.TextHelper;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.DefaultStyles;
import guideme.document.LytSize;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytGuiSprite;
import guideme.document.block.LytHBox;
import guideme.document.block.LytParagraph;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.render.GuiAssets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MIRecipeTypeContributions implements RecipeTypeMappingSupplier {
    @Override
    public void collect(RecipeTypeMappings mappings) {
        mappings.add(MIRegistries.FORGE_HAMMER_RECIPE_TYPE.get(), MIRecipeTypeContributions::forgeHammer);

        for (var recipeType : MIMachineRecipeTypes.getRecipeTypes()) {
            mappings.add(recipeType, MIRecipeTypeContributions::machine);
        }
    }

    private static LytStandardRecipeBox<ForgeHammerRecipe> forgeHammer(RecipeHolder<ForgeHammerRecipe> holder) {
        var recipe = holder.value();
        var requiresTool = recipe.hammerDamage() > 0;

        var durabilityText = new LytParagraph();
        durabilityText.setStyle(DefaultStyles.CRAFTING_RECIPE_TYPE);
        durabilityText.appendText(requiresTool ? MIText.DurabilityCost.text("" + recipe.hammerDamage()).getString() : MIText.NoToolRequired.text().getString());

        return LytStandardRecipeBox.builder()
                .icon(MIBlock.FORGE_HAMMER)
                .title(MIBlock.FORGE_HAMMER.asItem().getDescription().getString())
                .addTop(durabilityText)
                .customBody(forgeHammerGrid(recipe))
                .build(holder);
    }

    private static LytBlock forgeHammerGrid(ForgeHammerRecipe recipe) {
        var requiresTool = recipe.hammerDamage() > 0;

        var grid = new LytHBox();
        grid.setGap(2);
        grid.setAlignItems(AlignItems.CENTER);
        grid.setWrap(false);

        grid.append(requiresTool ? new LytMIItemSlot(Ingredient.of(ForgeTool.TAG), 1, 1, true) : new LytMIItemSlot(true));
        grid.append(new LytMIItemSlot(recipe.ingredient(), recipe.count(), 1, true));

        grid.append(new LytGuiSprite(GuiAssets.ARROW, new LytSize(24, 17)));

        grid.append(new LytMIItemSlot(ItemVariant.of(recipe.result()), recipe.result().getCount(), 1, false));

        return grid;
    }

    private static LytStandardRecipeBox<MachineRecipe> machine(RecipeHolder<MachineRecipe> holder) {
        var recipe = holder.value();
        for (var params : ReiMachineRecipes.categories.values()) {
            if (params.recipeType == recipe.getType() && params.recipePredicate.test(recipe)) {
                var paramsDimensions = params.calculateDimensions();
                return LytStandardRecipeBox.builder()
                        .icon(BuiltInRegistries.ITEM.get(params.workstations.getFirst()).getDefaultInstance())
                        .title(Component.translatable("rei_categories.%s.%s".formatted(params.category.getNamespace(), params.category.getPath())).getString())
                        .addTop(machineHeader(params, paramsDimensions, recipe))
                        .customBody(new LytMachineRecipeSlots(params, paramsDimensions, recipe))
                        .build(holder);
            }
        }
        throw new IllegalStateException("Could not find fitting machine category params for recipe " + holder.id());
    }

    private static LytBlock machineHeader(MachineCategoryParams params, MachineCategoryParams.Dimensions paramsDimensions, MachineRecipe recipe) {
        var header = new LytMachineRecipeHeader(params, recipe, paramsDimensions.width());
        header.setGap(2);
        header.setAlignItems(AlignItems.CENTER);

        header.append(new LytMachineRecipeHeaderEuCostImage(params.steamMode));

        var euCost = new LytParagraph();
        euCost.setStyle(DefaultStyles.CRAFTING_RECIPE_TYPE);
        euCost.appendText(TextHelper.getEuTextTick(recipe.eu).getString());
        header.append(euCost);

        boolean includeRecipeConditionDisplay = params.includeRecipeConditionDisplay(recipe);
        if (includeRecipeConditionDisplay) {
            var condition = new LytMachineRecipeHeaderConditionItemImage(params.getRecipeConditionDisplayItems(recipe));
            condition.setMarginLeft(5);
            condition.setMarginRight(5);
            header.append(condition);
        }

        var duration = new LytParagraph();
        duration.setStyle(DefaultStyles.CRAFTING_RECIPE_TYPE);
        duration.appendText(MIText.BaseDurationSeconds.text(recipe.duration / 20.0).getString());
        if (!includeRecipeConditionDisplay) {
            duration.setMarginLeft(10);
        }
        header.append(duration);

        return header;
    }
}

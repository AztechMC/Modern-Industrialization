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
package aztech.modern_industrialization.compat.viewer.usage;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerRecipe;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import aztech.modern_industrialization.items.ForgeTool;
import java.util.Comparator;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public class ForgeHammerCategory extends ViewerCategory<RecipeHolder<ForgeHammerRecipe>> {
    public static final ResourceLocation ID = new MIIdentifier("forge_hammer");

    private final int startPointX;
    private final int startPointY;

    protected ForgeHammerCategory() {
        super((Class) RecipeHolder.class, ID, MIBlock.FORGE_HAMMER.asBlock().getName(),
                MIBlock.FORGE_HAMMER.asItem().getDefaultInstance(), 150, 40);

        this.startPointX = width / 2 - 25;
        this.startPointY = height / 2 - 18;
    }

    @Override
    public void buildWorkstations(WorkstationConsumer consumer) {
        consumer.accept(MIBlock.FORGE_HAMMER);
    }

    @Override
    public void buildRecipes(RecipeManager recipeManager, RegistryAccess registryAccess, Consumer<RecipeHolder<ForgeHammerRecipe>> consumer) {
        recipeManager.getAllRecipesFor(MIRegistries.FORGE_HAMMER_RECIPE_TYPE.get())
                .stream()
                .sorted(Comparator.comparing(RecipeHolder::id))
                .forEach(consumer);
    }

    @Override
    public void buildLayout(RecipeHolder<ForgeHammerRecipe> holder, LayoutBuilder builder) {
        var recipe = holder.value();

        builder.inputSlot(startPointX + 5, startPointY + 6).ingredient(recipe.ingredient(), recipe.count(), 1);

        if (recipe.hammerDamage() > 0) {
            builder.inputSlot(startPointX - 23, startPointY + 6).ingredient(Ingredient.of(ForgeTool.TAG), 1, 1).removeBackground().markCatalyst();
        }

        builder.outputSlot(startPointX + 62, startPointY + 6).item(recipe.result());
    }

    @Override
    public void buildWidgets(RecipeHolder<ForgeHammerRecipe> holder, WidgetList widgets) {
        var recipe = holder.value();

        // Draw arrow
        widgets.arrow(startPointX + 27, startPointY + 4);

        // Draw hammer
        widgets.texture(ForgeHammerScreen.FORGE_HAMMER_GUI, startPointX - 24, startPointY + 5, 7, 32, 18, 18);

        Component text = recipe.hammerDamage() > 0 ? MIText.DurabilityCost.text(recipe.hammerDamage()) : MIText.NoToolRequired.text();
        widgets.secondaryText(text, startPointX - 24, 28);
    }
}

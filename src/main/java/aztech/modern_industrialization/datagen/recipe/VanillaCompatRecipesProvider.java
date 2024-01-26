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
package aztech.modern_industrialization.datagen.recipe;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.recipe.json.ShapelessRecipeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Datagen for recipes that produce vanilla materials using MI machines.
 */
public class VanillaCompatRecipesProvider extends MIRecipesProvider {
    public VanillaCompatRecipesProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void buildRecipes(RecipeOutput exporter) {
        generateCopperOxidation(exporter, Items.COPPER_BLOCK, Items.EXPOSED_COPPER, Items.WEATHERED_COPPER, Items.OXIDIZED_COPPER);
        generateCopperOxidation(exporter, Items.CUT_COPPER, Items.EXPOSED_CUT_COPPER, Items.WEATHERED_CUT_COPPER, Items.OXIDIZED_CUT_COPPER);
        generateCopperOxidation(exporter, Items.CUT_COPPER_SLAB, Items.EXPOSED_CUT_COPPER_SLAB, Items.WEATHERED_CUT_COPPER_SLAB,
                Items.OXIDIZED_CUT_COPPER_SLAB);
        generateCopperOxidation(exporter, Items.CUT_COPPER_STAIRS, Items.EXPOSED_CUT_COPPER_STAIRS, Items.WEATHERED_CUT_COPPER_STAIRS,
                Items.OXIDIZED_CUT_COPPER_STAIRS);
        // waxed variants
        generateCopperOxidation(exporter, Items.WAXED_COPPER_BLOCK, Items.WAXED_EXPOSED_COPPER, Items.WAXED_WEATHERED_COPPER,
                Items.WAXED_OXIDIZED_COPPER);
        generateCopperOxidation(exporter, Items.WAXED_CUT_COPPER, Items.WAXED_EXPOSED_CUT_COPPER, Items.WAXED_WEATHERED_CUT_COPPER,
                Items.WAXED_OXIDIZED_CUT_COPPER);
        generateCopperOxidation(exporter, Items.WAXED_CUT_COPPER_SLAB, Items.WAXED_EXPOSED_CUT_COPPER_SLAB, Items.WAXED_WEATHERED_CUT_COPPER_SLAB,
                Items.WAXED_OXIDIZED_CUT_COPPER_SLAB);
        generateCopperOxidation(exporter, Items.WAXED_CUT_COPPER_STAIRS, Items.WAXED_EXPOSED_CUT_COPPER_STAIRS,
                Items.WAXED_WEATHERED_CUT_COPPER_STAIRS, Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS);

        // wax
        generateWax(exporter, Items.COPPER_BLOCK, Items.WAXED_COPPER_BLOCK);
        generateWax(exporter, Items.CUT_COPPER, Items.WAXED_CUT_COPPER);
        generateWax(exporter, Items.CUT_COPPER_SLAB, Items.WAXED_CUT_COPPER_SLAB);
        generateWax(exporter, Items.CUT_COPPER_STAIRS, Items.WAXED_CUT_COPPER_STAIRS);
        generateWax(exporter, Items.EXPOSED_COPPER, Items.WAXED_EXPOSED_COPPER);
        generateWax(exporter, Items.EXPOSED_CUT_COPPER, Items.WAXED_EXPOSED_CUT_COPPER);
        generateWax(exporter, Items.EXPOSED_CUT_COPPER_SLAB, Items.WAXED_EXPOSED_CUT_COPPER_SLAB);
        generateWax(exporter, Items.EXPOSED_CUT_COPPER_STAIRS, Items.WAXED_EXPOSED_CUT_COPPER_STAIRS);
        generateWax(exporter, Items.WEATHERED_COPPER, Items.WAXED_WEATHERED_COPPER);
        generateWax(exporter, Items.WEATHERED_CUT_COPPER, Items.WAXED_WEATHERED_CUT_COPPER);
        generateWax(exporter, Items.WEATHERED_CUT_COPPER_SLAB, Items.WAXED_WEATHERED_CUT_COPPER_SLAB);
        generateWax(exporter, Items.WEATHERED_CUT_COPPER_STAIRS, Items.WAXED_WEATHERED_CUT_COPPER_STAIRS);
        generateWax(exporter, Items.OXIDIZED_COPPER, Items.WAXED_OXIDIZED_COPPER);
        generateWax(exporter, Items.OXIDIZED_CUT_COPPER, Items.WAXED_OXIDIZED_CUT_COPPER);
        generateWax(exporter, Items.OXIDIZED_CUT_COPPER_SLAB, Items.WAXED_OXIDIZED_CUT_COPPER_SLAB);
        generateWax(exporter, Items.OXIDIZED_CUT_COPPER_STAIRS, Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS);

        // misc recipes
        new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 2, 100)
                .addItemInput(Items.STONE, 1)
                .addItemOutput(Items.COBBLESTONE, 1)
                .offerTo(exporter, "vanilla_recipes/macerator/stone_to_cobblestone");
    }

    private void generateCopperOxidation(RecipeOutput exporter, Item unaffected, Item exposed, Item weathered, Item oxidized) {
        oxidize(exporter, unaffected, exposed);
        oxidize(exporter, exposed, weathered);
        oxidize(exporter, weathered, oxidized);
    }

    private void oxidize(RecipeOutput exporter, Item from, Item to) {
        new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 8, 100)
                .addItemInput(from, 1)
                .addFluidInput(MIFluids.OXYGEN, 100)
                .addItemOutput(to, 1)
                .offerTo(exporter, "vanilla_recipes/oxidation/" + BuiltInRegistries.ITEM.getKey(from).getPath());
    }

    private void generateWax(RecipeOutput exporter, Item from, Item to) {
        var recipe = ShapelessRecipeBuilder.shapeless(to).requires(from).requires(MIItem.WAX);
        recipe.offerTo(exporter, "vanilla_recipes/waxing/" + BuiltInRegistries.ITEM.getKey(from).getPath());

        MachineRecipeBuilder chemicalReactorRecipe = new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 8, 100)
                .addItemInput(from, 1)
                .addItemInput(MIItem.WAX, 1)
                .addItemOutput(to, 1);

        chemicalReactorRecipe.offerTo(exporter, "vanilla_recipes/chemical_reactor/waxing/" + BuiltInRegistries.ITEM.getKey(from).getPath());
    }
}

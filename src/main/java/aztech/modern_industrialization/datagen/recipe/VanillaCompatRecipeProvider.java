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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.WeatheringCopperCollection;

import java.util.concurrent.CompletableFuture;

/**
 * Datagen for recipes that produce vanilla materials using MI machines.
 */
public class VanillaCompatRecipeProvider extends BaseRecipeProvider {
    protected VanillaCompatRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    public void buildRecipes() {
        // MC 26.2 unified copper variants into WeatheringCopperCollection<Item>; iterate the families
        // instead of referencing ~56 now-removed individual Items.* fields.
        generateCopperFamily(Items.COPPER_BLOCK);
        generateCopperFamily(Items.CUT_COPPER);
        generateCopperFamily(Items.CUT_COPPER_SLAB);
        generateCopperFamily(Items.CUT_COPPER_STAIRS);

        // misc recipes
        machine(MIMachineRecipeTypes.MACERATOR, 2, 100)
                .itemIn(Items.STONE, 1)
                .itemOut(Items.COBBLESTONE, 1)
                .offerTo(output, "vanilla_recipes/macerator/stone_to_cobblestone");
    }

    private void generateCopperFamily(WeatheringCopperCollection<Item> family) {
        generateCopperOxidation(family.weathering());
        generateCopperOxidation(family.waxed());

        // wax: each weathering state -> its waxed counterpart
        var weathering = family.weathering();
        var waxed = family.waxed();
        generateWax(weathering.unaffected(), waxed.unaffected());
        generateWax(weathering.exposed(), waxed.exposed());
        generateWax(weathering.weathered(), waxed.weathered());
        generateWax(weathering.oxidized(), waxed.oxidized());
    }

    private void generateCopperOxidation(WeatheringCopperCollection.ByState<Item> states) {
        oxidize(states.unaffected(), states.exposed());
        oxidize(states.exposed(), states.weathered());
        oxidize(states.weathered(), states.oxidized());
    }

    private void oxidize(Item from, Item to) {
        machine(MIMachineRecipeTypes.CHEMICAL_REACTOR, 8, 100)
                .itemIn(from, 1)
                .fluidIn(MIFluids.OXYGEN, 100)
                .itemOut(to, 1)
                .offerTo(output, "vanilla_recipes/oxidation/" + BuiltInRegistries.ITEM.getKey(from).getPath());
    }

    private void generateWax(Item from, Item to) {
        var recipe = ShapelessRecipeBuilder.shapeless(to).requires(from).requires(MIItem.WAX);
        recipe.offerTo(output, "vanilla_recipes/waxing/" + BuiltInRegistries.ITEM.getKey(from).getPath());

        MachineRecipeBuilder chemicalReactorRecipe = machine(MIMachineRecipeTypes.CHEMICAL_REACTOR, 8, 100)
                .itemIn(from, 1)
                .itemIn(MIItem.WAX, 1)
                .itemOut(to, 1);

        chemicalReactorRecipe.offerTo(output, "vanilla_recipes/chemical_reactor/waxing/" + BuiltInRegistries.ITEM.getKey(from).getPath());
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new VanillaCompatRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Vanilla Compat Recipes";
        }
    }
}

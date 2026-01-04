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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.recipe.json.ShapelessRecipeBuilder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;

public class UpgradeRecipeProvider extends BaseRecipeProvider {
    protected UpgradeRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    public void buildRecipes() {
        buildSteelUpgrades();
        buildQuantumUpgrades();
    }

    private static final String[] STEEL_UPGRADE_MACHINES = { "compressor", "macerator", "cutting_machine", "water_pump", "mixer", "furnace",
            "boiler" };
    private static final Set<String> STEEL_NO_UNPACKER = Set.of("furnace", "boiler");

    private void buildSteelUpgrades() {
        Item upgrade = BuiltInRegistries.ITEM.getValue(MI.id("steel_upgrade"));

        for (String machine : STEEL_UPGRADE_MACHINES) {
            Item bronze = BuiltInRegistries.ITEM.getValue(MI.id("bronze_" + machine));
            Item steel = BuiltInRegistries.ITEM.getValue(MI.id("steel_" + machine));

            var recipe = ShapelessRecipeBuilder.shapeless(steel)
                    .requires(bronze)
                    .requires(upgrade);
            recipe.offerTo(output, "upgrade/craft/steel/" + machine);

            var recipePacker = machine(MIMachineRecipeTypes.PACKER, 2, 100).itemIn(bronze, 1)
                    .itemIn(upgrade, 1).itemOut(steel, 1);
            recipePacker.offerTo(output, "upgrade/packer/steel/" + machine);

            if (!STEEL_NO_UNPACKER.contains(machine)) {
                var recipeUnpacker = machine(MIMachineRecipeTypes.UNPACKER, 2, 100).itemOut(bronze, 1)
                        .itemOut(upgrade, 1).itemIn(steel, 1);
                recipeUnpacker.offerTo(output, "upgrade/unpacker/steel/" + machine);
            }
        }
    }

    private static final List<String> QUANTUM_ITEMS = List.of("helmet", "chestplate", "leggings", "boots", "sword");

    private void buildQuantumUpgrades() {
        for (var itemType : QUANTUM_ITEMS) {
            var packerRecipe = machine(MIMachineRecipeTypes.PACKER, 1_000_000, 200)
                    .itemIn("minecraft:netherite_" + itemType, 1)
                    .itemIn("modern_industrialization:quantum_upgrade", 1)
                    .itemOut("modern_industrialization:quantum_" + itemType, 1);
            packerRecipe.offerTo(output, "upgrade/packer/quantum/" + itemType);
        }
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new UpgradeRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Upgrade Recipes";
        }
    }
}

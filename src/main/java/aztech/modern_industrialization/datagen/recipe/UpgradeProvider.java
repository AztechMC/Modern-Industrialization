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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.recipe.json.ShapelessRecipeBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;

public class UpgradeProvider extends MIRecipesProvider {

    public UpgradeProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        buildSteelUpgrades(consumer);
        buildQuantumUpgrades(consumer);
    }

    private static final String[] STEEL_UPGRADE_MACHINES = { "compressor", "macerator", "cutting_machine", "water_pump", "mixer", "furnace",
            "boiler" };
    private static final Set<String> STEEL_NO_UNPACKER = Set.of("furnace", "boiler");

    private void buildSteelUpgrades(RecipeOutput consumer) {
        Item upgrade = BuiltInRegistries.ITEM.get(new MIIdentifier("steel_upgrade"));

        for (String machine : STEEL_UPGRADE_MACHINES) {
            Item bronze = BuiltInRegistries.ITEM.get(new MIIdentifier("bronze_" + machine));
            Item steel = BuiltInRegistries.ITEM.get(new MIIdentifier("steel_" + machine));

            var recipe = ShapelessRecipeBuilder.shapeless(steel)
                    .requires(bronze)
                    .requires(upgrade);
            recipe.offerTo(consumer, "upgrade/craft/steel/" + machine);

            var recipePacker = new MachineRecipeBuilder(MIMachineRecipeTypes.PACKER, 2, 100).addItemInput(bronze, 1)
                    .addItemInput(upgrade, 1).addItemOutput(steel, 1);
            recipePacker.offerTo(consumer, "upgrade/packer/steel/" + machine);

            if (!STEEL_NO_UNPACKER.contains(machine)) {
                var recipeUnpacker = new MachineRecipeBuilder(MIMachineRecipeTypes.UNPACKER, 2, 100).addItemOutput(bronze, 1)
                        .addItemOutput(upgrade, 1).addItemInput(steel, 1);
                recipeUnpacker.offerTo(consumer, "upgrade/unpacker/steel/" + machine);
            }
        }
    }

    private static final List<String> QUANTUM_ITEMS = List.of("helmet", "chestplate", "leggings", "boots", "sword");

    private void buildQuantumUpgrades(RecipeOutput consumer) {
        for (var itemType : QUANTUM_ITEMS) {
            var packerRecipe = new MachineRecipeBuilder(MIMachineRecipeTypes.PACKER, 1_000_000, 200)
                    .addItemInput("minecraft:netherite_" + itemType, 1)
                    .addItemInput("modern_industrialization:quantum_upgrade", 1)
                    .addItemOutput("modern_industrialization:quantum_" + itemType, 1);
            packerRecipe.offerTo(consumer, "upgrade/packer/quantum/" + itemType);
        }
    }
}

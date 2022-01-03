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
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import aztech.modern_industrialization.recipe.json.SmithingRecipeJson;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

public class SteelUpgradeProvider extends MIRecipesProvider {

    private static final String[] WITH_UPGRADE = { "compressor", "macerator", "cutting_machine", "water_pump", "mixer" };

    public SteelUpgradeProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> consumer) {

        Item upgrade = Registry.ITEM.get(new MIIdentifier("steel_upgrade"));

        for (String machine : WITH_UPGRADE) {
            Item bronze = Registry.ITEM.get(new MIIdentifier("bronze_" + machine));
            Item steel = Registry.ITEM.get(new MIIdentifier("steel_" + machine));

            SmithingRecipeJson recipe = new SmithingRecipeJson(bronze, upgrade, steel);
            recipe.offerTo(consumer, "upgrade/smithing/steel/" + machine);

            MIRecipeJson recipePacker = MIRecipeJson.create(MIMachineRecipeTypes.PACKER, 2, 100).addItemInput(bronze, 1)
                    .addItemInput(upgrade, 1).addItemOutput(steel, 1);
            recipePacker.offerTo(consumer, "upgrade/packer/steel/" + machine);

            MIRecipeJson recipeUnpacker = MIRecipeJson.create(MIMachineRecipeTypes.UNPACKER, 2, 100).addItemOutput(bronze, 1)
                    .addItemOutput(upgrade, 1).addItemInput(steel, 1);
            recipeUnpacker.offerTo(consumer, "upgrade/unpacker/steel/" + machine);
        }

    }
}

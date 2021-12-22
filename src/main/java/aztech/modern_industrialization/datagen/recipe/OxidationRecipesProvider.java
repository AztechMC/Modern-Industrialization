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
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.server.recipe.RecipeJsonProvider;

public final class OxidationRecipesProvider extends MIRecipesProvider {

    public OxidationRecipesProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<RecipeJsonProvider> consumer) {
        genOxidation(consumer, "cut_copper");
        genOxidation(consumer, "cut_copper_slab");
        genOxidation(consumer, "cut_copper_stairs");

        //Gotta love how this one item just randomly has _block at the end, while literally everything else doesn't
        genOxidationCopperBlock(consumer, "copper");
    }


    private static void genOxidation(Consumer<RecipeJsonProvider> consumer, String item) {
        MIRecipeJson exposed = MIRecipeJson.create(MIMachineRecipeTypes.CHEMICAL_REACTOR, 4, 400).addFluidInput(MIFluids.OXYGEN, 100)
                .addItemInput("minecraft:" + item, 1).addItemOutput("minecraft:exposed" + "_" + item, 1);

        exposed.offerTo(consumer, "chemical_reactor/exposed/" + item);

        MIRecipeJson weathered = MIRecipeJson.create(MIMachineRecipeTypes.CHEMICAL_REACTOR, 4, 400).addFluidInput(MIFluids.OXYGEN, 100)
                .addItemInput("minecraft:exposed" + "_" + item, 1).addItemOutput("minecraft:weathered" + "_" + item, 1);

        weathered.offerTo(consumer, "chemical_reactor/weathered/" + item);

        MIRecipeJson slab = MIRecipeJson.create(MIMachineRecipeTypes.CHEMICAL_REACTOR, 4, 400).addFluidInput(MIFluids.OXYGEN, 100)
                .addItemInput("minecraft:weathered" + "_" + item, 1).addItemOutput("minecraft:oxidized" + "_" + item, 1);

        oxidized.offerTo(consumer, "chemical_reactor/oxidized/" + item);
    }

    private static void genOxidationCopperBlock(Consumer<RecipeJsonProvider> consumer, String item) {
        MIRecipeJson exposed = MIRecipeJson.create(MIMachineRecipeTypes.CHEMICAL_REACTOR, 4, 400).addFluidInput(MIFluids.OXYGEN, 100)
                .addItemInput("minecraft:" + item + "_block", 1).addItemOutput("minecraft:exposed" + "_" + item, 1);

        exposed.offerTo(consumer, "chemical_reactor/exposed/" + item);

        MIRecipeJson weathered = MIRecipeJson.create(MIMachineRecipeTypes.CHEMICAL_REACTOR, 4, 400).addFluidInput(MIFluids.OXYGEN, 100)
                .addItemInput("minecraft:exposed" + "_" + item, 1).addItemOutput("minecraft:weathered" + "_" + item, 1);

        weathered.offerTo(consumer, "chemical_reactor/weathered/" + item);

        MIRecipeJson oxidized = MIRecipeJson.create(MIMachineRecipeTypes.CHEMICAL_REACTOR, 4, 400).addFluidInput(MIFluids.OXYGEN, 100)
                .addItemInput("minecraft:weathered" + "_" + item, 1).addItemOutput("minecraft:oxidized" + "_" + item, 1);

        oxidized.offerTo(consumer, "chemical_reactor/oxidized/" + item);
    }

}

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
package aztech.modern_industrialization.datagen;

import aztech.modern_industrialization.datagen.advancement.MIAdvancementsProvider;
import aztech.modern_industrialization.datagen.loot.BlockLootTableProvider;
import aztech.modern_industrialization.datagen.recipe.AlloyRecipesProvider;
import aztech.modern_industrialization.datagen.recipe.AssemblerRecipesProvider;
import aztech.modern_industrialization.datagen.recipe.CompatRecipesProvider;
import aztech.modern_industrialization.datagen.recipe.DyeRecipesProvider;
import aztech.modern_industrialization.datagen.recipe.HatchRecipesProvider;
import aztech.modern_industrialization.datagen.recipe.HeatExchangerRecipesProvider;
import aztech.modern_industrialization.datagen.recipe.MaterialRecipesProvider;
import aztech.modern_industrialization.datagen.recipe.PetrochemRecipesProvider;
import aztech.modern_industrialization.datagen.recipe.PlankRecipesProvider;
import aztech.modern_industrialization.datagen.recipe.SteelUpgradeProvider;
import aztech.modern_industrialization.datagen.recipe.VanillaCompatRecipesProvider;
import aztech.modern_industrialization.datagen.tag.MIBlockTagProvider;
import aztech.modern_industrialization.datagen.tag.MIItemTagProvider;
import aztech.modern_industrialization.datagen.tag.MIPoiTypeTagProvider;
import aztech.modern_industrialization.datagen.translation.TranslationProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class MIDatagenServer {
    public static void configure(FabricDataGenerator gen, boolean runtimeDatagen) {
	FabricDataGenerator.Pack pack = gen.createPack();

        pack.addProvider(PetrochemRecipesProvider::new);
        pack.addProvider(PlankRecipesProvider::new);
        pack.addProvider(HeatExchangerRecipesProvider::new);
        pack.addProvider(HatchRecipesProvider::new);
        pack.addProvider(AlloyRecipesProvider::new);
        pack.addProvider(MaterialRecipesProvider::new);
        pack.addProvider(DyeRecipesProvider::new);
        pack.addProvider(AssemblerRecipesProvider::new);
        pack.addProvider(CompatRecipesProvider::new);
        pack.addProvider(SteelUpgradeProvider::new);
        pack.addProvider(VanillaCompatRecipesProvider::new);

        pack.addProvider(BlockLootTableProvider::new);

        pack.addProvider(MIBlockTagProvider::new);
	// FIXME 1.19.3
        //pack.addProvider(new MIItemTagProvider(pack.output, runtimeDatagen));
        pack.addProvider(MIPoiTypeTagProvider::new);

        /*var translationProvider = new TranslationProvider(pack.output, runtimeDatagen);
        pack.addProvider(new MIAdvancementsProvider(output, translationProvider));
        pack.addProvider(translationProvider);*/

        //pack.addProvider(WorldgenProvider::new);
    }
}

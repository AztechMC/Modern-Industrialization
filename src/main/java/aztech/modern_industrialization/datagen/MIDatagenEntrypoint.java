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

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.datagen.advancement.MIAdvancementsProvider;
import aztech.modern_industrialization.datagen.model.MachineModelsProvider;
import aztech.modern_industrialization.datagen.recipe.*;
import aztech.modern_industrialization.datagen.tag.MIBlockTagProvider;
import aztech.modern_industrialization.datagen.tag.MIItemTagProvider;
import aztech.modern_industrialization.datagen.texture.TexturesProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class MIDatagenEntrypoint implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        ModernIndustrialization.LOGGER.info("Starting Modern Industrialization Datagen");

        dataGenerator.addProvider(PetrochemRecipesProvider::new);
        dataGenerator.addProvider(PlankRecipesProvider::new);
        dataGenerator.addProvider(HeatExchangerRecipesProvider::new);
        dataGenerator.addProvider(HatchRecipesProvider::new);
        dataGenerator.addProvider(AlloyRecipesProvider::new);
        dataGenerator.addProvider(MaterialRecipesProvider::new);
        dataGenerator.addProvider(DyeRecipesProvider::new);
        dataGenerator.addProvider(AssemblerRecipesProvider::new);
        dataGenerator.addProvider(CompatRecipesProvider::new);
        dataGenerator.addProvider(SteelUpgradeProvider::new);

        dataGenerator.addProvider(MIAdvancementsProvider::new);

        dataGenerator.addProvider(MachineModelsProvider::new);

        dataGenerator.addProvider(MIBlockTagProvider::new);
        dataGenerator.addProvider(MIItemTagProvider::new);

        dataGenerator.addProvider(TexturesProvider::new);

        ModernIndustrialization.LOGGER.info("Modern Industrialization Datagen done");
    }
}

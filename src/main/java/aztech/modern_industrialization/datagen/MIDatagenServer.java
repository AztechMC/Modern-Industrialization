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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.datagen.advancement.MIAdvancementsProvider;
import aztech.modern_industrialization.datagen.dynreg.DynamicRegistryDatagen;
import aztech.modern_industrialization.datagen.loot.BlockLootTableProvider;
import aztech.modern_industrialization.datagen.recipe.MaterialRecipesProvider;
import aztech.modern_industrialization.datagen.translation.TranslationProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MIDatagenServer {
    public static void configure(
            DataGenerator gen,
            ExistingFileHelper fileHelper,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            boolean run,
            boolean runtimeDatagen) {
        // TODO NEO
//        var aggregate = pack.addProvider(AggregateDataProvider.create("Server Data"));
//
//        aggregate.addProvider(PetrochemRecipesProvider::new);
//        aggregate.addProvider(PlankRecipesProvider::new);
//        aggregate.addProvider(HeatExchangerRecipesProvider::new);
//        aggregate.addProvider(HatchRecipesProvider::new);
//        aggregate.addProvider(AlloyRecipesProvider::new);
        gen.addProvider(run, new MaterialRecipesProvider(gen.getPackOutput(), lookupProvider));
//        aggregate.addProvider(DyeRecipesProvider::new);
//        aggregate.addProvider(AssemblerRecipesProvider::new);
//        aggregate.addProvider(CompatRecipesProvider::new);
//        aggregate.addProvider(UpgradeProvider::new);
//        aggregate.addProvider(VanillaCompatRecipesProvider::new);

        gen.addProvider(run, new LootTableProvider(gen.getPackOutput(), Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLootTableProvider::new, LootContextParamSets.BLOCK))));

        gen.addProvider(run, new DatapackBuiltinEntriesProvider(gen.getPackOutput(), lookupProvider, DynamicRegistryDatagen.getBuilder(), Set.of(MI.ID)));

//        aggregate.addProvider(MIBlockTagProvider::new);
//        aggregate.addProvider((packOutput, registriesFuture) -> new MIItemTagProvider(packOutput, registriesFuture, runtimeDatagen));
//        aggregate.addProvider(MIPoiTypeTagProvider::new);

        var translationProvider = new TranslationProvider(gen.getPackOutput(), runtimeDatagen);
        gen.addProvider(run, new AdvancementProvider(gen.getPackOutput(), lookupProvider, fileHelper, List.of(
                new MIAdvancementsProvider(translationProvider))));

        // Must either remain separate or be made to use futures to wait for dependencies!
        gen.addProvider(run, translationProvider);
    }
}

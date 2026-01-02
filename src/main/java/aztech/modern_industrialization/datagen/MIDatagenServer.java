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
import aztech.modern_industrialization.datagen.tag.MIVillagerTradesTagProvider;
import aztech.modern_industrialization.trading.MITradeSets;
import aztech.modern_industrialization.datagen.advancement.MIAdvancementsProvider;
import aztech.modern_industrialization.datagen.datamap.MIDataMapProvider;
import aztech.modern_industrialization.datagen.dynreg.DynamicRegistryDatagen;
import aztech.modern_industrialization.datagen.loot.BlockLootTableProvider;
import aztech.modern_industrialization.datagen.loot.MIGiftLoot;
import aztech.modern_industrialization.datagen.recipe.AlloyRecipeProvider;
import aztech.modern_industrialization.datagen.recipe.CompatRecipeProvider;
import aztech.modern_industrialization.datagen.recipe.DyeRecipeProvider;
import aztech.modern_industrialization.datagen.recipe.HatchRecipeProvider;
import aztech.modern_industrialization.datagen.recipe.HeatExchangerRecipeProvider;
import aztech.modern_industrialization.datagen.recipe.MaterialRecipeProvider;
import aztech.modern_industrialization.datagen.recipe.PetrochemRecipeProvider;
import aztech.modern_industrialization.datagen.recipe.PlankRecipeProvider;
import aztech.modern_industrialization.datagen.recipe.UpgradeRecipeProvider;
import aztech.modern_industrialization.datagen.recipe.VanillaCompatRecipeProvider;
import aztech.modern_industrialization.datagen.structure.EmptyTestStructureGenerator;
import aztech.modern_industrialization.datagen.tag.MIBlockTagProvider;
import aztech.modern_industrialization.datagen.tag.MIFluidTagProvider;
import aztech.modern_industrialization.datagen.tag.MIItemTagProvider;
import aztech.modern_industrialization.datagen.tag.MIPoiTypeTagProvider;
import aztech.modern_industrialization.datagen.translation.TranslationProvider;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import aztech.modern_industrialization.trading.MITrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

public class MIDatagenServer {
    public static void configure(
            DataGenerator gen,
            CompletableFuture<HolderLookup.Provider> registries,
            boolean runtimeDatagen) {
        // TODO 26.1 - run all of them in parallel?
        var aggregate = gen.addProvider(true, new AggregateDataProvider(gen.getPackOutput(), registries, "Server Data"));

        aggregate.addProvider(PetrochemRecipeProvider.Runner::new);
        aggregate.addProvider(PlankRecipeProvider.Runner::new);
        aggregate.addProvider(HeatExchangerRecipeProvider.Runner::new);
        aggregate.addProvider(HatchRecipeProvider.Runner::new);
        aggregate.addProvider(AlloyRecipeProvider.Runner::new);
        aggregate.addProvider(MaterialRecipeProvider.Runner::new);
        aggregate.addProvider(DyeRecipeProvider.Runner::new);
        // TODO 26.1
//        aggregate.addProvider(AssemblerRecipeProvider.Runner::new);
        if (!runtimeDatagen) {
            aggregate.addProvider(CompatRecipeProvider.Runner::new);
        }
        aggregate.addProvider(UpgradeRecipeProvider.Runner::new);
        aggregate.addProvider(VanillaCompatRecipeProvider.Runner::new);

        aggregate.addProvider(EmptyTestStructureGenerator::new);

        gen.addProvider(true, new LootTableProvider(gen.getPackOutput(), Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLootTableProvider::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(MIGiftLoot::new, LootContextParamSets.GIFT)),
                registries));

        var registrySetBuilder = DynamicRegistryDatagen.getBuilder();
        registrySetBuilder.add(Registries.TRADE_SET, MITradeSets::bootstrap);
        registrySetBuilder.add(Registries.VILLAGER_TRADE, MITrades::bootstrap);
        var registriesWithMiPatch = RegistryPatchGenerator.createLookup(registries, registrySetBuilder);
        gen.addProvider(true,
                new DatapackBuiltinEntriesProvider(gen.getPackOutput(), registriesWithMiPatch, Set.of(MI.ID)));
        var registriesWithMi = registriesWithMiPatch.thenApply(RegistrySetBuilder.PatchedRegistries::full);

        gen.addProvider(true, new MIBlockTagProvider(gen.getPackOutput(), registries));
        gen.addProvider(true, new MIFluidTagProvider(gen.getPackOutput(), registries));
        gen.addProvider(true, new MIItemTagProvider(gen.getPackOutput(), registries, runtimeDatagen));
        gen.addProvider(true, new MIPoiTypeTagProvider(gen.getPackOutput(), registries));
        gen.addProvider(true, new MIVillagerTradesTagProvider(gen.getPackOutput(), registriesWithMi));

        gen.addProvider(true, new MIDataMapProvider(gen.getPackOutput(), registries));

        var translationProvider = new TranslationProvider(gen.getPackOutput());
        gen.addProvider(true, new AdvancementProvider(gen.getPackOutput(), registries, List.of(
                new MIAdvancementsProvider(translationProvider))));

        // Must either remain separate or be made to use futures to wait for dependencies!
        gen.addProvider(true, translationProvider);
    }
}

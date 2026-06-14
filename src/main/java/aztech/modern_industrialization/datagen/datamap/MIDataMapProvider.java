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

package aztech.modern_industrialization.datagen.datamap;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.api.datamaps.FluidFuel;
import aztech.modern_industrialization.api.datamaps.ItemPipeUpgrade;
import aztech.modern_industrialization.api.datamaps.ItemTooltip;
import aztech.modern_industrialization.api.datamaps.MIDataMaps;
import aztech.modern_industrialization.api.datamaps.MachineUpgrade;
import aztech.modern_industrialization.datagen.loot.MILootTables;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MIParts;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;

public class MIDataMapProvider extends DataMapProvider {
    public MIDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider registries) {
        gatherFurnaceFuels();

        gatherFluidFuels();
        gatherItemPipeUpgrades();
        gatherMachineUpgrades();
        gatherItemTooltips();

        builder(NeoForgeDataMaps.RAID_HERO_GIFTS)
                .add(MIRegistries.INDUSTRIALIST, new RaidHeroGift(MILootTables.INDUSTRIALIST_GIFT), false);
    }

    private void gatherFurnaceFuels() {
        addFuel("coke", 6400);
        addFuel("coke_dust", 6400);
        addFuel("coke_block", 6400 * 9);
        addFuel("coal_crushed_dust", 1600);
        builder(NeoForgeDataMaps.FURNACE_FUELS)
                .add(ItemTags.create(ResourceLocation.parse("c:dusts/coal")), new FurnaceFuel(1600), false);
        addFuel("coal_tiny_dust", 160);
        addFuel("lignite_coal", 1600);
        addFuel("lignite_coal_block", 16000);
        addFuel("lignite_coal_crushed_dust", 1600);
        addFuel("lignite_coal_dust", 1600);
        addFuel("lignite_coal_tiny_dust", 160);
        addFuel("carbon_dust", 6400);
        addFuel("carbon_tiny_dust", 640);
        addFuel(MIItem.GUIDE_BOOK.path(), 300);
    }

    private void addFuel(String path, int value) {
        builder(NeoForgeDataMaps.FURNACE_FUELS).add(MI.id(path), new FurnaceFuel(value), false);
    }

    private void gatherFluidFuels() {
        addFluidFuel(MIFluids.HYDROGEN, 1);
        addFluidFuel(MIFluids.DEUTERIUM, 1);
        addFluidFuel(MIFluids.TRITIUM, 1);
        addFluidFuel(MIFluids.CRUDE_OIL, 16);
        addFluidFuel(MIFluids.SYNTHETIC_OIL, 16);
        addFluidFuel(MIFluids.RAW_BIODIESEL, 50);
        addFluidFuel(MIFluids.NAPHTHA, 80);
        addFluidFuel(MIFluids.CREOSOTE, 160);
        addFluidFuel(MIFluids.LIGHT_FUEL, 160);
        addFluidFuel(MIFluids.HEAVY_FUEL, 240);
        addFluidFuel(MIFluids.BIODIESEL, 250);
        addFluidFuel(MIFluids.DIESEL, 400);
        addFluidFuel(MIFluids.BOOSTED_DIESEL, 800);
    }

    private void addFluidFuel(FluidDefinition fluidDefinition, int euPerMb) {
        builder(MIDataMaps.FLUID_FUELS).add(fluidDefinition.getId(), new FluidFuel(euPerMb), false);
    }

    private void gatherItemPipeUpgrades() {
        addItemPipeUpgrade(MIItem.MOTOR, 2);
        addItemPipeUpgrade(MIItem.LARGE_MOTOR, 16);
        addItemPipeUpgrade(MIItem.ADVANCED_MOTOR, 64);
        addItemPipeUpgrade(MIItem.LARGE_ADVANCED_MOTOR, 512);
    }

    private void addItemPipeUpgrade(ItemDefinition<?> itemDefinition, int maxExtractedItems) {
        builder(MIDataMaps.ITEM_PIPE_UPGRADES).add(itemDefinition.getId(), new ItemPipeUpgrade(maxExtractedItems), false);
    }

    private void gatherMachineUpgrades() {
        addMachineUpgrade(MIItem.BASIC_UPGRADE, 2);
        addMachineUpgrade(MIItem.ADVANCED_UPGRADE, 16);
        addMachineUpgrade(MIItem.TURBO_UPGRADE, 64);
        addMachineUpgrade(MIItem.HIGHLY_ADVANCED_UPGRADE, 512);
        addMachineUpgrade(MIItem.QUANTUM_UPGRADE, 999999999);
    }

    private void addMachineUpgrade(ItemDefinition<?> itemDefinition, int extraMaxEu) {
        builder(MIDataMaps.MACHINE_UPGRADES).add(itemDefinition.getId(), new MachineUpgrade(extraMaxEu), false);
    }

    private void gatherItemTooltips() {
        addItemTooltip("capacitor", MIText.HasBetterYieldAssemblerRecipe);
        addItemTooltip("inductor", MIText.HasBetterYieldAssemblerRecipe);
        addItemTooltip("resistor", MIText.HasBetterYieldAssemblerRecipe);
        // Gears get 2x the yield using the assembler.
        for (var material : MaterialRegistry.getMaterials().values()) {
            var part = material.getNullablePart(MIParts.GEAR);
            if (part != null) {
                addItemTooltip(part.getItemId().split(":")[1], MIText.HasBetterYieldAssemblerRecipe);
            }
        }
    }

    private void addItemTooltip(String path, MIText text) {
        var component = text.text().setStyle(MITooltips.DEFAULT_STYLE);
        builder(MIDataMaps.ITEM_TOOLTIPS).add(MI.id(path), new ItemTooltip(List.of(component)), false);
    }
}

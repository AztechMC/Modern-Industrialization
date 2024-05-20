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
package aztech.modern_industrialization.compat.kubejs.machine;

import static aztech.modern_industrialization.machines.init.SingleBlockCraftingMachines.*;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.GeneratorMachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.GeneratorMultiblockBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.SteamCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.FluidItemConsumerComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.RecipeEfficiencyBar;
import aztech.modern_industrialization.machines.init.MachineRegistrationHelper;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.init.SingleBlockCraftingMachines;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import dev.latvian.mods.kubejs.event.EventJS;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings("unused")
public class RegisterMachinesEventJS extends EventJS implements ShapeTemplateHelper {

    public ProgressBar.Parameters progressBar(int renderX, int renderY, String type) {
        return new ProgressBar.Parameters(renderX, renderY, type);
    }

    public RecipeEfficiencyBar.Parameters efficiencyBar(int renderX, int renderY) {
        return new RecipeEfficiencyBar.Parameters(renderX, renderY);
    }

    public EnergyBar.Parameters energyBar(int renderX, int renderY) {
        return new EnergyBar.Parameters(renderX, renderY);
    }

    public void craftingSingleBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, List<String> tiers,
            // gui
            int backgroundHeight, // can be -1 to use default
            ProgressBar.Parameters progressBar, RecipeEfficiencyBar.Parameters efficiencyBar, EnergyBar.Parameters energyBar,
            // slots
            int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs, int bucketCapacity,
            Consumer<SlotPositions.Builder> itemSlotPositions, Consumer<SlotPositions.Builder> fluidSlotPositions,
            // model
            boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {

        craftingSingleBlock(englishName, internalName, recipeType, tiers, backgroundHeight, progressBar, efficiencyBar, energyBar,
                itemInputs, itemOutputs, fluidInputs, fluidOutputs, bucketCapacity, itemSlotPositions, fluidSlotPositions,
                frontOverlay, topOverlay, sideOverlay, config -> {
                });
    }

    public void craftingSingleBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, List<String> tiers,
            // gui
            int backgroundHeight, // can be -1 to use default
            ProgressBar.Parameters progressBar, RecipeEfficiencyBar.Parameters efficiencyBar, EnergyBar.Parameters energyBar,
            // slots
            int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs, int bucketCapacity,
            Consumer<SlotPositions.Builder> itemSlotPositions, Consumer<SlotPositions.Builder> fluidSlotPositions,
            // model
            boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // Optional config
            Consumer<ExtraMachineConfig.CraftingSingleBlock> extraConfig) {

        var config = new SingleBlockCraftingMachines.Config();
        extraConfig.accept(new ExtraMachineConfig.CraftingSingleBlock(config));

        int tiersMask = 0;
        for (String tier : tiers) {
            tiersMask |= switch (tier) {
            case "bronze" -> TIER_BRONZE;
            case "steel" -> TIER_STEEL;
            case "electric" -> TIER_ELECTRIC;
            default -> throw new IllegalArgumentException("Unknown tier: " + tier);
            };
        }
        Consumer<MachineGuiParameters.Builder> guiParams = backgroundHeight < 0 ? p -> {
        } : p -> p.backgroundHeight(backgroundHeight);

        SingleBlockCraftingMachines.registerMachineTiers(
                englishName, internalName, recipeType,
                itemInputs, itemOutputs, fluidInputs, fluidOutputs,
                guiParams, progressBar, efficiencyBar, energyBar,
                itemSlotPositions, fluidSlotPositions,
                frontOverlay, topOverlay, sideOverlay,
                tiersMask, bucketCapacity, config);
    }

    private void simpleMultiBlock(
            // general
            String englishName, String internalName, ShapeTemplate multiblockShape,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // machine entity
            Function<BEP, MachineBlockEntity> factory) {
        var controllerCasing = MachineCasings.get(controllerCasingName);
        MachineRegistrationHelper.registerMachine(englishName, internalName, factory);
        MachineRegistrationHelper.addMachineModel(internalName, overlayFolder, controllerCasing, frontOverlay, topOverlay, sideOverlay);
        ReiMachineRecipes.registerMultiblockShape(internalName, multiblockShape);
    }

    public void simpleElectricCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Parameters progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {

        simpleElectricCraftingMultiBlock(englishName, internalName, recipeType, multiblockShape, progressBar,
                itemInputPositions, itemOutputPositions, fluidInputPositions, fluidOutputPositions,
                controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                config -> {
                });
    }

    public void simpleElectricCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Parameters progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // Optional config
            Consumer<ExtraMachineConfig.CraftingMultiBlock> extraConfig) {

        var config = new ExtraMachineConfig.CraftingMultiBlock();
        extraConfig.accept(config);

        simpleCraftingMultiBlock(englishName, internalName, recipeType, multiblockShape, progressBar,
                itemInputPositions, itemOutputPositions, fluidInputPositions, fluidOutputPositions,
                controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                bep -> new ElectricCraftingMultiblockBlockEntity(bep, internalName, multiblockShape, recipeType),
                config.reiConfigs);
    }

    public void simpleSteamCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Parameters progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {

        simpleSteamCraftingMultiBlock(englishName, internalName, recipeType, multiblockShape, progressBar,
                itemInputPositions, itemOutputPositions, fluidInputPositions, fluidOutputPositions,
                controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                config -> {
                });
    }

    public void simpleSteamCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Parameters progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // optional config
            Consumer<ExtraMachineConfig.CraftingMultiBlock> extraConfig) {

        var config = new ExtraMachineConfig.CraftingMultiBlock();
        config.reiConfigs.add(rei -> rei.steam(true));
        extraConfig.accept(config);

        simpleCraftingMultiBlock(englishName, internalName, recipeType, multiblockShape, progressBar,
                itemInputPositions, itemOutputPositions, fluidInputPositions, fluidOutputPositions,
                controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                bep -> new SteamCraftingMultiblockBlockEntity(bep, internalName, multiblockShape, recipeType, config.steamOverclockCatalysts),
                config.reiConfigs);
    }

    private void simpleCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Parameters progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
            // machine entity and rei configuration
            Function<BEP, MachineBlockEntity> factory, List<Consumer<MultiblockMachines.Rei>> reiConfigs) {

        simpleMultiBlock(englishName, internalName, multiblockShape, controllerCasingName, overlayFolder, frontOverlay,
                topOverlay, sideOverlay, factory);

        var rei = new MultiblockMachines.Rei(englishName, internalName, recipeType, progressBar)
                .items(itemInputPositions, itemOutputPositions)
                .fluids(fluidInputPositions, fluidOutputPositions);
        for (var reiConfig : reiConfigs) {
            reiConfig.accept(rei);
        }
        rei.register();
    }

    public void simpleGeneratorSingleBlock(
            // General,
            String englishName, String internalName,
            String cableTierName,
            long maxEnergyProduction,
            long energyCapacity,
            long fluidStorageCapacity,
            Consumer<FluidItemConsumerBuilder> builder,
            String casingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {

        var componentBuilder = new FluidItemConsumerBuilder(maxEnergyProduction);
        builder.accept(componentBuilder);

        MachineRegistrationHelper.registerMachine(
                englishName, internalName,
                bep -> new GeneratorMachineBlockEntity(bep,
                        internalName,
                        CableTier.getTier(cableTierName),
                        energyCapacity,
                        fluidStorageCapacity,
                        componentBuilder.build()),
                MachineBlockEntity::registerItemApi, MachineBlockEntity::registerFluidApi, GeneratorMachineBlockEntity::registerEnergyApi);

        MachineRegistrationHelper.addMachineModel(
                internalName, overlayFolder,
                MachineCasings.get(casingName),
                frontOverlay, topOverlay, sideOverlay);
    }

    public void simpleGeneratorSingleBlock(
            // General,
            String englishName,
            String internalName,
            String cableTierName,
            long maxEnergyProduction,
            long energyCapacity,
            Consumer<FluidItemConsumerBuilder> builder,
            String casingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        simpleGeneratorSingleBlock(
                englishName, internalName,
                cableTierName,
                maxEnergyProduction,
                energyCapacity,
                0,
                builder,
                casingName, overlayFolder, frontOverlay, topOverlay, sideOverlay);
    }

    public void simpleGeneratorMultiBlock(
            // general
            String englishName, String internalName, ShapeTemplate multiblockShape,
            long maxEnergyProduction, Consumer<FluidItemConsumerBuilder> builder,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        var componentBuilder = new FluidItemConsumerBuilder(maxEnergyProduction);
        builder.accept(componentBuilder);
        Function<BEP, MachineBlockEntity> ctor = bep -> new GeneratorMultiblockBlockEntity(bep, internalName, multiblockShape,
                componentBuilder.build());
        simpleMultiBlock(englishName, internalName, multiblockShape, controllerCasingName, overlayFolder, frontOverlay, topOverlay, sideOverlay,
                ctor);
    }

    static class FluidItemConsumerBuilder {

        long maxEnergyProduction;

        FluidItemConsumerComponent.EuProductionMapBuilder<Item> itemEuProductionMapBuilder = new FluidItemConsumerComponent.EuProductionMapBuilder<>(
                BuiltInRegistries.ITEM);

        FluidItemConsumerComponent.EuProductionMapBuilder<Fluid> fluidEuProductionMapBuilder = new FluidItemConsumerComponent.EuProductionMapBuilder<>(
                BuiltInRegistries.FLUID);

        boolean doesAcceptAllFluidFuels = false;
        boolean doesAcceptAllItemFuels = false;

        public FluidItemConsumerBuilder(long maxEnergyProduction) {
            this.maxEnergyProduction = maxEnergyProduction;
        }

        public FluidItemConsumerBuilder fluidFuels() {
            doesAcceptAllFluidFuels = true;
            return this;
        }

        public FluidItemConsumerBuilder furnaceFuels() {
            doesAcceptAllItemFuels = true;
            return this;
        }

        public FluidItemConsumerBuilder fluid(ResourceLocation fluidId, long euPerMb) {
            fluidEuProductionMapBuilder.add(fluidId, euPerMb);
            return this;
        }

        public FluidItemConsumerBuilder item(ResourceLocation itemId, long euPerItem) {
            itemEuProductionMapBuilder.add(itemId, euPerItem);
            return this;
        }

        public FluidItemConsumerComponent build() {
            return new FluidItemConsumerComponent(
                    maxEnergyProduction,
                    doesAcceptAllItemFuels ? FluidItemConsumerComponent.itemFuels() : itemEuProductionMapBuilder.build(),
                    doesAcceptAllFluidFuels ? FluidItemConsumerComponent.fluidFuels() : fluidEuProductionMapBuilder.build());
        }
    }

}

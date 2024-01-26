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
package aztech.modern_industrialization.machines.init;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.compat.rei.machines.SteamMode;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.ElectricCraftingMachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.SteamCraftingMachineBlockEntity;
import aztech.modern_industrialization.machines.components.MachineInventoryComponent;
import aztech.modern_industrialization.machines.components.OverclockComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.RecipeEfficiencyBar;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Registration of all single block crafting machines.
 */
public final class SingleBlockCraftingMachines {
    public static void init() {
        // @formatter:off
        registerMachineTiers(
                "Assembler", "assembler", MIMachineRecipeTypes.ASSEMBLER, 9, 3, 2, 0,
                guiParams -> guiParams.backgroundHeight(186),
                new ProgressBar.Parameters(105, 45, "circuit"), new RecipeEfficiencyBar.Parameters(48, 86), new EnergyBar.Parameters(14, 44),
                items -> items.addSlots(42, 27, 3, 3).addSlots(139, 27, 1, 3), fluids -> fluids.addSlots(98, 27, 2, 1),
                true, true, false,
                TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Centrifuge","centrifuge", MIMachineRecipeTypes.CENTRIFUGE, 1, 4, 1, 4, guiParams -> {},
                new ProgressBar.Parameters(65, 33, "centrifuge"), new RecipeEfficiencyBar.Parameters(50, 66), DEFAULT_ENERGY_BAR,
                items -> items.addSlot(42, 27).addSlots(93, 27, 2, 2), fluids -> fluids.addSlot(42, 45).addSlots(131, 27, 2, 2),
                true, true, true,
                TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Chemical Reactor", "chemical_reactor", MIMachineRecipeTypes.CHEMICAL_REACTOR, 3, 3, 3, 3, guiParams -> {},
                new ProgressBar.Parameters(88, 35, "triple_arrow"), new RecipeEfficiencyBar.Parameters(50, 66), new EnergyBar.Parameters(12, 35),
                items -> items.addSlots(30, 27, 3, 1).addSlots(116, 27, 3, 1), fluids -> fluids.addSlots(30, 47, 3, 1).addSlots(116, 47, 3, 1),
                true, false, false,
                TIER_ELECTRIC, 24
        );
        registerMachineTiers(
                "Compressor", "compressor", MIMachineRecipeTypes.COMPRESSOR, 1, 1, 0, 0, guiParams -> {},
                new ProgressBar.Parameters(77, 34, "compress"), new RecipeEfficiencyBar.Parameters(38, 62), new EnergyBar.Parameters(18, 30),
                items -> items.addSlot(56, 35).addSlot(102, 35), fluids -> {},
                true, true, true,
                TIER_BRONZE | TIER_STEEL | TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Cutting Machine","cutting_machine", MIMachineRecipeTypes.CUTTING_MACHINE, 1, 1, 1, 0, guiParams -> {},
                new ProgressBar.Parameters(88, 31, "slice"), new RecipeEfficiencyBar.Parameters(38, 62), new EnergyBar.Parameters(15, 34),
                items -> items.addSlot(60, 35).addSlot(120, 35), fluids -> fluids.addSlot(40, 35),
                true, false, false,
                TIER_BRONZE | TIER_STEEL | TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Distillery", "distillery", MIMachineRecipeTypes.DISTILLERY, 0, 0, 1, 1, guiParams -> {},
                new ProgressBar.Parameters(77, 33, "arrow"), new RecipeEfficiencyBar.Parameters(38, 62), new EnergyBar.Parameters(18, 30),
                items -> {}, fluids -> fluids.addSlot(56, 35).addSlot(102, 35),
                true, false, false,
                TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Electrolyzer", "electrolyzer", MIMachineRecipeTypes.ELECTROLYZER, 1, 4, 1, 4, guiParams -> {},
                new ProgressBar.Parameters(66, 35, "arrow"), new RecipeEfficiencyBar.Parameters(50, 66), DEFAULT_ENERGY_BAR,
                items -> items.addSlot(42, 27).addSlots(93, 27, 2, 2), fluids -> fluids.addSlot(42, 47).addSlots(131, 27, 2, 2),
                true, false, true,
                TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Furnace", "furnace", MIMachineRecipeTypes.FURNACE, 1, 1, 0, 0, guiParams -> {},
                new ProgressBar.Parameters(77, 33, "arrow"), new RecipeEfficiencyBar.Parameters(38, 62), new EnergyBar.Parameters(18, 30),
                items -> items.addSlot(56, 35).addSlot(102, 35), fluids -> {},
                true, false, false,
                TIER_BRONZE | TIER_STEEL | TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Macerator", "macerator", MIMachineRecipeTypes.MACERATOR, 1, 4, 0, 0, guiParams -> {},
                new ProgressBar.Parameters(77, 33, "macerate"), new RecipeEfficiencyBar.Parameters(38, 66), DEFAULT_ENERGY_BAR,
                items -> items.addSlot(56, 35).addSlots(102, 27, 2, 2), fluids -> {},
                true, true, false,
                TIER_BRONZE | TIER_STEEL | TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Mixer", "mixer", MIMachineRecipeTypes.MIXER, 4, 2, 2, 2, guiParams -> {},
                new ProgressBar.Parameters(103, 33, "arrow"), new RecipeEfficiencyBar.Parameters(50, 66), new EnergyBar.Parameters(15, 34),
                items -> items.addSlots(62, 27, 2, 2).addSlots(129, 27, 1, 2), fluids -> fluids.addSlots(42, 27, 1, 2).addSlots(149, 27, 1, 2),
                true, true, true,
                TIER_BRONZE | TIER_STEEL | TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Packer", "packer", MIMachineRecipeTypes.PACKER, 3, 1, 0, 0, guiParams -> guiParams.backgroundHeight(178),
                new ProgressBar.Parameters(77, 33, "arrow"), new RecipeEfficiencyBar.Parameters(38, 74), new EnergyBar.Parameters(18, 30),
                items -> items.addSlots(56, 18, 1, 3).addSlot(102, 36), fluids -> {},
                true, false, false,
                TIER_STEEL | TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Polarizer", "polarizer", MIMachineRecipeTypes.POLARIZER, 2, 1, 0, 0, guiParams -> {},
                new ProgressBar.Parameters(77, 30, "magnet"), new RecipeEfficiencyBar.Parameters(38, 62), new EnergyBar.Parameters(18, 30),
                items -> items.addSlots(56, 23, 1, 2).addSlot(102, 32), fluids -> {},
                true, true, false,
                TIER_ELECTRIC, 16
        );
        registerMachineTiers(
                "Wiremill", "wiremill", MIMachineRecipeTypes.WIREMILL, 1, 1, 0, 0, guiParams -> {},
                new ProgressBar.Parameters(77, 34, "wiremill"), new RecipeEfficiencyBar.Parameters(38, 62), new EnergyBar.Parameters(18, 30),
                items -> items.addSlot(56, 35).addSlot(102, 35), fluids -> {},
                true, true, false,
                TIER_STEEL | TIER_ELECTRIC, 16
        );

        registerMachineTiers(
                "Unpacker", "unpacker", MIMachineRecipeTypes.UNPACKER, 1, 2, 0, 0, guiParams -> {},
                new ProgressBar.Parameters(77, 33, "arrow"), new RecipeEfficiencyBar.Parameters(38, 66), new EnergyBar.Parameters(18, 30),
                items -> items.addSlots(56, 36, 1, 1).addSlots(102, 27, 1, 2), fluids -> {},
                true, false, false,
                TIER_STEEL | TIER_ELECTRIC, 16
        );
        // @formatter:on
    }

    private static final EnergyBar.Parameters DEFAULT_ENERGY_BAR = new EnergyBar.Parameters(18, 34);

    public static void registerMachineTiers(String englishName, String machine, MachineRecipeType type, int itemInputCount, int itemOutputCount,
            int fluidInputCount,
            int fluidOutputCount, Consumer<MachineGuiParameters.Builder> guiParams, ProgressBar.Parameters progressBarParams,
            RecipeEfficiencyBar.Parameters efficiencyBarParams, EnergyBar.Parameters energyBarParams, Consumer<SlotPositions.Builder> itemPositions,
            Consumer<SlotPositions.Builder> fluidPositions, boolean frontOverlay, boolean topOverlay, boolean sideOverlay, int tiers,
            int ioBucketCapacity) {
        registerMachineTiers(englishName, machine, type, itemInputCount, itemOutputCount, fluidInputCount, fluidOutputCount,
                guiParams, progressBarParams, efficiencyBarParams, energyBarParams, itemPositions, fluidPositions, frontOverlay,
                topOverlay, sideOverlay, tiers, ioBucketCapacity, new Config());
    }

    public static void registerMachineTiers(String englishName, String machine, MachineRecipeType type, int itemInputCount, int itemOutputCount,
            int fluidInputCount,
            int fluidOutputCount, Consumer<MachineGuiParameters.Builder> guiParams, ProgressBar.Parameters progressBarParams,
            RecipeEfficiencyBar.Parameters efficiencyBarParams, EnergyBar.Parameters energyBarParams, Consumer<SlotPositions.Builder> itemPositions,
            Consumer<SlotPositions.Builder> fluidPositions, boolean frontOverlay, boolean topOverlay, boolean sideOverlay, int tiers,
            int ioBucketCapacity, Config extraConfig) {

        for (int i = 0; i < 2; ++i) {
            if (i == 0 && (tiers & TIER_BRONZE) == 0) {
                continue;
            }
            if (i == 1 && (tiers & TIER_STEEL) == 0) {
                continue;
            }

            SlotPositions items = new SlotPositions.Builder().buildWithConsumer(itemPositions);
            SlotPositions fluids = new SlotPositions.Builder().addSlot(12, 35).buildWithConsumer(fluidPositions);
            MachineTier tier = i == 0 ? MachineTier.BRONZE : MachineTier.STEEL;
            String prefix = i == 0 ? "bronze" : "steel";
            String englishPrefix = i == 0 ? "Bronze " : "Steel ";
            int steamBuckets = i == 0 ? 2 : 4;
            MachineGuiParameters.Builder guiParamsBuilder = new MachineGuiParameters.Builder(prefix + "_" + machine, true);
            guiParams.accept(guiParamsBuilder);
            MachineGuiParameters builtGuiParams = guiParamsBuilder.build();
            String id = prefix + "_" + machine;

            MachineRegistrationHelper.registerMachine(englishPrefix + englishName, id,
                    bet -> new SteamCraftingMachineBlockEntity(bet, type,
                            buildComponent(itemInputCount, itemOutputCount, fluidInputCount, fluidOutputCount, items, fluids, steamBuckets,
                                    ioBucketCapacity),
                            builtGuiParams, progressBarParams, tier, extraConfig.steamOverclockCatalysts),
                    bet -> {
                        if (itemInputCount + itemOutputCount > 0) {
                            MachineBlockEntity.registerItemApi(bet);
                        }
                        MachineBlockEntity.registerFluidApi(bet);
                    });
            MachineRegistrationHelper.addMachineModel(prefix, id, machine, frontOverlay, topOverlay, sideOverlay);
        }
        if ((tiers & TIER_ELECTRIC) > 0) {
            SlotPositions items = new SlotPositions.Builder().buildWithConsumer(itemPositions);
            SlotPositions fluids = new SlotPositions.Builder().buildWithConsumer(fluidPositions);

            String id = tiers == TIER_ELECTRIC ? machine : "electric_" + machine;

            MachineGuiParameters.Builder guiParamsBuilder = new MachineGuiParameters.Builder(id, true);
            guiParams.accept(guiParamsBuilder);
            MachineGuiParameters builtGuiParams = guiParamsBuilder.build();

            String electricEnglishName = englishName;

            if ((tiers & TIER_BRONZE) > 0 | (tiers & TIER_STEEL) > 0) {
                electricEnglishName = "Electric " + englishName;
            }

            MachineRegistrationHelper.registerMachine(electricEnglishName, id,
                    bet -> new ElectricCraftingMachineBlockEntity(bet, type,
                            buildComponent(itemInputCount, itemOutputCount, fluidInputCount, fluidOutputCount, items, fluids, 0, ioBucketCapacity),
                            builtGuiParams,
                            energyBarParams, progressBarParams, efficiencyBarParams, MachineTier.LV, 3200),
                    bet -> {
                        ElectricCraftingMachineBlockEntity.registerEnergyApi(bet);
                        if (itemInputCount + itemOutputCount > 0) {
                            MachineBlockEntity.registerItemApi(bet);
                        }
                        if (fluidInputCount + fluidOutputCount > 0) {
                            MachineBlockEntity.registerFluidApi(bet);
                        }
                    });
            MachineRegistrationHelper.addMachineModel("electric", id, machine, frontOverlay, topOverlay, sideOverlay);
        }

        SlotPositions items = new SlotPositions.Builder().buildWithConsumer(itemPositions);
        SlotPositions fluids = new SlotPositions.Builder().buildWithConsumer(fluidPositions);
        registerReiTiers(englishName, machine, type,
                new MachineCategoryParams(null, null, items.sublist(0, itemInputCount),
                        items.sublist(itemInputCount, itemInputCount + itemOutputCount),
                        fluids.sublist(0, fluidInputCount), fluids.sublist(fluidInputCount, fluidInputCount + fluidOutputCount), progressBarParams,
                        null, false, SteamMode.BOTH),
                tiers);
    }

    private static void registerReiTiers(String englishName, String machine, MachineRecipeType recipeType, MachineCategoryParams categoryParams,
            int tiers) {
        List<MachineCategoryParams> previousCategories = new ArrayList<>();
        int previousMaxEu = 0;
        for (int i = 0; i < 3; ++i) {
            if (((tiers >> i) & 1) > 0) {
                int minEu = previousMaxEu + 1;
                int maxEu = i == 0 ? 2 : i == 1 ? 4 : Integer.MAX_VALUE;
                String prefix = i == 0 ? "bronze_" : i == 1 ? "steel_" : tiers == SingleBlockCraftingMachines.TIER_ELECTRIC ? "" : "electric_";
                String itemId = prefix + machine;
                String englishPrefix = i == 0 ? "Bronze " : i == 1 ? "Steel " : "Electric ";
                String fullEnglishName = tiers == SingleBlockCraftingMachines.TIER_ELECTRIC || previousMaxEu == 0 ? englishName
                        : englishPrefix + englishName;
                MachineCategoryParams category = new MachineCategoryParams(fullEnglishName, itemId, categoryParams.itemInputs,
                        categoryParams.itemOutputs,
                        categoryParams.fluidInputs, categoryParams.fluidOutputs, categoryParams.progressBarParams,
                        recipe -> recipe.getType() == recipeType && minEu <= recipe.eu && recipe.eu <= maxEu, false,
                        i < 2 ? SteamMode.BOTH : SteamMode.ELECTRIC_ONLY);
                ReiMachineRecipes.registerCategory(itemId, category);
                ReiMachineRecipes.registerMachineClickArea(itemId, categoryParams.progressBarParams.toRectangle());
                previousCategories.add(category);
                for (MachineCategoryParams param : previousCategories) {
                    param.workstations.add(new MIIdentifier(itemId));
                    ReiMachineRecipes.registerRecipeCategoryForMachine(itemId, param.category);
                }
                previousMaxEu = maxEu;
            }
        }
    }

    public static final int TIER_BRONZE = 1;
    public static final int TIER_STEEL = 2;
    public static final int TIER_ELECTRIC = 4;

    /**
     * @param steamBuckets Number of steam buckets in the steam input slot, or 0 for
     *                     no steam input slot
     */
    private static MachineInventoryComponent buildComponent(int itemInputCount, int itemOutputCount, int fluidInputCount, int fluidOutputCount,
            SlotPositions itemPositions, SlotPositions fluidPositions, int steamBuckets, int ioBucketCapacity) {

        List<ConfigurableItemStack> itemInputStacks = new ArrayList<>();
        for (int i = 0; i < itemInputCount; ++i) {
            itemInputStacks.add(ConfigurableItemStack.standardInputSlot());
        }
        List<ConfigurableItemStack> itemOutputStacks = new ArrayList<>();
        for (int i = 0; i < itemOutputCount; ++i) {
            itemOutputStacks.add(ConfigurableItemStack.standardOutputSlot());
        }
        List<ConfigurableFluidStack> fluidInputStacks = new ArrayList<>();
        if (steamBuckets > 0) {
            fluidInputStacks.add(ConfigurableFluidStack.lockedInputSlot(1000L * steamBuckets, MIFluids.STEAM.asFluid()));
        }
        for (int i = 0; i < fluidInputCount; ++i) {
            fluidInputStacks.add(ConfigurableFluidStack.standardInputSlot(1000L * ioBucketCapacity));
        }
        List<ConfigurableFluidStack> fluidOutputStacks = new ArrayList<>();
        for (int i = 0; i < fluidOutputCount; ++i) {
            fluidOutputStacks.add(ConfigurableFluidStack.standardOutputSlot(1000L * ioBucketCapacity));
        }

        return new MachineInventoryComponent(itemInputStacks, itemOutputStacks, fluidInputStacks, fluidOutputStacks, itemPositions, fluidPositions);
    }

    private SingleBlockCraftingMachines() {
    }

    public static class Config {
        public List<OverclockComponent.Catalyst> steamOverclockCatalysts = OverclockComponent.getDefaultCatalysts();
    }
}

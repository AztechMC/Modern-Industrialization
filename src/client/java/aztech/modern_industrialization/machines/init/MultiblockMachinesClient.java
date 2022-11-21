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

import static aztech.modern_industrialization.machines.models.MachineCasings.CLEAN_STAINLESS_STEEL;

import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.compat.rei.machines.SteamMode;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGui;
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGuiClient;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MultiblockMachinesClient {
    public static void clientInit() {
        MachineRegistrationHelper.addMachineModel("coke_oven", "coke_oven", MachineCasings.BRICKS, true, false, false);
        new Rei("Coke Oven", "coke_oven", MIMachineRecipeTypes.COKE_OVEN, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlot(102, 35))
                .fluids(inputs -> {
                }, outputs -> outputs.addSlot(102, 53))
                .steam(true)
                .register();

        MachineRegistrationHelper.addMachineModel("steam_blast_furnace", "steam_blast_furnace", MachineCasings.FIREBRICKS, true, false, false);
        new Rei("Steam Blast Furnace", "steam_blast_furnace", MIMachineRecipeTypes.BLAST_FURNACE, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlots(56, 35, 2, 1), outputs -> outputs.addSlots(102, 35, 1, 1))
                .fluids(fluids -> fluids.addSlots(36, 35, 1, 1), outputs -> outputs.addSlots(122, 35, 1, 1))
                .workstations("steam_blast_furnace", "electric_blast_furnace").extraTest(recipe -> recipe.eu <= 4)
                .steam(false)
                .register();

        MachineRegistrationHelper.addMachineModel("electric_blast_furnace", "electric_blast_furnace", MachineCasings.HEATPROOF, true, false, false);
        // note: the REI category is built in the static {} block of ElectricBlastFurnaceBlockEntity.java

        MachineRegistrationHelper.addMachineModel("large_steam_boiler", "large_boiler", MachineCasings.BRONZE_PLATED_BRICKS, true, false, false);

        MachineRegistrationHelper.addMachineModel("advanced_large_steam_boiler", "large_boiler", MachineCasings.BRONZE_PLATED_BRICKS, true, false,
                false);

        MachineRegistrationHelper.addMachineModel("high_pressure_large_steam_boiler", "large_boiler", CLEAN_STAINLESS_STEEL, true, false, false);

        MachineRegistrationHelper.addMachineModel("high_pressure_advanced_large_steam_boiler", "large_boiler", CLEAN_STAINLESS_STEEL, true, false,
                false);

        MachineRegistrationHelper.addMachineModel("steam_quarry", "quarry", MachineCasings.STEEL, true, false, false);
        new Rei("Steam Quarry", "steam_quarry", MIMachineRecipeTypes.QUARRY, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlots(102, 35, 4, 4))
                .workstations("steam_quarry", "electric_quarry").extraTest(recipe -> recipe.eu <= 4)
                .steam(false)
                .register();
        new Rei("Electric Quarry", "electric_quarry", MIMachineRecipeTypes.QUARRY, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlots(102, 35, 4, 4))
                .workstations("electric_quarry").extraTest(recipe -> recipe.eu > 4)
                .register();

        MachineRegistrationHelper.addMachineModel("electric_quarry", "quarry", MachineCasings.STEEL, true, false, false);

        MachineRegistrationHelper.addMachineModel("vacuum_freezer", "vacuum_freezer", MachineCasings.FROSTPROOF, true, false, false);
        new Rei("Vacuum Freezer", "vacuum_freezer", MIMachineRecipeTypes.VACUUM_FREEZER, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlots(56, 35, 2, 1), outputs -> outputs.addSlot(102, 35))
                .fluids(inputs -> inputs.addSlots(36, 35, 2, 1), outputs -> outputs.addSlot(122, 35))
                .register();

        MachineRegistrationHelper.addMachineModel("oil_drilling_rig", "oil_drilling_rig", MachineCasings.STEEL, true, false, false);
        new Rei("Oil Drilling Rig", "oil_drilling_rig", MIMachineRecipeTypes.OIL_DRILLING_RIG, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlot(36, 35), outputs -> {
                })
                .fluids(inputs -> {
                }, outputs -> outputs.addSlot(122, 35))
                .register();

        MachineRegistrationHelper.addMachineModel("distillation_tower", "distillation_tower", CLEAN_STAINLESS_STEEL, true, false, false);
        new Rei("Distillation Tower", "distillation_tower", MIMachineRecipeTypes.DISTILLATION_TOWER, new ProgressBar.Parameters(77, 33, "arrow"))
                .fluids(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlots(102, 35, 1, 8))
                .register();

        MachineRegistrationHelper.addMachineModel("large_diesel_generator", "diesel_generator", MachineCasings.SOLID_TITANIUM, true, false, false);

        MachineRegistrationHelper.addMachineModel("large_steam_turbine", "steam_turbine", CLEAN_STAINLESS_STEEL, true, false, false);

        MachineRegistrationHelper.addMachineModel("heat_exchanger", "heat_exchanger", MachineCasings.STAINLESS_STEEL_PIPE, true, false, false);
        new Rei("Heat Exchanger", "heat_exchanger", MIMachineRecipeTypes.HEAT_EXCHANGER, new ProgressBar.Parameters(77, 42, "arrow"))
                .items(inputs -> inputs.addSlot(36, 35), outputs -> outputs.addSlot(122, 35))
                .fluids(inputs -> inputs.addSlots(56, 35, 2, 1), outputs -> outputs.addSlots(102, 35, 2, 1))
                .register();

        MachineRegistrationHelper.addMachineModel("pressurizer", "pressurizer", MachineCasings.TITANIUM_PIPE, true, false, false);
        new Rei("Pressurizer", "pressurizer", MIMachineRecipeTypes.PRESSURIZER, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlot(38, 35), outputs -> {
                })
                .fluids(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlot(102, 35))
                .register();

        MachineRegistrationHelper.addMachineModel("implosion_compressor", "compressor", MachineCasings.SOLID_TITANIUM, true, false, false);
        new Rei("Implosion Compressor", "implosion_compressor", MIMachineRecipeTypes.IMPLOSION_COMPRESSOR,
                new ProgressBar.Parameters(77, 42, "compress"))
                        .items(inputs -> inputs.addSlots(36, 35, 2, 2), outputs -> outputs.addSlot(102, 42))
                        .register();

        MachineRegistrationHelper.addMachineModel("nuclear_reactor", "nuclear_reactor", MachineCasings.NUCLEAR, true, false, false, true);

        MachineRegistrationHelper.addMachineModel("large_tank",
                "large_tank", MachineCasings.STEEL, true, false, false, false);

        MachineRegistrationHelper.addMachineModel("fusion_reactor",
                "fusion_reactor", MachineCasings.EV, true, false, false, true);
        new Rei("Fusion Reactor", "fusion_reactor", MIMachineRecipeTypes.FUSION_REACTOR, new ProgressBar.Parameters(66, 33, "arrow"))
                .fluids(inputs -> inputs.addSlots(26, 35, 1, 2), outputs -> outputs.addSlots(92, 35, 1, 3))
                .register();

        MachineRegistrationHelper.addMachineModel("plasma_turbine", "steam_turbine",
                MachineCasings.PLASMA_HANDLING_IRIDIUM, true, false, false);

        registerEbfReiCategories();
    }

    private static void registerEbfReiCategories() {
        // Register REI categories
        for (int i = 0; i < ElectricBlastFurnaceBlockEntity.coils.size(); ++i) {
            long previousMax = i == 0 ? 4 : ElectricBlastFurnaceBlockEntity.coilsMaxBaseEU.get(ElectricBlastFurnaceBlockEntity.coils.get(i - 1));
            long currentMax = ElectricBlastFurnaceBlockEntity.coilsMaxBaseEU.get(ElectricBlastFurnaceBlockEntity.coils.get(i));
            List<String> workstations = new ArrayList<>();
            workstations.add("electric_blast_furnace");
            for (int j = i; j < ElectricBlastFurnaceBlockEntity.coils.size(); ++j) {
                workstations.add(ElectricBlastFurnaceBlockEntity.coilNames.get(j));
            }

            new Rei("EBF" + ElectricBlastFurnaceBlockEntity.coilEnglishNames.get(i), "electric_blast_furnace_" + i,
                    MIMachineRecipeTypes.BLAST_FURNACE,
                    new ProgressBar.Parameters(77, 33, "arrow"))
                            .items(inputs -> inputs.addSlots(56, 35, 2, 1), outputs -> outputs.addSlot(102, 35))
                            .fluids(fluids -> fluids.addSlot(36, 35), outputs -> outputs.addSlot(122, 35))
                            .extraTest(recipe -> previousMax < recipe.eu && recipe.eu <= currentMax)
                            .workstations(workstations.toArray(new String[0])).register();
        }
    }

    private static final Rectangle CRAFTING_GUI = new Rectangle(CraftingMultiblockGui.X, CraftingMultiblockGui.Y,
            CraftingMultiblockGui.W, CraftingMultiblockGui.H);

    public static class Rei {
        private final String englishName;
        private final String category;
        private final MachineRecipeType recipeType;
        private final ProgressBar.Parameters progressBarParams;
        private final List<String> workstations;
        private Predicate<MachineRecipe> extraTest = recipe -> true;
        private SlotPositions itemInputs = SlotPositions.empty();
        private SlotPositions itemOutputs = SlotPositions.empty();
        private SlotPositions fluidInputs = SlotPositions.empty();
        private SlotPositions fluidOutputs = SlotPositions.empty();
        private SteamMode steamMode = SteamMode.ELECTRIC_ONLY;
        private static final Predicate<MachineScreen> SHAPE_VALID_PREDICATE = screen -> {
            for (GuiComponentClient client : screen.getMenu().components) {
                if (client instanceof CraftingMultiblockGuiClient cmGui) {
                    if (cmGui.isShapeValid) {
                        return true;
                    }
                }
            }
            return false;
        };

        public Rei(String englishName, String category, MachineRecipeType recipeType, ProgressBar.Parameters progressBarParams) {
            this.englishName = englishName;
            this.category = category;
            this.recipeType = recipeType;
            this.progressBarParams = progressBarParams;
            this.workstations = new ArrayList<>();
            workstations.add(category);
        }

        public Rei items(Consumer<SlotPositions.Builder> inputs, Consumer<SlotPositions.Builder> outputs) {
            itemInputs = new SlotPositions.Builder().buildWithConsumer(inputs);
            itemOutputs = new SlotPositions.Builder().buildWithConsumer(outputs);
            return this;
        }

        public Rei fluids(Consumer<SlotPositions.Builder> inputs, Consumer<SlotPositions.Builder> outputs) {
            fluidInputs = new SlotPositions.Builder().buildWithConsumer(inputs);
            fluidOutputs = new SlotPositions.Builder().buildWithConsumer(outputs);
            return this;
        }

        public Rei extraTest(Predicate<MachineRecipe> extraTest) {
            this.extraTest = extraTest;
            return this;
        }

        public Rei workstations(String... workstations) {
            this.workstations.clear();
            this.workstations.addAll(Arrays.asList(workstations));
            return this;
        }

        public Rei steam(boolean steamOnly) {
            this.steamMode = steamOnly ? SteamMode.STEAM_ONLY : SteamMode.BOTH;
            return this;
        }

        public final void register() {
            ReiMachineRecipes.registerCategory(category, new MachineCategoryParams(englishName, category, itemInputs, itemOutputs, fluidInputs,
                    fluidOutputs, progressBarParams, recipe -> recipe.getType() == recipeType && extraTest.test(recipe), true, steamMode));
            for (String workstation : workstations) {
                ReiMachineRecipes.registerWorkstation(category, workstation);
                ReiMachineRecipes.registerRecipeCategoryForMachine(workstation, category, SHAPE_VALID_PREDICATE);
                ReiMachineRecipes.registerMachineClickArea(workstation, CRAFTING_GUI);
            }
        }
    }
}

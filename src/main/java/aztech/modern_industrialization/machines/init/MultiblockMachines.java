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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.compat.rei.machines.SteamMode;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.blockentities.multiblocks.*;
import aztech.modern_industrialization.machines.components.FluidItemConsumerComponent;
import aztech.modern_industrialization.machines.components.OverclockComponent;
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGui;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class MultiblockMachines {
    private static final Rectangle CRAFTING_GUI = new Rectangle(CraftingMultiblockGui.X, CraftingMultiblockGui.Y,
            CraftingMultiblockGui.W, CraftingMultiblockGui.H);
    // @formatter:off
    public static Supplier<BlockEntityType<?>> COKE_OVEN;
    public static Supplier<BlockEntityType<?>> STEAM_BLAST_FURNACE;
    public static Supplier<BlockEntityType<?>> STEAM_QUARRY;
    public static Supplier<BlockEntityType<?>> ELECTRIC_BLAST_FURNACE;
    public static Supplier<BlockEntityType<?>> LARGE_STEAM_BOILER;
    public static Supplier<BlockEntityType<?>> ADVANCED_LARGE_STEAM_BOILER;
    public static Supplier<BlockEntityType<?>> HIGH_PRESSURE_LARGE_STEAM_BOILER;
    public static Supplier<BlockEntityType<?>> HIGH_PRESSURE_ADVANCED_LARGE_STEAM_BOILER;
    public static Supplier<BlockEntityType<?>> ELECTRIC_QUARRY;
    public static Supplier<BlockEntityType<?>> OIL_DRILLING_RIG;
    public static Supplier<BlockEntityType<?>> VACUUM_FREEZER;
    public static Supplier<BlockEntityType<?>> DISTILLATION_TOWER;
    public static Supplier<BlockEntityType<?>> LARGE_DIESEL_GENERATOR;
    public static Supplier<BlockEntityType<?>> LARGE_STEAM_TURBINE;
    public static Supplier<BlockEntityType<?>> HEAT_EXCHANGER;
    public static Supplier<BlockEntityType<?>> PRESSURIZER;
    public static Supplier<BlockEntityType<?>> IMPLOSION_COMPRESSOR;
    public static Supplier<BlockEntityType<?>> NUCLEAR_REACTOR;
    public static Supplier<BlockEntityType<?>> LARGE_TANK;
    public static Supplier<BlockEntityType<?>> FUSION_REACTOR;
    public static Supplier<BlockEntityType<?>> PLASMA_TURBINE;

    private static void cokeOven() {
        ShapeTemplate cokeOvenShape = new ShapeTemplate.Structure(MI.id("coke_oven")).build();
        COKE_OVEN = MachineRegistrationHelper.registerMachine("Coke Oven", "coke_oven",
                bet -> new SteamCraftingMultiblockBlockEntity(bet, "coke_oven", cokeOvenShape, MIMachineRecipeTypes.COKE_OVEN, OverclockComponent.getDefaultCatalysts()));
        ReiMachineRecipes.registerMultiblockShape("coke_oven", cokeOvenShape);
    }

    private static void steamBlastFurnace() {
        ShapeTemplate sbfShape = new ShapeTemplate.Structure(MI.id("steam_blast_furnace")).build();
        STEAM_BLAST_FURNACE = MachineRegistrationHelper.registerMachine("Steam Blast Furnace", "steam_blast_furnace",
                bet -> new SteamCraftingMultiblockBlockEntity(bet, "steam_blast_furnace", sbfShape, MIMachineRecipeTypes.BLAST_FURNACE, OverclockComponent.getDefaultCatalysts()));
        ReiMachineRecipes.registerMultiblockShape("steam_blast_furnace", sbfShape);
    }

    private static void electricBlastFurnace() {
        ELECTRIC_BLAST_FURNACE = MachineRegistrationHelper.registerMachine("Electric Blast Furnace", "electric_blast_furnace",
                ElectricBlastFurnaceBlockEntity::new);
        ElectricBlastFurnaceBlockEntity.registerReiShapes();
    }

    private static void steamBoilers() {
        ShapeTemplate largeSteamBoilerShape = new ShapeTemplate.Structure(MI.id("large_steam_boiler")).build();
        LARGE_STEAM_BOILER = MachineRegistrationHelper.registerMachine("Large Steam Boiler", "large_steam_boiler",
                bet -> new SteamBoilerMultiblockBlockEntity(bet, largeSteamBoilerShape, "large_steam_boiler",
                        256, false));
        ReiMachineRecipes.registerMultiblockShape("large_steam_boiler", largeSteamBoilerShape);

        ShapeTemplate advancedLargeSteamBoilerShape = new ShapeTemplate.Structure(MI.id("advanced_large_steam_boiler")).build();
        ADVANCED_LARGE_STEAM_BOILER = MachineRegistrationHelper.registerMachine("Advanced Large Steam Boiler", "advanced_large_steam_boiler",
                bet -> new SteamBoilerMultiblockBlockEntity(bet, advancedLargeSteamBoilerShape, "advanced_large_steam_boiler",
                        1024, false));
        ReiMachineRecipes.registerMultiblockShape("advanced_large_steam_boiler", advancedLargeSteamBoilerShape);


        ShapeTemplate highPressureLargeSteamBoilerShape = new ShapeTemplate.Structure(MI.id("high_pressure_large_steam_boiler")).build();
        HIGH_PRESSURE_LARGE_STEAM_BOILER = MachineRegistrationHelper.registerMachine(
                "High Pressure Large Steam Boiler",
                "high_pressure_large_steam_boiler",
                bet -> new SteamBoilerMultiblockBlockEntity(bet, highPressureLargeSteamBoilerShape, "high_pressure_large_steam_boiler",
                        2048, true));
        ReiMachineRecipes.registerMultiblockShape("high_pressure_large_steam_boiler", highPressureLargeSteamBoilerShape);

        ShapeTemplate highPressureAdvancedLargeSteamBoilerShape = new ShapeTemplate.Structure(MI.id("high_pressure_advanced_large_steam_boiler")).build();
        HIGH_PRESSURE_ADVANCED_LARGE_STEAM_BOILER = MachineRegistrationHelper.registerMachine(
                "High Pressure Advanced Large Steam Boiler",
                "high_pressure_advanced_large_steam_boiler",
                bet -> new SteamBoilerMultiblockBlockEntity(bet, highPressureAdvancedLargeSteamBoilerShape, "high_pressure_advanced_large_steam_boiler",
                        8192, true));
        ReiMachineRecipes.registerMultiblockShape("high_pressure_advanced_large_steam_boiler", highPressureAdvancedLargeSteamBoilerShape);
    }

    private static void quarries() {
        ShapeTemplate quarryShape = new ShapeTemplate.Structure(MI.id("steam_quarry")).build();
        STEAM_QUARRY = MachineRegistrationHelper.registerMachine(
                "Steam Quarry",
                "steam_quarry",
                bet -> new SteamCraftingMultiblockBlockEntity(bet, "steam_quarry", quarryShape, MIMachineRecipeTypes.QUARRY, OverclockComponent.getDefaultCatalysts()));
        ReiMachineRecipes.registerMultiblockShape("steam_quarry", quarryShape);

        ShapeTemplate quarryElectricShape = new ShapeTemplate.Structure(MI.id("electric_quarry")).build();
        ELECTRIC_QUARRY = MachineRegistrationHelper.registerMachine(
                "Electric Quarry",
                "electric_quarry",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "electric_quarry", quarryElectricShape, MIMachineRecipeTypes.QUARRY));
        ReiMachineRecipes.registerMultiblockShape("electric_quarry", quarryElectricShape);
    }

    private static void oilDrillingRig() {
        ShapeTemplate oilDrillingRigShape = new ShapeTemplate.Structure(MI.id("oil_drilling_rig")).build();
        OIL_DRILLING_RIG = MachineRegistrationHelper.registerMachine(
                "Oil Drilling Rig",
                "oil_drilling_rig", bet -> new ElectricCraftingMultiblockBlockEntity(bet,
                        "oil_drilling_rig", oilDrillingRigShape, MIMachineRecipeTypes.OIL_DRILLING_RIG));
        ReiMachineRecipes.registerMultiblockShape("oil_drilling_rig", oilDrillingRigShape);
    }

    private static void vacuumFreezer() {
        ShapeTemplate vacuumFreezerShape = new ShapeTemplate.Structure(MI.id("vacuum_freezer")).build();
        VACUUM_FREEZER = MachineRegistrationHelper.registerMachine(
                "Vacuum Freezer",
                "vacuum_freezer",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "vacuum_freezer", vacuumFreezerShape, MIMachineRecipeTypes.VACUUM_FREEZER));
        ReiMachineRecipes.registerMultiblockShape("vacuum_freezer", vacuumFreezerShape);
    }

    private static void distillationTower() {
        DISTILLATION_TOWER = MachineRegistrationHelper.registerMachine(
                "Distillation Tower",
                "distillation_tower", DistillationTowerBlockEntity::new);
        DistillationTowerBlockEntity.registerReiShapes();
    }

    private static void largeDieselGenerator() {
        ShapeTemplate largeDieselGeneratorShape = new ShapeTemplate.Structure(MI.id("large_diesel_generator")).build();
        LARGE_DIESEL_GENERATOR = MachineRegistrationHelper.registerMachine(
                "Large Diesel Generator",
                "large_diesel_generator", bet ->
                        new GeneratorMultiblockBlockEntity(bet, "large_diesel_generator",
                                largeDieselGeneratorShape,
                                FluidItemConsumerComponent.ofFluidFuels(16384)));
        ReiMachineRecipes.registerMultiblockShape("large_diesel_generator", largeDieselGeneratorShape);
    }

    private static void largeSteamTurbine() {
        ShapeTemplate largeSteamTurbineShape = new ShapeTemplate.Structure(MI.id("large_steam_turbine")).build();
        LARGE_STEAM_TURBINE = MachineRegistrationHelper.registerMachine(
                "Large Steam Turbine",
                "large_steam_turbine", bet ->
                        new GeneratorMultiblockBlockEntity(bet, "large_steam_turbine", largeSteamTurbineShape,
                                FluidItemConsumerComponent.ofFluid(16384,
                                        new FluidItemConsumerComponent.EuProductionMapBuilder<>(BuiltInRegistries.FLUID)
                                                .add(MIFluids.STEAM.getId(), 1)
                                                .add(MIFluids.HIGH_PRESSURE_STEAM.getId(), 8)
                                                .add(MIFluids.HEAVY_WATER_STEAM.getId(), 1)
                                                .add(MIFluids.HIGH_PRESSURE_HEAVY_WATER_STEAM.getId(), 8)
                                                .build()
                                )
                                ));
        ReiMachineRecipes.registerMultiblockShape("large_steam_turbine", largeSteamTurbineShape);
    }

    private static void heatExchanger() {
        ShapeTemplate heatExchangerShape = new ShapeTemplate.Structure(MI.id("heat_exchanger")).build();
        HEAT_EXCHANGER = MachineRegistrationHelper.registerMachine(
                "Heat Exchanger",
                "heat_exchanger",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "heat_exchanger", heatExchangerShape, MIMachineRecipeTypes.HEAT_EXCHANGER));
        ReiMachineRecipes.registerMultiblockShape("heat_exchanger", heatExchangerShape);

    }

    private static void pressurizer() {
        ShapeTemplate pressurizerShape = new ShapeTemplate.Structure(MI.id("pressurizer")).build();
        PRESSURIZER = MachineRegistrationHelper.registerMachine(
                "Pressurizer",
                "pressurizer",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "pressurizer", pressurizerShape, MIMachineRecipeTypes.PRESSURIZER));
        ReiMachineRecipes.registerMultiblockShape("pressurizer", pressurizerShape);
    }

    private static void implosionCompressor() {
        ShapeTemplate implosionCompressorShape = new ShapeTemplate.Structure(MI.id("implosion_compressor")).build();
        IMPLOSION_COMPRESSOR = MachineRegistrationHelper.registerMachine(
                "Implosion Compressor",
                "implosion_compressor",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "implosion_compressor", implosionCompressorShape, MIMachineRecipeTypes.IMPLOSION_COMPRESSOR));
        ReiMachineRecipes.registerMultiblockShape("implosion_compressor", implosionCompressorShape);
    }

    private static void nuclearReactor() {
        NUCLEAR_REACTOR = MachineRegistrationHelper.registerMachine(
                "Nuclear Reactor",
                "nuclear_reactor", NuclearReactorMultiblockBlockEntity::new);
        NuclearReactorMultiblockBlockEntity.registerReiShapes();
    }

    private static void largeTank() {
        LARGE_TANK = MachineRegistrationHelper.registerMachine(
                "Large Tank",
                "large_tank", LargeTankMultiblockBlockEntity::new, LargeTankMultiblockBlockEntity::registerFluidAPI);
    }

    private static void fusionReactor() {
        ShapeTemplate fusionReactorShape = new ShapeTemplate.Structure(MI.id("fusion_reactor")).build();
        FUSION_REACTOR = MachineRegistrationHelper.registerMachine(
                "Fusion Reactor",
                "fusion_reactor",
                bet -> new FusionReactorBlockEntity(bet, "fusion_reactor",
                        fusionReactorShape));
        ReiMachineRecipes.registerMultiblockShape("fusion_reactor", fusionReactorShape);

    }

    private static void plasmaTurbine() {
        ShapeTemplate plasmaTurbineShape = new ShapeTemplate.Structure(MI.id("plasma_turbine")).build();
        PLASMA_TURBINE = MachineRegistrationHelper.registerMachine(
                "Plasma Turbine",
                "plasma_turbine", bet ->
                        new GeneratorMultiblockBlockEntity(bet, "plasma_turbine", plasmaTurbineShape,
                                FluidItemConsumerComponent.ofSingleFluid(
                                        1 << 20,
                                        MIFluids.HELIUM_PLASMA,
                                        100000
                                )));
        ReiMachineRecipes.registerMultiblockShape("plasma_turbine", plasmaTurbineShape);
    }

    public static void init() {
        cokeOven();
        steamBlastFurnace();
        electricBlastFurnace();
        steamBoilers();
        quarries();
        oilDrillingRig();
        vacuumFreezer();
        distillationTower();
        largeDieselGenerator();
        largeSteamTurbine();
        heatExchanger();
        pressurizer();
        implosionCompressor();
        nuclearReactor();
        largeTank();
        fusionReactor();
        plasmaTurbine();

        clientInit();
    }

    private static void clientInit() {
        MachineRegistrationHelper.addMachineModel("coke_oven", "coke_oven", MachineCasings.BRICKS, true, false, false);
        new Rei("Coke Oven", "coke_oven", MIMachineRecipeTypes.COKE_OVEN, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlot(102, 35))
                .fluids(inputs -> {
                }, outputs -> outputs.addSlot(102, 53))
                .steam(true)
                .register();

        MachineRegistrationHelper.addMachineModel("steam_blast_furnace", "steam_blast_furnace", MachineCasings.FIREBRICKS, true, false, false);
        new Rei("Steam Blast Furnace", "steam_blast_furnace", MIMachineRecipeTypes.BLAST_FURNACE, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlots(56, 35, 1, 2), outputs -> outputs.addSlots(102, 35, 1, 1))
                .fluids(fluids -> fluids.addSlots(36, 35, 1, 1), outputs -> outputs.addSlots(122, 35, 1, 1))
                .workstations("steam_blast_furnace", "electric_blast_furnace").extraTest(recipe -> recipe.eu <= 4)
                .steam(false)
                .register();

        MachineRegistrationHelper.addMachineModel("electric_blast_furnace", "electric_blast_furnace", MachineCasings.HEATPROOF, true, false, false);

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
                .items(inputs -> inputs.addSlots(56, 35, 1, 2), outputs -> outputs.addSlot(102, 35))
                .fluids(inputs -> inputs.addSlots(36, 35, 1, 2), outputs -> outputs.addSlot(122, 35))
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
                .fluids(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlots(102, 35, 8, 1))
                .register();

        MachineRegistrationHelper.addMachineModel("large_diesel_generator", "diesel_generator", MachineCasings.SOLID_TITANIUM, true, false, false);

        MachineRegistrationHelper.addMachineModel("large_steam_turbine", "steam_turbine", CLEAN_STAINLESS_STEEL, true, false, false);

        MachineRegistrationHelper.addMachineModel("heat_exchanger", "heat_exchanger", MachineCasings.STAINLESS_STEEL_PIPE, true, false, false);
        new Rei("Heat Exchanger", "heat_exchanger", MIMachineRecipeTypes.HEAT_EXCHANGER, new ProgressBar.Parameters(77, 42, "arrow"))
                .items(inputs -> inputs.addSlot(36, 35), outputs -> outputs.addSlot(122, 35))
                .fluids(inputs -> inputs.addSlots(56, 35, 1, 2), outputs -> outputs.addSlots(102, 35, 1, 2))
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
                "fusion_reactor", CableTier.EV.casing, true, false, false, true);
        new Rei("Fusion Reactor", "fusion_reactor", MIMachineRecipeTypes.FUSION_REACTOR, new ProgressBar.Parameters(66, 33, "arrow"))
                .fluids(inputs -> inputs.addSlots(26, 35, 2, 1), outputs -> outputs.addSlots(92, 35, 3, 1))
                .register();

        MachineRegistrationHelper.addMachineModel("plasma_turbine", "steam_turbine",
                MachineCasings.PLASMA_HANDLING_IRIDIUM, true, false, false);

        registerEbfReiCategories();
    }

    private static void registerEbfReiCategories() {
        // Register REI categories
        for (int i = 0; i < ElectricBlastFurnaceBlockEntity.tiers.size(); ++i) {
            var tier = ElectricBlastFurnaceBlockEntity.tiers.get(i);
            long previousMax = i == 0 ? 4 : ElectricBlastFurnaceBlockEntity.tiers.get(i - 1).maxBaseEu();
            long currentMax = tier.maxBaseEu();

            var extraWorkstations = IntStream.range(i, ElectricBlastFurnaceBlockEntity.tiers.size())
                    .mapToObj(j -> ElectricBlastFurnaceBlockEntity.tiers.get(j).coilBlockId())
                    .toArray(ResourceLocation[]::new);

            new Rei("EBF (%s Tier)".formatted(tier.englishName()), "electric_blast_furnace_" + tier.coilBlockId().getPath(),
                    MIMachineRecipeTypes.BLAST_FURNACE,
                    new ProgressBar.Parameters(77, 33, "arrow"))
                            .items(inputs -> inputs.addSlots(56, 35, 1, 2), outputs -> outputs.addSlot(102, 35))
                            .fluids(fluids -> fluids.addSlot(36, 35), outputs -> outputs.addSlot(122, 35))
                            .extraTest(recipe -> previousMax < recipe.eu && recipe.eu <= currentMax)
                            .workstations("electric_blast_furnace")
                            .extraWorkstations(extraWorkstations)
                            .register();
        }
    }

    public static class Rei {
        private final String englishName;
        private final ResourceLocation category;
        private final MachineRecipeType recipeType;
        private final ProgressBar.Parameters progressBarParams;
        private final List<ResourceLocation> workstations;
        // extra workstations to be displayed in viewers, can be any item id
        private final List<ResourceLocation> extraWorkstations;
        private Predicate<MachineRecipe> extraTest = recipe -> true;
        private final SlotPositions.Builder itemInputs = new SlotPositions.Builder();
        private final SlotPositions.Builder itemOutputs = new SlotPositions.Builder();
        private final SlotPositions.Builder fluidInputs = new SlotPositions.Builder();
        private final SlotPositions.Builder fluidOutputs = new SlotPositions.Builder();
        private SteamMode steamMode = SteamMode.ELECTRIC_ONLY;

        public Rei(String englishName, ResourceLocation category, MachineRecipeType recipeType, ProgressBar.Parameters progressBarParams) {
            this.englishName = englishName;
            this.category = category;
            this.recipeType = recipeType;
            this.progressBarParams = progressBarParams;
            this.workstations = new ArrayList<>();
            this.extraWorkstations = new ArrayList<>();
            workstations.add(category);
        }
        
        public Rei(String englishName, String category, MachineRecipeType recipeType, ProgressBar.Parameters progressBarParams) {
            this(englishName, MI.id(category), recipeType, progressBarParams);
        }

        public Rei items(Consumer<SlotPositions.Builder> inputs, Consumer<SlotPositions.Builder> outputs) {
            inputs.accept(itemInputs);
            outputs.accept(itemOutputs);
            return this;
        }

        public Rei fluids(Consumer<SlotPositions.Builder> inputs, Consumer<SlotPositions.Builder> outputs) {
            inputs.accept(fluidInputs);
            outputs.accept(fluidOutputs);
            return this;
        }

        public Rei extraTest(Predicate<MachineRecipe> extraTest) {
            this.extraTest = extraTest;
            return this;
        }
        
        public Rei workstations(ResourceLocation... workstations) {
            this.workstations.clear();
            this.workstations.addAll(Arrays.asList(workstations));
            return this;
        }
        
        public Rei workstations(String... workstations) {
            return workstations(Arrays.stream(workstations).map(MI::id).toList().toArray(new ResourceLocation[0]));
        }

        public Rei extraWorkstations(ResourceLocation... extraWorkstations) {
            this.extraWorkstations.clear();
            this.extraWorkstations.addAll(Arrays.asList(extraWorkstations));
            return this;
        }

        public Rei steam(boolean steamOnly) {
            this.steamMode = steamOnly ? SteamMode.STEAM_ONLY : SteamMode.BOTH;
            return this;
        }

        public final void register() {
            // Allow KJS scripts to add slots
            KubeJSProxy.instance.fireAddMultiblockSlotsEvent(category.getPath(), itemInputs, itemOutputs, fluidInputs, fluidOutputs);

            ReiMachineRecipes.registerCategory(category, new MachineCategoryParams(englishName, category,
                    itemInputs.build(), itemOutputs.build(), fluidInputs.build(), fluidOutputs.build(),
                    progressBarParams, recipeType, extraTest, true, steamMode));
            for (ResourceLocation workstation : workstations) {
                ReiMachineRecipes.registerWorkstation(category, workstation);
                ReiMachineRecipes.registerRecipeCategoryForMachine(workstation, category, ReiMachineRecipes.MachineScreenPredicate.MULTIBLOCK);
                ReiMachineRecipes.registerMachineClickArea(workstation, CRAFTING_GUI);
            }
            for (ResourceLocation extraWorkstation : extraWorkstations) {
                ReiMachineRecipes.registerWorkstation(category, extraWorkstation);
            }
        }
    }
}

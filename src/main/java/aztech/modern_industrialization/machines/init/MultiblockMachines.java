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

import static aztech.modern_industrialization.machines.multiblocks.HatchType.*;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.compat.rei.Rectangle;
import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.blockentities.multiblocks.*;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.components.sync.CraftingMultiblockGui;
import aztech.modern_industrialization.machines.components.sync.ProgressBar;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModels;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.Fluids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class MultiblockMachines {
    public static BlockEntityType COKE_OVEN;
    public static BlockEntityType STEAM_BLAST_FURNACE;
    public static BlockEntityType STEAM_QUARRY;
    public static BlockEntityType ELECTRIC_BLAST_FURNACE;
    public static BlockEntityType LARGE_STEAM_BOILER;
    public static BlockEntityType ADVANCED_LARGE_STEAM_BOILER;
    public static BlockEntityType HIGH_PRESSURE_LARGE_STEAM_BOILER;
    public static BlockEntityType HIGH_PRESSURE_ADVANCED_LARGE_STEAM_BOILER;
    public static BlockEntityType ELECTRIC_QUARRY;
    public static BlockEntityType OIL_DRILLING_RIG;
    public static BlockEntityType VACUUM_FREEZER;
    public static BlockEntityType DISTILLATION_TOWER;

    public static void init() {
        SimpleMember bricks = SimpleMember.forBlock(Blocks.BRICKS);
        HatchFlags cokeOvenHatches = new HatchFlags.Builder().with(ITEM_INPUT).with(ITEM_OUTPUT).with(FLUID_INPUT).build();
        ShapeTemplate cokeOvenShape = new ShapeTemplate.Builder(MachineCasings.BRICKS).add3by3Levels(-1, 1, bricks, cokeOvenHatches).build();
        COKE_OVEN = MachineRegistrationHelper.registerMachine("coke_oven",
                bet -> new SteamCraftingMultiblockBlockEntity(bet, "coke_oven", cokeOvenShape, MIMachineRecipeTypes.COKE_OVEN));
        ReiMachineRecipes.registerMultiblockShape("coke_oven", cokeOvenShape);

        SimpleMember fireclayBricks = SimpleMember.forBlock(MIBlock.BLOCK_FIRE_CLAY_BRICKS);
        HatchFlags sbfHatches = new HatchFlags.Builder().with(ITEM_INPUT, ITEM_OUTPUT, FLUID_INPUT, FLUID_OUTPUT).build();
        ShapeTemplate sbfShape = new ShapeTemplate.Builder(MachineCasings.FIREBRICKS).add3by3Levels(-1, 2, fireclayBricks, sbfHatches).build();
        STEAM_BLAST_FURNACE = MachineRegistrationHelper.registerMachine("steam_blast_furnace",
                bet -> new SteamCraftingMultiblockBlockEntity(bet, "steam_blast_furnace", sbfShape, MIMachineRecipeTypes.BLAST_FURNACE));
        ReiMachineRecipes.registerMultiblockShape("steam_blast_furnace", sbfShape);


        ELECTRIC_BLAST_FURNACE = MachineRegistrationHelper.registerMachine("electric_blast_furnace",
                bet -> new ElectricBlastFurnaceBlockEntity(bet));
        ElectricBlastFurnaceBlockEntity.registerReiShapes();

        SimpleMember invarCasings = SimpleMember.forBlock(MIBlock.blocks.get("heatproof_machine_casing"));
        SimpleMember bronzePlatedBricks = SimpleMember.forBlock(MIBlock.blocks.get("bronze_plated_bricks"));
        SimpleMember bronzePipe = SimpleMember.forBlock(MIBlock.blocks.get("bronze_machine_casing_pipe"));
        HatchFlags slbHatchFlags = new HatchFlags.Builder().with(ITEM_INPUT, FLUID_INPUT, FLUID_OUTPUT).build();
        ShapeTemplate largeSteamBoilerShape = new ShapeTemplate.Builder(MachineCasings.HEATPROOF).add3by3(-1, invarCasings, false, slbHatchFlags)
                .add3by3(0, bronzePlatedBricks, true, null).add3by3(1, bronzePlatedBricks, true, null).add3by3(2, bronzePlatedBricks, false, null)
                .add(0, 0, 1, bronzePipe, null).add(0, 1, 1, bronzePipe, null).build();

        LARGE_STEAM_BOILER = MachineRegistrationHelper.registerMachine("large_steam_boiler",
                bet -> new SteamBoilerMultiblockBlockEntity(bet, largeSteamBoilerShape, "large_steam_boiler",
                        16, 1, Fluids.WATER, MIFluids.STEAM));
        ReiMachineRecipes.registerMultiblockShape("large_steam_boiler", largeSteamBoilerShape);


        ShapeTemplate advancedLargeSteamBoilerShape = new ShapeTemplate.Builder(MachineCasings.HEATPROOF)
                .add3by3(-2, invarCasings, false, slbHatchFlags)
                .add3by3(-1, bronzePlatedBricks, true, null)
                .add3by3(0, bronzePlatedBricks, true, null)
                .add3by3(1, bronzePlatedBricks, true, null)
                .add3by3(2, bronzePlatedBricks, false, null)
                .add(0, -1, 1, bronzePipe, null)
                .add(0, 0, 1, bronzePipe, null)
                .add(0, 1, 1, bronzePipe, null).build();

        ADVANCED_LARGE_STEAM_BOILER = MachineRegistrationHelper.registerMachine("advanced_large_steam_boiler",
                bet -> new SteamBoilerMultiblockBlockEntity(bet, advancedLargeSteamBoilerShape, "advanced_large_steam_boiler",
                        64, 1, Fluids.WATER, MIFluids.STEAM));
        ReiMachineRecipes.registerMultiblockShape("advanced_large_steam_boiler", advancedLargeSteamBoilerShape);

        SimpleMember stainlessSteelClean = SimpleMember.forBlock(MIBlock.blocks.get("clean_stainless_steel_machine_casing"));
        SimpleMember stainlessSteelPipe = SimpleMember.forBlock(MIBlock.blocks.get("stainless_steel_machine_casing_pipe"));
        ShapeTemplate highPressureLargeSteamBoilerShape = new ShapeTemplate.Builder(MachineCasings.HEATPROOF)
                .add3by3(-1, invarCasings, false, slbHatchFlags)
                .add3by3(0, stainlessSteelClean, true, null)
                .add3by3(1, stainlessSteelClean, true, null)
                .add3by3(2, stainlessSteelClean, false, null)
                .add(0, 0, 1, stainlessSteelPipe, null)
                .add(0, 1, 1, stainlessSteelPipe, null).build();

        HIGH_PRESSURE_LARGE_STEAM_BOILER = MachineRegistrationHelper.registerMachine("high_pressure_large_steam_boiler",
                bet -> new SteamBoilerMultiblockBlockEntity(bet, highPressureLargeSteamBoilerShape, "high_pressure_large_steam_boiler",
                        16, 8, MIFluids.HIGH_PRESSURE_WATER, MIFluids.HIGH_PRESSURE_STEAM));
        ReiMachineRecipes.registerMultiblockShape("high_pressure_large_steam_boiler", highPressureLargeSteamBoilerShape);

        ShapeTemplate highPressureAdvancedLargeSteamBoilerShape = new ShapeTemplate.Builder(MachineCasings.HEATPROOF)
                .add3by3(-2, invarCasings, false, slbHatchFlags)
                .add3by3(-1, stainlessSteelClean, true, null)
                .add3by3(0, stainlessSteelClean, true, null)
                .add3by3(1, stainlessSteelClean, true, null)
                .add3by3(2, stainlessSteelClean, false, null)
                .add(0, -1, 1, stainlessSteelPipe, null)
                .add(0, 0, 1, stainlessSteelPipe, null)
                .add(0, 1, 1, stainlessSteelPipe, null).build();

        HIGH_PRESSURE_ADVANCED_LARGE_STEAM_BOILER = MachineRegistrationHelper.registerMachine("high_pressure_advanced_large_steam_boiler",
                bet -> new SteamBoilerMultiblockBlockEntity(bet, highPressureAdvancedLargeSteamBoilerShape, "high_pressure_advanced_large_steam_boiler",
                        64, 8, MIFluids.HIGH_PRESSURE_WATER, MIFluids.HIGH_PRESSURE_STEAM));
        ReiMachineRecipes.registerMultiblockShape("high_pressure_advanced_large_steam_boiler", highPressureAdvancedLargeSteamBoilerShape);

        SimpleMember steelCasing = SimpleMember.forBlock(MIBlock.blocks.get("steel_machine_casing"));
        SimpleMember steelPipe = SimpleMember.forBlock(MIBlock.blocks.get("steel_machine_casing_pipe"));
        HatchFlags quarryHatchFlags = new HatchFlags.Builder().with(ITEM_INPUT, FLUID_INPUT, ITEM_OUTPUT).build();
        HatchFlags quarryElectricHatchFlags = new HatchFlags.Builder().with(ITEM_INPUT, ITEM_OUTPUT, ENERGY_INPUT).build();

        ShapeTemplate.Builder quarryShapeBuilder = new ShapeTemplate.Builder(MachineCasings.STEEL).add3by3(0, steelCasing, true, quarryHatchFlags)
                .add3by3(1, steelCasing, true, quarryHatchFlags);

        ShapeTemplate.Builder quarryElectricShapeBuilder = new ShapeTemplate.Builder(MachineCasings.STEEL)
                .add3by3(0, steelCasing, true, quarryElectricHatchFlags).add3by3(1, steelCasing, true, quarryElectricHatchFlags);

        for (int y = 2; y <= 4; y++) {
            quarryShapeBuilder.add(-1, y, 1, steelPipe, null);
            quarryShapeBuilder.add(1, y, 1, steelPipe, null);
            quarryElectricShapeBuilder.add(-1, y, 1, steelPipe, null);
            quarryElectricShapeBuilder.add(1, y, 1, steelPipe, null);
        }
        quarryShapeBuilder.add(0, 4, 1, steelCasing, null);
        quarryElectricShapeBuilder.add(0, 4, 1, steelCasing, null);

        SimpleMember chain = SimpleMember.verticalChain();

        for (int y = 0; y <= 3; y++) {
            quarryShapeBuilder.add(0, y, 1, chain, null);
            quarryElectricShapeBuilder.add(0, y, 1, chain, null);
        }

        ShapeTemplate quarryShape = quarryShapeBuilder.build();
        ShapeTemplate quarryElectricShape = quarryElectricShapeBuilder.build();

        STEAM_QUARRY = MachineRegistrationHelper.registerMachine("quarry",
                bet -> new SteamCraftingMultiblockBlockEntity(bet, "quarry", quarryShape, MIMachineRecipeTypes.QUARRY));
        ReiMachineRecipes.registerMultiblockShape("quarry", quarryShape);
        ELECTRIC_QUARRY = MachineRegistrationHelper.registerMachine("electric_quarry",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "electric_quarry", quarryElectricShape, MIMachineRecipeTypes.QUARRY));
        ReiMachineRecipes.registerMultiblockShape("electric_quarry", quarryElectricShape);

        SimpleMember frostproofMachineCasing = SimpleMember.forBlock(MIBlock.blocks.get("frostproof_machine_casing"));
        HatchFlags vacuumFreezerHatches = new HatchFlags.Builder().with(ITEM_INPUT).with(ITEM_OUTPUT).with(FLUID_INPUT).with(FLUID_OUTPUT)
                .with(ENERGY_INPUT).build();
        ShapeTemplate vacuumFreezerShape = new ShapeTemplate.Builder(MachineCasings.FROSTPROOF)
                .add3by3LevelsRoofed(-1, 2, frostproofMachineCasing, vacuumFreezerHatches).build();
        VACUUM_FREEZER = MachineRegistrationHelper.registerMachine("vacuum_freezer",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "vacuum_freezer", vacuumFreezerShape, MIMachineRecipeTypes.VACUUM_FREEZER));
        ReiMachineRecipes.registerMultiblockShape("vacuum_freezer", vacuumFreezerShape);

        oilDrillingRig();

        DISTILLATION_TOWER = MachineRegistrationHelper.registerMachine("distillation_tower", DistillationTowerBlockEntity::new);
        DistillationTowerBlockEntity.registerReiShapes();
    }

    public static void oilDrillingRig() {
        ShapeTemplate.Builder oilDrillingRigShapeBuilder = new ShapeTemplate.Builder(MachineCasings.STEEL);
        SimpleMember steelCasing = SimpleMember.forBlock(MIBlock.blocks.get("steel_machine_casing"));
        SimpleMember steelPipe = SimpleMember.forBlock(MIBlock.blocks.get("steel_machine_casing_pipe"));
        SimpleMember chain = SimpleMember.verticalChain();
        HatchFlags hatchFlags = new HatchFlags.Builder().with(ITEM_INPUT).with(FLUID_OUTPUT).with(ENERGY_INPUT).build();
        // pillars
        for (int y = -4; y <= -2; ++y) {
            oilDrillingRigShapeBuilder.add(-2, y, -1, steelCasing, null);
            oilDrillingRigShapeBuilder.add(2, y, -1, steelCasing, null);
            oilDrillingRigShapeBuilder.add(-2, y, 3, steelCasing, null);
            oilDrillingRigShapeBuilder.add(2, y, 3, steelCasing, null);
        }
        // platform
        for (int x = -2; x <= 2; ++x) {
            for (int z = -1; z <= 3; ++z) {
                if (x == 2 || x == -2 || z == -1 || z == 3) {
                    oilDrillingRigShapeBuilder.add(x, -1, z, steelCasing, null);
                }
            }
        }
        // chains and pipe casings
        for (int y = -4; y <= 4; ++y) {
            oilDrillingRigShapeBuilder.add(-1, y, 1, chain, null);
            oilDrillingRigShapeBuilder.add(1, y, 1, chain, null);
            if (y >= -1) {
                oilDrillingRigShapeBuilder.add(0, y, 1, steelPipe, null);
            }
        }
        // top
        for (int x = -2; x <= 2; ++x) {
            oilDrillingRigShapeBuilder.add(x, 5, 1, steelCasing, null);
        }
        // hatches
        oilDrillingRigShapeBuilder.add(-1, 0, 0, steelCasing, hatchFlags);
        oilDrillingRigShapeBuilder.add(1, 0, 0, steelCasing, hatchFlags);
        oilDrillingRigShapeBuilder.add(-1, 0, 2, steelCasing, hatchFlags);
        oilDrillingRigShapeBuilder.add(0, 0, 2, steelCasing, hatchFlags);
        oilDrillingRigShapeBuilder.add(1, 0, 2, steelCasing, hatchFlags);

        ShapeTemplate oilDrillingRigShape = oilDrillingRigShapeBuilder.build();

        OIL_DRILLING_RIG = MachineRegistrationHelper.registerMachine("oil_drilling_rig", bet -> new ElectricCraftingMultiblockBlockEntity(bet,
                "oil_drilling_rig", oilDrillingRigShape, MIMachineRecipeTypes.OIL_DRILLING_RIG));
        ReiMachineRecipes.registerMultiblockShape("oil_drilling_rig", oilDrillingRigShape);
    }

    @SuppressWarnings("unchecked")
    public static void clientInit() {
        MachineModels.addTieredMachine("coke_oven", "coke_oven", MachineCasings.BRICKS, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(COKE_OVEN, MultiblockMachineBER::new);
        new Rei("coke_oven", MIMachineRecipeTypes.COKE_OVEN, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlot(102, 35))
                .fluids(inputs -> {}, outputs -> outputs.addSlot(102, 53))
                .register();

        MachineModels.addTieredMachine("steam_blast_furnace", "steam_blast_furnace", MachineCasings.FIREBRICKS, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(STEAM_BLAST_FURNACE, MultiblockMachineBER::new);
        new Rei("steam_blast_furnace", MIMachineRecipeTypes.BLAST_FURNACE, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlots(56, 35, 2, 1), outputs -> outputs.addSlots(102, 35, 1, 1))
                .fluids(fluids -> fluids.addSlots(36, 35, 1, 1), outputs -> outputs.addSlots(122, 35, 1, 1))
                .workstations("steam_blast_furnace", "electric_blast_furnace").extraTest(recipe -> recipe.eu <= 4)
                .register();

        MachineModels.addTieredMachine("electric_blast_furnace", "electric_blast_furnace", MachineCasings.HEATPROOF, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(ELECTRIC_BLAST_FURNACE, MultiblockMachineBER::new);
        new Rei("electric_blast_furnace", MIMachineRecipeTypes.BLAST_FURNACE, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlots(56, 35, 2, 1), outputs -> outputs.addSlot(102, 35))
                .fluids(fluids -> fluids.addSlot(36, 35), outputs -> outputs.addSlot(122, 35))
                .extraTest(recipe -> recipe.eu > 4)
                .register();

        MachineModels.addTieredMachine("large_steam_boiler", "large_boiler", MachineCasings.BRONZE_PLATED_BRICKS, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(LARGE_STEAM_BOILER, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("advanced_large_steam_boiler", "large_boiler", MachineCasings.BRONZE_PLATED_BRICKS, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(ADVANCED_LARGE_STEAM_BOILER, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("high_pressure_large_steam_boiler", "large_boiler", MachineCasings.CLEAN_STAINLESS_STEEL, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(HIGH_PRESSURE_LARGE_STEAM_BOILER, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("high_pressure_advanced_large_steam_boiler", "large_boiler", MachineCasings.CLEAN_STAINLESS_STEEL, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(HIGH_PRESSURE_ADVANCED_LARGE_STEAM_BOILER, MultiblockMachineBER::new);


        MachineModels.addTieredMachine("quarry", "quarry", MachineCasings.STEEL, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(STEAM_QUARRY, MultiblockMachineBER::new);
        new Rei("quarry", MIMachineRecipeTypes.QUARRY, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlots(102, 35, 4, 4))
                .workstations("quarry", "electric_quarry")
                .register();

        MachineModels.addTieredMachine("electric_quarry", "quarry", MachineCasings.STEEL, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(ELECTRIC_QUARRY, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("vacuum_freezer", "vacuum_freezer", MachineCasings.FROSTPROOF, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(VACUUM_FREEZER, MultiblockMachineBER::new);
        new Rei("vacuum_freezer", MIMachineRecipeTypes.VACUUM_FREEZER, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlots(56, 35, 2, 1), outputs -> outputs.addSlot(102, 35))
                .fluids(inputs -> inputs.addSlot(36, 35), outputs -> outputs.addSlot(122, 35))
                .register();

        MachineModels.addTieredMachine("oil_drilling_rig", "oil_drilling_rig", MachineCasings.STEEL, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(OIL_DRILLING_RIG, MultiblockMachineBER::new);
        new Rei("oil_drilling_rig", MIMachineRecipeTypes.OIL_DRILLING_RIG, new ProgressBar.Parameters(77, 33, "arrow"))
                .items(inputs -> inputs.addSlot(36, 35), outputs -> {})
                .fluids(inputs -> {}, outputs -> outputs.addSlot(122, 35))
                .register();

        MachineModels.addTieredMachine("distillation_tower", "distillation_tower", MachineCasings.CLEAN_STAINLESS_STEEL, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(DISTILLATION_TOWER, MultiblockMachineBER::new);
        new Rei("distillation_tower", MIMachineRecipeTypes.DISTILLATION_TOWER, new ProgressBar.Parameters(77, 33, "arrow"))
                .fluids(inputs -> inputs.addSlot(56, 35), outputs -> outputs.addSlots(102, 35, 1, 8))
                .register();
    }

    private static final Rectangle CRAFTING_GUI = new Rectangle(CraftingMultiblockGui.X, CraftingMultiblockGui.Y,
            CraftingMultiblockGui.W, CraftingMultiblockGui.H);

    private static class Rei {
        private final String category;
        private final MachineRecipeType recipeType;
        private final ProgressBar.Parameters progressBarParams;
        private final List<String> workstations;
        private Predicate<MachineRecipe> extraTest = recipe -> true;
        private SlotPositions itemInputs = SlotPositions.empty();
        private SlotPositions itemOutputs = SlotPositions.empty();
        private SlotPositions fluidInputs = SlotPositions.empty();
        private SlotPositions fluidOutputs = SlotPositions.empty();
        private static final Predicate<MachineScreenHandlers.ClientScreen> SHAPE_VALID_PREDICATE = screen -> {
            for (SyncedComponent.Client client : screen.getScreenHandler().components) {
                if (client instanceof CraftingMultiblockGui.Client) {
                    CraftingMultiblockGui.Client cmGui = (CraftingMultiblockGui.Client) client;
                    if (cmGui.isShapeValid) {
                        return true;
                    }
                }
            }
            return false;
        };

        Rei(String category, MachineRecipeType recipeType, ProgressBar.Parameters progressBarParams) {
            this.category = category;
            this.recipeType = recipeType;
            this.progressBarParams = progressBarParams;
            this.workstations = new ArrayList<>();
            workstations.add(category);
        }

        Rei items(Consumer<SlotPositions.Builder> inputs, Consumer<SlotPositions.Builder> outputs) {
            itemInputs = new SlotPositions.Builder().buildWithConsumer(inputs);
            itemOutputs = new SlotPositions.Builder().buildWithConsumer(outputs);
            return this;
        }

        Rei fluids(Consumer<SlotPositions.Builder> inputs, Consumer<SlotPositions.Builder> outputs) {
            fluidInputs = new SlotPositions.Builder().buildWithConsumer(inputs);
            fluidOutputs = new SlotPositions.Builder().buildWithConsumer(outputs);
            return this;
        }

        Rei extraTest(Predicate<MachineRecipe> extraTest) {
            this.extraTest = extraTest;
            return this;
        }

        Rei workstations(String... workstations) {
            this.workstations.clear();
            this.workstations.addAll(Arrays.asList(workstations));
            return this;
        }

        final void register() {
            ReiMachineRecipes.registerCategory(category, new MachineCategoryParams(category, itemInputs, itemOutputs, fluidInputs, fluidOutputs, progressBarParams, recipe -> recipe.getType() == recipeType && extraTest.test(recipe)));
            for (String workstation : workstations) {
                ReiMachineRecipes.registerWorkstation(category, workstation);
                ReiMachineRecipes.registerRecipeCategoryForMachine(workstation, category, SHAPE_VALID_PREDICATE);
                ReiMachineRecipes.registerMachineClickArea(workstation, CRAFTING_GUI);
            }
        }
    }
}

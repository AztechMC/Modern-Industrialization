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
package aztech.modern_industrialization.machinesv2.init;

import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.*;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.machinesv2.blockentities.multiblocks.ElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machinesv2.blockentities.multiblocks.LargeSteamBoilerMultiblockBlockEntity;
import aztech.modern_industrialization.machinesv2.blockentities.multiblocks.SteamCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModels;
import aztech.modern_industrialization.machinesv2.multiblocks.HatchFlags;
import aztech.modern_industrialization.machinesv2.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machinesv2.multiblocks.SimpleMember;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;

@SuppressWarnings("rawtypes")
public class MultiblockMachines {
    public static BlockEntityType COKE_OVEN;
    public static BlockEntityType STEAM_BLAST_FURNACE;
    public static BlockEntityType STEAM_QUARRY;
    public static BlockEntityType ELECTRIC_BLAST_FURNACE;
    public static BlockEntityType LARGE_STEAM_BOILER;
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

        SimpleMember fireclayBricks = SimpleMember.forBlock(MIBlock.BLOCK_FIRE_CLAY_BRICKS);
        HatchFlags sbfHatches = new HatchFlags.Builder().with(ITEM_INPUT, ITEM_OUTPUT, FLUID_INPUT, FLUID_OUTPUT).build();
        ShapeTemplate sbfShape = new ShapeTemplate.Builder(MachineCasings.FIREBRICKS).add3by3Levels(-1, 2, fireclayBricks, sbfHatches).build();
        STEAM_BLAST_FURNACE = MachineRegistrationHelper.registerMachine("steam_blast_furnace",
                bet -> new SteamCraftingMultiblockBlockEntity(bet, "steam_blast_furnace", sbfShape, MIMachineRecipeTypes.BLAST_FURNACE));

        SimpleMember invarCasings = SimpleMember.forBlock(MIBlock.blocks.get("heatproof_machine_casing"));
        SimpleMember cupronickelCoils = SimpleMember.forBlock(MIBlock.blocks.get("cupronickel_coil"));
        HatchFlags ebfHatches = new HatchFlags.Builder().with(ITEM_INPUT, ITEM_OUTPUT, FLUID_INPUT, FLUID_OUTPUT, ENERGY_INPUT).build();
        ShapeTemplate ebfShape = new ShapeTemplate.Builder(MachineCasings.HEATPROOF).add3by3(0, invarCasings, false, ebfHatches)
                .add3by3(1, cupronickelCoils, true, null).add3by3(2, cupronickelCoils, true, null).add3by3(3, invarCasings, false, ebfHatches)
                .build();
        ELECTRIC_BLAST_FURNACE = MachineRegistrationHelper.registerMachine("electric_blast_furnace",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "electric_blast_furnace", ebfShape, MIMachineRecipeTypes.BLAST_FURNACE));

        SimpleMember bronzePlatedBricks = SimpleMember.forBlock(MIBlock.blocks.get("bronze_plated_bricks"));
        SimpleMember bronzePipe = SimpleMember.forBlock(MIBlock.blocks.get("bronze_machine_casing_pipe"));
        HatchFlags slbHatchFlags = new HatchFlags.Builder().with(ITEM_INPUT, FLUID_INPUT, FLUID_OUTPUT).build();
        ShapeTemplate largeSteamBoilerShape = new ShapeTemplate.Builder(MachineCasings.HEATPROOF).add3by3(-1, invarCasings, false, slbHatchFlags)
                .add3by3(0, bronzePlatedBricks, true, null).add3by3(1, bronzePlatedBricks, true, null).add3by3(2, bronzePlatedBricks, false, null)
                .add(0, 0, 1, bronzePipe, null).add(0, 1, 1, bronzePipe, null).build();
        LARGE_STEAM_BOILER = MachineRegistrationHelper.registerMachine("large_steam_boiler",
                bet -> new LargeSteamBoilerMultiblockBlockEntity(bet, largeSteamBoilerShape));

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
        ELECTRIC_QUARRY = MachineRegistrationHelper.registerMachine("electric_quarry",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "electric_quarry", quarryElectricShape, MIMachineRecipeTypes.QUARRY));

        SimpleMember frostproofMachineCasing = SimpleMember.forBlock(MIBlock.blocks.get("frostproof_machine_casing"));
        HatchFlags vacuumFreezerHatches = new HatchFlags.Builder().with(ITEM_INPUT).with(ITEM_OUTPUT).with(FLUID_INPUT).with(FLUID_OUTPUT)
                .with(ENERGY_INPUT).build();
        ShapeTemplate vacuumFreezerShape = new ShapeTemplate.Builder(MachineCasings.FROSTPROOF)
                .add3by3LevelsRoofed(-1, 2, frostproofMachineCasing, vacuumFreezerHatches).build();
        VACUUM_FREEZER = MachineRegistrationHelper.registerMachine("vacuum_freezer",
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, "vacuum_freezer", vacuumFreezerShape, MIMachineRecipeTypes.VACUUM_FREEZER));

        oilDrillingRig();
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

        OIL_DRILLING_RIG = MachineRegistrationHelper.registerMachine("oil_drilling_rig", bet -> new ElectricCraftingMultiblockBlockEntity(bet,
                "oil_drilling_rig", oilDrillingRigShapeBuilder.build(), MIMachineRecipeTypes.OIL_DRILLING_RIG));
    }

    @SuppressWarnings("unchecked")
    public static void clientInit() {
        MachineModels.addTieredMachine("coke_oven", "coke_oven", MachineCasings.BRICKS, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(COKE_OVEN, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("steam_blast_furnace", "steam_blast_furnace", MachineCasings.FIREBRICKS, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(STEAM_BLAST_FURNACE, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("electric_blast_furnace", "electric_blast_furnace", MachineCasings.HEATPROOF, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(ELECTRIC_BLAST_FURNACE, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("large_steam_boiler", "large_boiler", MachineCasings.BRONZE_PLATED_BRICKS, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(LARGE_STEAM_BOILER, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("quarry", "quarry", MachineCasings.STEEL, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(STEAM_QUARRY, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("electric_quarry", "quarry", MachineCasings.STEEL, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(ELECTRIC_QUARRY, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("vacuum_freezer", "vacuum_freezer", MachineCasings.FROSTPROOF, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(VACUUM_FREEZER, MultiblockMachineBER::new);

        MachineModels.addTieredMachine("oil_drilling_rig", "oil_drilling_rig", MachineCasings.STEEL, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(OIL_DRILLING_RIG, MultiblockMachineBER::new);
    }
}

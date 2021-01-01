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
package aztech.modern_industrialization.machines;

import static aztech.modern_industrialization.machines.impl.MachineTier.*;
import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.*;
import static aztech.modern_industrialization.machines.impl.multiblock.MultiblockShapes.*;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.impl.SteamMachineFactory;
import aztech.modern_industrialization.machines.impl.multiblock.*;
import aztech.modern_industrialization.machines.recipe.FurnaceRecipeProxy;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.special.*;
import aztech.modern_industrialization.nuclear.MINuclearItem;
import aztech.modern_industrialization.nuclear.NuclearReactorBlockEntity;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;

public class MIMachines {
    // Recipe
    public static final Map<MachineRecipeType, RecipeInfo> RECIPE_TYPES = new TreeMap<>(Comparator.comparing(MachineRecipeType::getId));
    public static final List<MachineFactory> WORKSTATIONS_FURNACES = new ArrayList<>();

    private static MachineRecipeType createRecipeType(String kind) {
        MachineRecipeType type = new MachineRecipeType(new MIIdentifier(kind));
        RECIPE_TYPES.put(type, new RecipeInfo());
        return type;
    }

    public static class RecipeInfo {
        public final List<MachineFactory> factories = new ArrayList<>();
    }

    // Single block
    public static final MachineRecipeType RECIPE_ASSEMBLER = createRecipeType("assembler").withItemInputs().withFluidInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_CENTRIFUGE = createRecipeType("centrifuge").withItemInputs().withFluidInputs().withItemOutputs()
            .withFluidOutputs();
    public static final MachineRecipeType RECIPE_CHEMICAL_REACTOR = createRecipeType("chemical_reactor").withItemInputs().withFluidInputs()
            .withItemOutputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_COMPRESSOR = createRecipeType("compressor").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_CUTTING_MACHINE = createRecipeType("cutting_machine").withItemInputs().withFluidInputs()
            .withItemOutputs();
    public static final MachineRecipeType RECIPE_DISTILLERY = createRecipeType("distillery").withFluidInputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_ELECTROLYZER = createRecipeType("electrolyzer").withItemInputs().withFluidInputs().withItemOutputs()
            .withFluidOutputs();
    // public static final MachineRecipeType RECIPE_FLUID_EXTRACTOR =
    // createRecipeType("fluid_extractor").withItemInputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_FURNACE = new FurnaceRecipeProxy(null);
    public static final MachineRecipeType RECIPE_MACERATOR = createRecipeType("macerator").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_MIXER = createRecipeType("mixer").withItemInputs().withFluidInputs().withItemOutputs()
            .withFluidOutputs();
    public static final MachineRecipeType RECIPE_PACKER = createRecipeType("packer").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_POLARIZER = createRecipeType("polarizer").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_WIREMILL = createRecipeType("wiremill").withItemInputs().withItemOutputs();
    // Multi block
    public static final MachineRecipeType RECIPE_COKE_OVEN = createRecipeType("coke_oven").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_BLAST_FURNACE = createRecipeType("blast_furnace").withItemInputs().withItemOutputs()
            .withFluidInputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_DISTILLATION_TOWER = createRecipeType("distillation_tower").withFluidInputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_VACUUM_FREEZER = createRecipeType("vacuum_freezer").withItemInputs().withItemOutputs()
            .withFluidInputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_OIL_DRILLING_RIG = createRecipeType("oil_drilling_rig").withItemInputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_QUARRY = createRecipeType("quarry").withItemInputs().withItemOutputs();

    // Shapes
    public static MultiblockShape COKE_OVEN_SHAPE;
    public static MultiblockShape BLAST_FURNACE_SHAPE;
    public static MultiblockShape STEAM_QUARRY_SHAPE;
    public static MultiblockShape ELECTRIC_QUARRY_SHAPE;
    public static MultiblockShape LARGE_BOILER_SHAPE;
    public static MultiblockShape OIL_DRILLING_RIG_SHAPE;
    public static MultiblockShape VACUUM_FREEZER_SHAPE;
    public static MultiblockShape NUCLEAR_REACTOR_SHAPE;

    public static final MachineFactory LARGE_STEAM_BOILER;
    public static final MachineFactory ELECTRIC_BLAST_FURNACE;
    public static final MachineFactory DIESEL_GENERATOR;

    private static MultiblockShape cokeOvenLike(int height, Block block, int extra_flags) {
        MultiblockShape shape = new MultiblockShape();
        MultiblockShape.Entry main_block = MultiblockShapes.block(block);
        for (int y = 0; y < height - 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = 0; z <= 2; z++) {
                    if (x != 0 || z != 1) {
                        if (x != 0 || y != 0 || z != 0) {
                            shape.addEntry(x, y, z, main_block);
                        }
                    }
                }
            }
        }

        MultiblockShape.Entry optionalHatch = MultiblockShapes.or(main_block,
                MultiblockShapes.hatch(HATCH_FLAG_FLUID_INPUT | HATCH_FLAG_ITEM_INPUT | HATCH_FLAG_ITEM_OUTPUT | extra_flags));
        for (int x = -1; x <= 1; x++) {
            for (int z = 0; z <= 2; z++) {
                shape.addEntry(x, -1, z, optionalHatch);
            }
        }
        return shape;
    }

    private static MultiblockShape quarryLike(int hatchFlags, int maxHatches) {
        MultiblockShape shape = new MultiblockShape();
        MultiblockShape.Entry steelCasing = MultiblockShapes.blockId(new MIIdentifier("steel_machine_casing"));
        MultiblockShape.Entry steelCasingPipe = MultiblockShapes.blockId(new MIIdentifier("steel_machine_casing_pipe"));
        MultiblockShape.Entry optionalQuarryHatch = MultiblockShapes.or(steelCasing, MultiblockShapes.hatch(hatchFlags));

        for (int z = 0; z < 3; z++) {
            shape.addEntry(1, 0, z, optionalQuarryHatch);
            shape.addEntry(-1, 0, z, optionalQuarryHatch);
        }
        shape.addEntry(0, 0, 2, optionalQuarryHatch);
        shape.addEntry(0, 0, 1, MultiblockShapes.verticalChain());

        for (int x = -1; x <= 1; ++x) {
            for (int z = 0; z < 3; z++) {
                if ((x != 0 || z != 1)) {
                    shape.addEntry(x, 1, z, optionalQuarryHatch);
                } else {
                    shape.addEntry(0, 1, 1, MultiblockShapes.verticalChain());
                }
            }
        }

        for (int y = 2; y < 5; y++) {
            shape.addEntry(-1, y, 1, steelCasingPipe);
            shape.addEntry(1, y, 1, steelCasingPipe);
            shape.addEntry(0, y, 1, y < 4 ? MultiblockShapes.verticalChain() : steelCasing);
        }
        shape.setMaxHatches(maxHatches);
        return shape;
    }

    static {
        COKE_OVEN_SHAPE = cokeOvenLike(3, Blocks.BRICKS, 0);
        BLAST_FURNACE_SHAPE = cokeOvenLike(4, MIBlock.BLOCK_FIRE_CLAY_BRICKS, HATCH_FLAG_FLUID_OUTPUT);

        STEAM_QUARRY_SHAPE = quarryLike(HATCH_FLAG_ITEM_INPUT | HATCH_FLAG_ITEM_OUTPUT | HATCH_FLAG_FLUID_INPUT, 4);
        ELECTRIC_QUARRY_SHAPE = quarryLike(HATCH_FLAG_ITEM_INPUT | HATCH_FLAG_ITEM_OUTPUT | HATCH_FLAG_ENERGY_INPUT, 6);

        VACUUM_FREEZER_SHAPE = cokeOvenLike(3, MIBlock.FROSTPROOF_MACHINE_CASING, 31).setMaxHatches(5);

        LARGE_BOILER_SHAPE = new MultiblockShape();
        MultiblockShape.Entry bronzeCasing = MultiblockShapes.block(MIBlock.BRONZE_PLATED_BRICKS);
        MultiblockShape.Entry bronzeCasingPipe = MultiblockShapes.block(MIBlock.BRONZE_MACHINE_CASING_PIPE);
        MultiblockShape.Entry optionalLargeBoilerHatch = MultiblockShapes.or(MultiblockShapes.block(MIBlock.HEATPROOF_MACHINE_CASING),
                MultiblockShapes.hatch(HATCH_FLAG_ITEM_INPUT | HATCH_FLAG_FLUID_INPUT | HATCH_FLAG_FLUID_OUTPUT));

        for (int x = -1; x <= 1; ++x) {
            for (int z = 0; z < 3; ++z) {
                LARGE_BOILER_SHAPE.addEntry(x, -1, z, optionalLargeBoilerHatch);
            }
        }
        for (int x = -1; x <= 1; ++x) {
            for (int y = 0; y < 3; ++y) {
                for (int z = 0; z < 3; ++z) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    if (x == 0 && z == 1) {
                        LARGE_BOILER_SHAPE.addEntry(x, y, z, y == 2 ? bronzeCasing : bronzeCasingPipe);
                    } else {
                        LARGE_BOILER_SHAPE.addEntry(x, y, z, bronzeCasing);
                    }
                }
            }
        }

        OIL_DRILLING_RIG_SHAPE = new MultiblockShape();
        MultiblockShape.Entry steelCasing = MultiblockShapes.blockId(new MIIdentifier("steel_machine_casing"));
        MultiblockShape.Entry steelCasingPipe = MultiblockShapes.blockId(new MIIdentifier("steel_machine_casing_pipe"));
        MultiblockShape.Entry optionalRigHatch = MultiblockShapes.or(steelCasing,
                MultiblockShapes.hatch(HATCH_FLAG_ITEM_INPUT | HATCH_FLAG_FLUID_OUTPUT | HATCH_FLAG_ENERGY_INPUT));
        // pillars
        for (int y = -4; y <= -2; ++y) {
            OIL_DRILLING_RIG_SHAPE.addEntry(-2, y, -1, steelCasing);
            OIL_DRILLING_RIG_SHAPE.addEntry(2, y, -1, steelCasing);
            OIL_DRILLING_RIG_SHAPE.addEntry(-2, y, 3, steelCasing);
            OIL_DRILLING_RIG_SHAPE.addEntry(2, y, 3, steelCasing);
        }
        // platform
        for (int x = -2; x <= 2; ++x) {
            for (int z = -1; z <= 3; ++z) {
                if (x == 2 || x == -2 || z == -1 || z == 3) {
                    OIL_DRILLING_RIG_SHAPE.addEntry(x, -1, z, steelCasing);
                }
            }
        }
        // chains and pipe casings
        for (int y = -4; y <= 4; ++y) {
            OIL_DRILLING_RIG_SHAPE.addEntry(-1, y, 1, verticalChain());
            OIL_DRILLING_RIG_SHAPE.addEntry(1, y, 1, verticalChain());
            if (y >= -1) {
                OIL_DRILLING_RIG_SHAPE.addEntry(0, y, 1, steelCasingPipe);
            }
        }
        // top
        for (int x = -2; x <= 2; ++x) {
            OIL_DRILLING_RIG_SHAPE.addEntry(x, 5, 1, steelCasing);
        }
        // hatches
        OIL_DRILLING_RIG_SHAPE.addEntry(-1, 0, 0, optionalRigHatch);
        OIL_DRILLING_RIG_SHAPE.addEntry(1, 0, 0, optionalRigHatch);
        OIL_DRILLING_RIG_SHAPE.addEntry(-1, 0, 2, optionalRigHatch);
        OIL_DRILLING_RIG_SHAPE.addEntry(0, 0, 2, optionalRigHatch);
        OIL_DRILLING_RIG_SHAPE.addEntry(1, 0, 2, optionalRigHatch);

        // nuclear reactor
        MultiblockShape.Entry nuclearCasing = MultiblockShapes.blockId(new MIIdentifier("nuclear_machine_casing"));
        MultiblockShape.Entry optionalNuclearInputHatch = MultiblockShapes.or(nuclearCasing,
                MultiblockShapes.hatch(HATCH_FLAG_ITEM_INPUT | HATCH_FLAG_FLUID_INPUT));
        MultiblockShape.Entry optionalNuclearOutputHatch = MultiblockShapes.or(nuclearCasing,
                MultiblockShapes.hatch(HATCH_FLAG_FLUID_OUTPUT | HATCH_FLAG_ITEM_OUTPUT));

        NUCLEAR_REACTOR_SHAPE = new MultiblockShape();
        for (int i = 0; i < 5; i++) {
            for (int j = -2; j <= 2; j++) {
                for (int k = -2; k <= 2; k++) {
                    if ((i == 0 || i == 4) || (j == -2 || j == 2) || (k == -2 || k == 2)) {
                        if (!(i == 0 && j == 0 && k == 0)) {
                            if (j == -2) {
                                NUCLEAR_REACTOR_SHAPE.addEntry(k, j, i, optionalNuclearOutputHatch);
                            } else if (j == 2) {
                                NUCLEAR_REACTOR_SHAPE.addEntry(k, j, i, optionalNuclearInputHatch);
                            } else {
                                NUCLEAR_REACTOR_SHAPE.addEntry(k, j, i, nuclearCasing);
                            }

                        }
                    }
                }
            }
        }

    }

    public static MachineFactory setupAssembler(MachineFactory factory) {
        return factory.setInputSlotPosition(42, 27, 3, 3).setOutputSlotPosition(129, 27, 1, 3).setInputLiquidSlotPosition(98, 27, 1, 1)
                .setupProgressBar(103, 48, 22, 15, true).setupBackground("assembler.png", 176, 186).setupEfficiencyBar(0, 186, 43, 86, 100, 2, true)
                .setupElectricityBar(18, 44).setInventoryPos(8, 104).setupOverlays("assembler", true, false, true);
    }

    public static MachineFactory setupCentrifuge(MachineFactory factory) {
        return factory.setInputSlotPosition(42, 27, 1, 1).setOutputSlotPosition(93, 27, 2, 2).setInputLiquidSlotPosition(42, 47, 1, 1)
                .setLiquidOutputSlotPosition(131, 27, 2, 2).setupProgressBar(66, 35, 22, 15, true).setupBackground("electrolyzer.png")
                .setupEfficiencyBar(0, 166, 50, 66, 100, 2, true).setupElectricityBar(18, 34).setupOverlays("centrifuge", true, true, true)
                .setupCasing("mv");
    }

    public static MachineFactory setupChemicalReactor(MachineFactory factory) {
        return factory.setInputSlotPosition(30, 27, 3, 1).setOutputSlotPosition(116, 27, 3, 1).setInputLiquidSlotPosition(30, 47, 3, 1)
                .setLiquidOutputSlotPosition(116, 47, 3, 1).setupProgressBar(88, 35, 22, 15, true).setupBackground("chemical_reactor.png")
                .setupEfficiencyBar(0, 166, 50, 66, 100, 2, true).setupElectricityBar(12, 34).setupOverlays("chemical_reactor", true, false, false)
                .setupCasing("mv");
    }

    public static MachineFactory setupCompressor(MachineFactory factory) {
        return factory.setInputSlotPosition(56, 35, 1, 1).setOutputSlotPosition(102, 35, 1, 1).setupProgressBar(76, 35, 22, 15, true)
                .setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2, true).setupElectricityBar(18, 34)
                .setupOverlays("compressor", true, true, true);
    }

    public static MachineFactory setupCuttingMachine(MachineFactory factory) {
        return factory.setInputLiquidSlotPosition(40, 35, 1, 1).setInputSlotPosition(60, 35, 1, 1).setOutputSlotPosition(120, 35, 1, 1)
                .setupProgressBar(88, 35, 22, 15, true).setupBackground("cutting_machine.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2, true)
                .setupElectricityBar(18, 34).setupOverlays("cutting_machine", true, false, false);
    }

    public static MachineFactory setupDistillery(MachineFactory factory) {
        return factory.setInputLiquidSlotPosition(56, 35, 1, 1).setLiquidOutputSlotPosition(102, 35, 1, 1).setupProgressBar(76, 35, 22, 15, true)
                .setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2, true).setupElectricityBar(18, 34)
                .setupOverlays("distillery", true, false, false).setupCasing("mv");
    }

    public static MachineFactory setupElectrolyzer(MachineFactory factory) {
        return factory.setInputSlotPosition(42, 27, 1, 1).setOutputSlotPosition(93, 27, 2, 2).setInputLiquidSlotPosition(42, 47, 1, 1)
                .setLiquidOutputSlotPosition(131, 27, 2, 2).setupProgressBar(66, 35, 22, 15, true).setupBackground("electrolyzer.png")
                .setupEfficiencyBar(0, 166, 50, 66, 100, 2, true).setupElectricityBar(18, 34).setupOverlays("electrolyzer", true, true, false)
                .setupCasing("mv");
    }

    public static MachineFactory setupFluidExtractor(MachineFactory factory) {
        return factory.setInputSlotPosition(56, 45, 1, 1).setLiquidOutputSlotPosition(102, 45, 1, 1).setupProgressBar(76, 45, 22, 15, true)
                .setupBackground("steam_furnace.png").setupOverlays("fluid_extractor", true, true, true);
    }

    public static MachineFactory setupFurnace(MachineFactory factory) {
        return factory.setInputSlotPosition(56, 35, 1, 1).setOutputSlotPosition(102, 35, 1, 1).setupProgressBar(76, 35, 22, 15, true)
                .setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2, true).setupElectricityBar(18, 34)
                .setupOverlays("furnace", true, false, false);
    }

    public static MachineFactory setupMacerator(MachineFactory factory) {
        return factory.setInputSlotPosition(56, 35, 1, 1).setOutputSlotPosition(102, 27, 2, 2).setupProgressBar(76, 35, 22, 15, true)
                .setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 66, 100, 2, true).setupElectricityBar(18, 34)
                .setupOverlays("macerator", true, false, true);
    }

    public static MachineFactory setupMixer(MachineFactory factory) {
        return factory.setInputSlotPosition(62, 27, 2, 2).setOutputSlotPosition(129, 27, 1, 2).setInputLiquidSlotPosition(42, 27, 1, 2)
                .setLiquidOutputSlotPosition(149, 27, 1, 2).setupProgressBar(102, 36, 22, 15, true).setupBackground("steam_mixer.png")
                .setupEfficiencyBar(0, 166, 50, 66, 100, 2, true).setupElectricityBar(18, 34).setupOverlays("mixer", true, true, true);
    }

    public static MachineFactory setupPacker(MachineFactory factory) {
        return factory.setInputSlotPosition(56, 27, 1, 2).setOutputSlotPosition(102, 27, 1, 2).setupProgressBar(76, 35, 22, 15, true)
                .setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 66, 100, 2, true).setupElectricityBar(18, 34)
                .setupOverlays("packer", true, false, false);
    }

    public static MachineFactory setupPolarizer(MachineFactory factory) {
        return factory.setInputSlotPosition(56, 35, 1, 1).setOutputSlotPosition(102, 35, 1, 1).setupProgressBar(76, 35, 22, 15, true)
                .setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2, true).setupElectricityBar(18, 34)
                .setupOverlays("polarizer", true, false, true);
    }

    public static MachineFactory setupWiremill(MachineFactory factory) {
        return factory.setInputSlotPosition(56, 35, 1, 1).setOutputSlotPosition(102, 35, 1, 1).setupProgressBar(76, 35, 22, 15, true)
                .setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2, true).setupElectricityBar(18, 34)
                .setupOverlays("wiremill", true, false, true);
    }

    @FunctionalInterface
    private interface MachineSetup {
        MachineFactory setup(MachineFactory factory);
    }

    private static void registerMachineTiers(String machineType, MachineRecipeType recipeType, int inputSlots, int outputSlots, int fluidInputSlots,
            int fluidOutputSlots, MachineSetup setup, boolean steamBricked, boolean bronze, boolean steel) {
        for (MachineTier tier : MachineTier.values()) {
            MachineFactory factory;
            if (tier.isSteam()) {
                if (tier == BRONZE && !bronze)
                    continue;
                if (tier == STEEL && !steel)
                    continue;
                factory = new SteamMachineFactory(tier.toString() + "_" + machineType, tier, MachineBlockEntity::new, recipeType, inputSlots,
                        outputSlots, fluidInputSlots, fluidOutputSlots).setSteamBucketCapacity(tier == BRONZE ? 2 : 4).setSteamSlotPos(12, 35);
                factory.setupCasing((steamBricked ? "bricked_" : "") + tier.toString());
            } else if (tier == LV) {
                factory = new MachineFactory(tier.toString() + "_" + machineType, tier, MachineBlockEntity::new, recipeType, inputSlots, outputSlots,
                        fluidInputSlots, fluidOutputSlots);
                factory.setupCasing(tier.toString());
            } else {
                continue;
            }
            setup.setup(factory);
        }
    }

    private static void registerMachineTiers(String machineType, MachineRecipeType recipeType, int inputSlots, int outputSlots, int fluidInputSlots,
            int fluidOutputSlots, MachineSetup setup, boolean steamBricked) {
        registerMachineTiers(machineType, recipeType, inputSlots, outputSlots, fluidInputSlots, fluidOutputSlots, setup, steamBricked, true, true);
    }

    private static void registerMachineTiersElectricOnly(String machineType, MachineRecipeType recipeType, int inputSlots, int outputSlots,
            int fluidInputSlots, int fluidOutputSlots, MachineSetup setup) {
        registerMachineTiers(machineType, recipeType, inputSlots, outputSlots, fluidInputSlots, fluidOutputSlots, setup, false, false, false);
    }

    private static int[] ITEM_HATCH_ROWS = new int[] { 1, 2, 2, 3 };
    private static int[] ITEM_HATCH_COLUMNS = new int[] { 1, 1, 2, 3 };
    private static int[] ITEM_HATCH_X = new int[] { 80, 80, 80, 71 };
    private static int[] ITEM_HATCH_Y = new int[] { 40, 30, 21, 21 };
    private static int FLUID_HATCH_X = ITEM_HATCH_X[0];
    private static int FLUID_HATCH_Y = ITEM_HATCH_Y[0];
    private static int[] FLUID_HATCH_BUCKETS = new int[] { 4, 8, 16, 32 };
    private static String[] casing = new String[] { "bronze", "steel", "mv", "hv" };

    private static void registerHatches() {
        int i = 0;
        for (String s : new String[] { "bronze", "steel", "advanced", "turbo" }) {
            MachineTier tier = s.equals("bronze") ? BRONZE : null;
            new MachineFactory(s + "_item_input_hatch", tier, f -> new HatchBlockEntity(f, ITEM_INPUT), null,
                    ITEM_HATCH_ROWS[i] * ITEM_HATCH_COLUMNS[i], 0, 0, 0)
                            .setInputSlotPosition(ITEM_HATCH_X[i], ITEM_HATCH_Y[i], ITEM_HATCH_ROWS[i], ITEM_HATCH_COLUMNS[i])
                            .setupBackground("default.png").setupCasing(casing[i]).setAutoInsert();
            new MachineFactory(s + "_item_output_hatch", tier, f -> new HatchBlockEntity(f, ITEM_OUTPUT), null, 0,
                    ITEM_HATCH_ROWS[i] * ITEM_HATCH_COLUMNS[i], 0, 0)
                            .setOutputSlotPosition(ITEM_HATCH_X[i], ITEM_HATCH_Y[i], ITEM_HATCH_ROWS[i], ITEM_HATCH_COLUMNS[i])
                            .setupBackground("default.png").setupCasing(casing[i]);
            new MachineFactory(s + "_fluid_input_hatch", tier, f -> new HatchBlockEntity(f, FLUID_INPUT), null, 0, 0, 1, 0)
                    .setInputBucketCapacity(FLUID_HATCH_BUCKETS[i]).setInputLiquidSlotPosition(FLUID_HATCH_X, FLUID_HATCH_Y, 1, 1)
                    .setupBackground("default.png").setupCasing(casing[i]).setAutoInsert();
            new MachineFactory(s + "_fluid_output_hatch", tier, f -> new HatchBlockEntity(f, FLUID_OUTPUT), null, 0, 0, 0, 1)
                    .setOutputBucketCapacity(FLUID_HATCH_BUCKETS[i]).setLiquidOutputSlotPosition(FLUID_HATCH_X, FLUID_HATCH_Y, 1, 1)
                    .setupBackground("default.png").setupCasing(casing[i]);
            i++;
        }

        for (CableTier tier : CableTier.values()) {
            new MachineFactory(tier.name + "_energy_input_hatch", null, f -> new EnergyInputHatchBlockEntity(f, tier), null, 0, 0, 0, 0)
                    .setupElectricityBar(76, 39, false).setupBackground("default.png").setupCasing(tier.name);
        }
    }

    private static void registerTransformer(CableTier in, CableTier out, String casing) {
        new MachineFactory(in.toString() + "_" + out.toString() + "_transformer", null, f -> new TransformerBlockEntity(f, in, out), null, 0, 0, 0, 0)
                .setupElectricityBar(76, 39, false).setupBackground("default.png").setupCasing(casing); // TODO: better output texture
    }

    public static void setupRecipes() {
        for (MachineRecipeType recipe : RECIPE_TYPES.keySet()) {
            registerRecipe(recipe);
        }
    }

    private static void registerRecipe(MachineRecipeType type) {
        Registry.register(Registry.RECIPE_TYPE, type.getId(), type);
        Registry.register(Registry.RECIPE_SERIALIZER, type.getId(), type);
    }

    static {
        new MachineFactory("bronze_boiler", BRONZE, SteamBoilerBlockEntity::new, null, 1, 0, 1, 1)
                .setInputBucketCapacity(2 * MITanks.BRONZE.bucketCapacity).setOutputBucketCapacity(2 * MITanks.BRONZE.bucketCapacity)
                .setInputSlotPosition(15, 32, 1, 1).setInputLiquidSlotPosition(50, 32, 1, 1).setLiquidOutputSlotPosition(134, 32, 1, 1)
                .setupProgressBar(176, 0, 15, 51, 14, 14, false, true).setupEfficiencyBar(0, 166, 50, 62, 100, 2).hideEfficiencyTooltip()
                .setupBackground("steam_boiler.png").setupOverlays("boiler", true, false, false).setupCasing("bricked_bronze");
        new SteamMachineFactory("bronze_water_pump", BRONZE, WaterPumpBlockEntity::new, null, 0, 0, 0, 1)
                .setSteamBucketCapacity(2 * MITanks.BRONZE.bucketCapacity).setSteamSlotPos(23, 23)
                .setOutputBucketCapacity(2 * MITanks.BRONZE.bucketCapacity).setLiquidOutputSlotPosition(104, 32, 1, 1)
                .setupBackground("water_pump.png").setupOverlays("pump", true, true, true).setupProgressBar(79, 33, 20, 15, true)
                .setupCasing("bronze");

        new MachineFactory("steel_boiler", STEEL, SteamBoilerBlockEntity::new, null, 1, 0, 1, 1)
                .setInputBucketCapacity(2 * MITanks.STEEL.bucketCapacity).setOutputBucketCapacity(2 * MITanks.STEEL.bucketCapacity)
                .setInputSlotPosition(15, 32, 1, 1).setInputLiquidSlotPosition(50, 32, 1, 1).setLiquidOutputSlotPosition(134, 32, 1, 1)
                .setupProgressBar(176, 0, 15, 51, 14, 14, false, true).setupEfficiencyBar(0, 166, 50, 62, 100, 2).hideEfficiencyTooltip()
                .setupBackground("steam_boiler.png").setupOverlays("boiler", true, false, false).setupCasing("bricked_steel");

        new SteamMachineFactory("steel_water_pump", STEEL, WaterPumpBlockEntity::new, null, 0, 0, 0, 1)
                .setSteamBucketCapacity(2 * MITanks.STEEL.bucketCapacity).setSteamSlotPos(23, 23)
                .setOutputBucketCapacity(2 * MITanks.STEEL.bucketCapacity).setLiquidOutputSlotPosition(104, 32, 1, 1)
                .setupBackground("water_pump.png").setupOverlays("pump", true, true, true).setupProgressBar(79, 33, 20, 15, true)
                .setupCasing("steel");
        new MachineFactory("lv_water_pump", LV, WaterPumpBlockEntity::new, null, 0, 0, 0, 1).setupElectricityBar(18, 34)
                .setOutputBucketCapacity(4 * MITanks.STEEL.bucketCapacity).setLiquidOutputSlotPosition(104, 32, 1, 1)
                .setupBackground("water_pump.png").setupOverlays("pump", true, true, true).setupProgressBar(79, 33, 20, 15, true).setupCasing("lv");
        registerMachineTiers("compressor", RECIPE_COMPRESSOR, 1, 1, 0, 0, MIMachines::setupCompressor, false);
        registerMachineTiers("cutting_machine", RECIPE_CUTTING_MACHINE, 1, 1, 1, 0, MIMachines::setupCuttingMachine, false);
        // registerMachineTiers("fluid_extractor", RECIPE_FLUID_EXTRACTOR, 1, 0, 0, 1,
        // MIMachines::setupFluidExtractor, false);
        registerMachineTiers("furnace", RECIPE_FURNACE, 1, 1, 0, 0, MIMachines::setupFurnace, true);
        registerMachineTiers("macerator", RECIPE_MACERATOR, 1, 4, 0, 0, MIMachines::setupMacerator, false);
        registerMachineTiers("mixer", RECIPE_MIXER, 4, 2, 2, 2, MIMachines::setupMixer, false);
        registerMachineTiers("packer", RECIPE_PACKER, 2, 2, 0, 0, MIMachines::setupPacker, false, false, true);
        registerMachineTiers("wiremill", RECIPE_WIREMILL, 1, 1, 0, 0, MIMachines::setupWiremill, false, false, true);

        registerMachineTiersElectricOnly("assembler", RECIPE_ASSEMBLER, 9, 3, 1, 0, MIMachines::setupAssembler);
        registerMachineTiersElectricOnly("centrifuge", RECIPE_CENTRIFUGE, 1, 4, 1, 4, MIMachines::setupCentrifuge);
        registerMachineTiersElectricOnly("chemical_reactor", RECIPE_CHEMICAL_REACTOR, 3, 3, 3, 3, MIMachines::setupChemicalReactor);
        registerMachineTiersElectricOnly("distillery", RECIPE_DISTILLERY, 0, 0, 1, 1, MIMachines::setupDistillery);
        registerMachineTiersElectricOnly("electrolyzer", RECIPE_ELECTROLYZER, 1, 4, 1, 4, MIMachines::setupElectrolyzer);
        registerMachineTiersElectricOnly("polarizer", RECIPE_POLARIZER, 1, 1, 0, 0, MIMachines::setupPolarizer);

        new SteamMachineFactory("coke_oven", null, f -> new MultiblockMachineBlockEntity(f, Collections.singletonList(COKE_OVEN_SHAPE)),
                RECIPE_COKE_OVEN, 1, 1, 0, 0).setInputSlotPosition(56, 35, 1, 1).setOutputSlotPosition(102, 35, 1, 1)
                        .setupProgressBar(76, 35, 22, 15, true).setupBackground("steam_furnace.png").setupOverlays("coke_oven", true, false, false)
                        .setupCasing("bricks");
        new SteamMachineFactory("steam_blast_furnace", null, f -> new MultiblockMachineBlockEntity(f, Collections.singletonList(BLAST_FURNACE_SHAPE)),
                RECIPE_BLAST_FURNACE, 2, 1, 1, 1).setInputSlotPosition(56, 35, 1, 2).setOutputSlotPosition(102, 35, 1, 1)
                        .setInputLiquidSlotPosition(36, 35, 1, 1).setLiquidOutputSlotPosition(122, 35, 1, 1).setupProgressBar(76, 35, 22, 15, true)
                        .setupBackground("steam_furnace.png").setupOverlays("steam_blast_furnace", true, false, false).setupCasing("firebricks");
        new SteamMachineFactory("quarry", null, f -> new MultiblockMachineBlockEntity(f, Collections.singletonList(STEAM_QUARRY_SHAPE)),
                RECIPE_QUARRY, 1, 16, 0, 0).setInputSlotPosition(56, 35, 1, 1).setOutputSlotPosition(102, 35, 4, 4)
                        .setupProgressBar(76, 35, 22, 15, true).setupBackground("steam_furnace.png").setupOverlays("quarry", true, false, false)
                        .setupCasing("steel");
        new MachineFactory("electric_quarry", UNLIMITED, f -> new MultiblockMachineBlockEntity(f, Collections.singletonList(ELECTRIC_QUARRY_SHAPE)),
                RECIPE_QUARRY, 1, 16, 0, 0).setInputSlotPosition(56, 35, 1, 1).setOutputSlotPosition(102, 35, 4, 4)
                        .setupProgressBar(76, 35, 22, 15, true).setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2)
                        .setupOverlays("quarry", true, false, false).setupCasing("steel");
        LARGE_STEAM_BOILER = new MachineFactory("large_steam_boiler", null,
                f -> new LargeSteamBoilerBlockEntity(f, Collections.singletonList(LARGE_BOILER_SHAPE)), null, 1, 0, 1, 1)
                        .setupProgressBar(176, 0, 15, 51, 14, 14, false, true).setupEfficiencyBar(0, 166, 50, 62, 100, 2).hideEfficiencyTooltip()
                        .setupBackground("steam_boiler.png").setupOverlays("large_boiler", true, false, false).setupCasing("bronze_plated_bricks");
        ELECTRIC_BLAST_FURNACE = new MachineFactory("electric_blast_furnace", UNLIMITED, ElectricBlastFurnaceBlockEntity::new, RECIPE_BLAST_FURNACE,
                2, 1, 1, 1).setInputSlotPosition(56, 35, 1, 2).setOutputSlotPosition(102, 35, 1, 1).setInputLiquidSlotPosition(36, 35, 1, 1)
                        .setLiquidOutputSlotPosition(122, 35, 1, 1).setupProgressBar(76, 35, 22, 15, true).setupBackground("steam_furnace.png")
                        .setupEfficiencyBar(0, 166, 38, 62, 100, 2).setupOverlays("electric_blast_furnace", true, false, false)
                        .setupCasing("heatproof");

        new MachineFactory("oil_drilling_rig", UNLIMITED, f -> new MultiblockMachineBlockEntity(f, Collections.singletonList(OIL_DRILLING_RIG_SHAPE)),
                RECIPE_OIL_DRILLING_RIG, 1, 0, 0, 1).setInputSlotPosition(56, 35, 1, 1).setLiquidOutputSlotPosition(102, 35, 1, 1)
                        .setupProgressBar(76, 35, 22, 15, true).setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2)
                        .setupOverlays("oil_drilling_rig", true, false, false).setupCasing("steel");

        new MachineFactory("vacuum_freezer", UNLIMITED, f -> new MultiblockMachineBlockEntity(f, Collections.singletonList(VACUUM_FREEZER_SHAPE)),
                RECIPE_VACUUM_FREEZER, 2, 1, 1, 1).setInputSlotPosition(56, 35, 1, 2).setOutputSlotPosition(102, 35, 1, 1)
                        .setInputLiquidSlotPosition(36, 35, 1, 1).setLiquidOutputSlotPosition(122, 35, 1, 1).setupProgressBar(76, 35, 22, 15, true)
                        .setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2)
                        .setupOverlays("vacuum_freezer", true, false, false).setupCasing("frostproof");

        new MachineFactory("nuclear_reactor", null, f -> new NuclearReactorBlockEntity(f, Collections.singletonList(NUCLEAR_REACTOR_SHAPE)), null, 64,
                0, 0, 0).setInputSlotPosition(15, 20, 8, 8).setupBackground("nuclear.png", 176, 256).setInventoryPos(8, 174)
                        .setupOverlays("vacuum_freezer", true, false, false).setupCasing("nuclear")
                        .setInsertPredicate(stack -> stack.getItem() instanceof MINuclearItem);

        new MachineFactory("distillation_tower", UNLIMITED, DistillationTowerBlockEntity::new, RECIPE_DISTILLATION_TOWER, 0, 0, 1, 8)
                .setInputLiquidSlotPosition(56, 35, 1, 1).setLiquidOutputSlotPosition(102, 35, 1, 8).setupProgressBar(76, 35, 22, 15, true)
                .setupBackground("steam_furnace.png").setupEfficiencyBar(0, 166, 38, 62, 100, 2)
                .setupOverlays("distillation_tower", true, false, false).setupCasing("clean");

        registerHatches();

        new MachineFactory("lv_steam_turbine", null, f -> new SteamTurbineBlockEntity(f, CableTier.LV), null, 0, 0, 1, 0)
                .setInputLiquidSlotPosition(23, 23, 1, 1).setupElectricityBar(76, 39, false).setupBackground("default.png").setupCasing("lv") // TODO:
                                                                                                                                              // custom
                                                                                                                                              // electric
                                                                                                                                              // output
                .setupOverlays("steam_turbine", true, false, false);
        new MachineFactory("mv_steam_turbine", null, f -> new SteamTurbineBlockEntity(f, CableTier.MV), null, 0, 0, 1, 0)
                .setInputLiquidSlotPosition(23, 23, 1, 1).setupElectricityBar(76, 39, false).setupBackground("default.png").setupCasing("mv") // TODO:
                                                                                                                                              // custom
                                                                                                                                              // electric
                                                                                                                                              // output
                .setupOverlays("steam_turbine", true, false, false);
        DIESEL_GENERATOR = new MachineFactory("diesel_generator", null, f -> new DieselGeneratorBlockEntity(f, CableTier.MV), null, 0, 0, 1, 0)
                .setInputLiquidSlotPosition(23, 23, 1, 1).setupElectricityBar(76, 39, false).setupBackground("default.png").setupCasing("mv") // TODO:
                                                                                                                                              // custom
                                                                                                                                              // electric
                                                                                                                                              // output
                .setupOverlays("diesel_generator", false, false, true);

        registerTransformer(CableTier.LV, CableTier.MV, "lv");
        registerTransformer(CableTier.MV, CableTier.LV, "lv");
        registerTransformer(CableTier.MV, CableTier.HV, "mv");
        registerTransformer(CableTier.HV, CableTier.MV, "mv");

        new MachineFactory("configurable_chest", null, ConfigurableChestBlockEntity::new, null, 21, 0, 0, 0).setInputSlotPosition(16, 26, 7, 3)
                .setInventoryPos(8, 96).setupBackground("configurable_chest.png", 176, 178).setupCasing("steel_crate");
    }
}

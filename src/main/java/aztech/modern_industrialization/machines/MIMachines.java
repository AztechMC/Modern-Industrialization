package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.impl.SteamMachineFactory;
import aztech.modern_industrialization.machines.impl.multiblock.*;
import aztech.modern_industrialization.machines.recipe.FurnaceRecipeProxy;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.special.SteamBoilerBlockEntity;
import aztech.modern_industrialization.machines.special.WaterPumpBlockEntity;
import net.minecraft.util.registry.Registry;

import java.util.*;

import static aztech.modern_industrialization.machines.impl.MachineTier.*;
import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.*;
import static aztech.modern_industrialization.machines.impl.multiblock.MultiblockShapes.*;

public class MIMachines {
    // Recipe
    public static final Map<MachineRecipeType, RecipeInfo> RECIPE_TYPES = new HashMap<>();
    public static final List<MachineFactory> WORKSTATIONS_FURNACES = new ArrayList<>();

    private static MachineRecipeType createRecipeType(String kind) {
        MachineRecipeType type = new MachineRecipeType(new MIIdentifier(kind));
        RECIPE_TYPES.put(type, new RecipeInfo());
        return type;
    }

    public static class RecipeInfo {
        public final List<MachineFactory> factories = new ArrayList<>();
    }

    public static final MachineRecipeType RECIPE_COMPRESSOR = createRecipeType("compressor").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_CUTTING_MACHINE = createRecipeType("cutting_machine").withItemInputs().withFluidInputs().withItemOutputs();
    //public static final MachineRecipeType RECIPE_FLUID_EXTRACTOR = createRecipeType("fluid_extractor").withItemInputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_FURNACE = new FurnaceRecipeProxy(null);
    public static final MachineRecipeType RECIPE_MACERATOR = createRecipeType("macerator").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_MIXER = createRecipeType("mixer").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_COKE_OVEN = createRecipeType("coke_oven").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_BLAST_FURNACE = createRecipeType("blast_furnace").withItemInputs().withItemOutputs();

    // Shapes
    public static MultiblockShape COKE_OVEN_SHAPE;

    static {
        COKE_OVEN_SHAPE = new MultiblockShape();
        MultiblockShape.Entry firebricks = MultiblockShapes.block(MIBlock.BLOCK_FIRE_CLAY_BRICKS);
        // two layers of pillars
        for(int i = -3; i <= -2; i++) {
            COKE_OVEN_SHAPE.addEntry(1, i, 0, firebricks);
            COKE_OVEN_SHAPE.addEntry(1, i, 2, firebricks);
            COKE_OVEN_SHAPE.addEntry(-1, i, 0, firebricks);
            COKE_OVEN_SHAPE.addEntry(-1, i, 2, firebricks);
        }
        // firebricks + fluid input hatch in center
        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
                COKE_OVEN_SHAPE.addEntry(i, -1, j+1, i == 0 && j == 0 ? MultiblockShapes.hatch(HATCH_FLAG_FLUID_INPUT) : firebricks);
            }
        }
        // corners of main layer
        COKE_OVEN_SHAPE.addEntry(1, 0, 0, firebricks);
        COKE_OVEN_SHAPE.addEntry(1, 0, 2, firebricks);
        COKE_OVEN_SHAPE.addEntry(-1, 0, 0, firebricks);
        COKE_OVEN_SHAPE.addEntry(-1, 0, 2, firebricks);
        // main layer hatches
        MultiblockShape.Entry optionalItem = MultiblockShapes.or(firebricks, MultiblockShapes.hatch(HATCH_FLAG_ITEM_INPUT | HATCH_FLAG_ITEM_OUTPUT));
        COKE_OVEN_SHAPE.addEntry(1, 0, 1, optionalItem);
        COKE_OVEN_SHAPE.addEntry(0, 0, 2, optionalItem);
        COKE_OVEN_SHAPE.addEntry(-1, 0, 1, optionalItem);
        // top layer firebricks + optional hatch in the center
        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
                COKE_OVEN_SHAPE.addEntry(i, 1, j+1, i == 0 && j == 0 ? optionalItem : firebricks);
            }
        }
    }


    public static MachineFactory setupCompressor(MachineFactory factory) {
        return factory
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupOverlays("compressor", true, true, true);
    }

    public static MachineFactory setupCuttingMachine(MachineFactory factory) {
        return factory
                .setInputLiquidSlotPosition(36, 45, 1, 1)
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupOverlays("cutting_machine", true, false, false);
    }

    public static MachineFactory setupFluidExtractor(MachineFactory factory) {
        return factory.setInputSlotPosition(56, 45, 1, 1).setLiquidOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupOverlays("fluid_extractor", true, true, true);
    }

    public static MachineFactory setupFurnace(MachineFactory factory) {
        return factory
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupOverlays("furnace", true, false, false);
    }

    public static MachineFactory setupMacerator(MachineFactory factory) {
        return factory
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 2, 2)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupOverlays("macerator", true, false, true);
    }

    public static MachineFactory setupMixer(MachineFactory factory) {
        return factory
                .setInputSlotPosition(52, 37, 2, 2).setOutputSlotPosition(119, 37, 1, 2)
                .setupProgressBar(92, 46, 22, 15, true).setupBackground("steam_mixer.png")
                .setupOverlays("mixer", true, true, true);
    }

    @FunctionalInterface
    private interface MachineSetup {
        MachineFactory setup(MachineFactory factory);
    }

    private static void registerMachineTiers(String machineType, MachineRecipeType recipeType, int inputSlots, int outputSlots, int fluidInputSlots, int fluidOutputSlots, MachineSetup setup, boolean steamBricked) {
        for (MachineTier tier : MachineTier.values()) {
            MachineFactory factory;
            if (tier.isSteam()) {
                factory = new SteamMachineFactory(tier.toString() + "_" + machineType, tier, MachineBlockEntity::new, recipeType, inputSlots, outputSlots, fluidInputSlots, fluidOutputSlots)
                        .setSteamBucketCapacity(tier == BRONZE ? 2 : 4).setSteamSlotPos(23, 23);
                factory.setupCasing((steamBricked ? "bricked_" : "") + tier.toString());
            } else {
                return;
                // TODO: electric machines
            }
            setup.setup(factory);
        }
    }

    private static int[] ITEM_HATCH_ROWS = new int[] {1, 2};
    private static int[] ITEM_HATCH_COLUMNS = new int[] {1, 1};
    private static int[] ITEM_HATCH_X = new int[] {80, 80};
    private static int[] ITEM_HATCH_Y = new int[] {40, 30};
    private static int FLUID_HATCH_X = ITEM_HATCH_X[0];
    private static int FLUID_HATCH_Y = ITEM_HATCH_Y[0];
    private static int[] FLUID_HATCH_BUCKETS = new int[] {4, 8};
    private static void registerHatches() {
        int i = 0;
        for(MachineTier tier : MachineTier.values()) {
            if(!tier.isSteam()) continue; // TODO: hatches for electric tiers
            new MachineFactory(tier.toString() + "_item_input_hatch", tier, (f, t) -> new HatchBlockEntity(f, ITEM_INPUT), null, ITEM_HATCH_ROWS[i] * ITEM_HATCH_COLUMNS[i], 0, 0, 0)
                    .setInputSlotPosition(ITEM_HATCH_X[i], ITEM_HATCH_Y[i], ITEM_HATCH_ROWS[i], ITEM_HATCH_COLUMNS[i])
                    .setupBackground("default.png")
                    .setupCasing(tier.toString());
            new MachineFactory(tier.toString() + "_item_output_hatch", tier, (f, t) -> new HatchBlockEntity(f, ITEM_OUTPUT), null, 0, ITEM_HATCH_ROWS[i] * ITEM_HATCH_COLUMNS[i], 0, 0)
                    .setOutputSlotPosition(ITEM_HATCH_X[i], ITEM_HATCH_Y[i], ITEM_HATCH_ROWS[i], ITEM_HATCH_COLUMNS[i])
                    .setupBackground("default.png")
                    .setupCasing(tier.toString());
            new MachineFactory(tier.toString() + "_fluid_input_hatch", tier, (f, t) -> new HatchBlockEntity(f, FLUID_INPUT), null, 0, 0, 1, 0)
                    .setInputBucketCapacity(FLUID_HATCH_BUCKETS[i])
                    .setInputLiquidSlotPosition(FLUID_HATCH_X, FLUID_HATCH_Y, 1, 1)
                    .setupBackground("default.png")
                    .setupCasing(tier.toString());
            new MachineFactory(tier.toString() + "_fluid_output_hatch", tier, (f, t) -> new HatchBlockEntity(f, FLUID_OUTPUT), null, 0, 0, 0, 1)
                    .setOutputBucketCapacity(FLUID_HATCH_BUCKETS[i])
                    .setLiquidOutputSlotPosition(FLUID_HATCH_X, FLUID_HATCH_Y, 1, 1)
                    .setupBackground("default.png")
                    .setupCasing(tier.toString());
            i++;
        }
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
                .setInputBucketCapacity(2* MITanks.BRONZE.bucketCapacity).setOutputBucketCapacity(2*MITanks.BRONZE.bucketCapacity)
                .setInputSlotPosition(15, 32, 1, 1)
                .setInputLiquidSlotPosition(50, 32, 1, 1).setLiquidOutputSlotPosition(134, 32, 1, 1)
                .setupProgressBar(176, 0, 15, 51, 14, 14, false, true)
                .setupEfficiencyBar(0, 166, 50, 62, 100, 2)
                .setupBackground("steam_boiler.png")
                .setupOverlays("boiler", true, false, false)
                .setupCasing("bricked_bronze");
        new SteamMachineFactory("bronze_water_pump", BRONZE, WaterPumpBlockEntity::new, null, 0, 0, 0, 1)
                .setSteamBucketCapacity(2*MITanks.BRONZE.bucketCapacity).setSteamSlotPos(23, 23)
                .setOutputBucketCapacity(2*MITanks.BRONZE.bucketCapacity)
                .setLiquidOutputSlotPosition(104, 32, 1, 1)
                .setupBackground("water_pump.png")
                .setupOverlays("pump", true, true, true)
                .setupProgressBar( 79, 33, 20, 15, true)
                .setupCasing("bronze");

        new MachineFactory("steel_boiler", STEEL, SteamBoilerBlockEntity::new, null, 1, 0, 1, 1)
                .setInputBucketCapacity(2*MITanks.STEEL.bucketCapacity).setOutputBucketCapacity(2*MITanks.STEEL.bucketCapacity)
                .setInputSlotPosition(15, 32, 1, 1)
                .setInputLiquidSlotPosition(50, 32, 1, 1).setLiquidOutputSlotPosition(134, 32, 1, 1)
                .setupProgressBar(176, 0, 15, 51, 14, 14, false, true)
                .setupEfficiencyBar(0, 166, 50, 62, 100, 2)
                .setupBackground("steam_boiler.png")
                .setupOverlays("boiler", true, false, false)
                .setupCasing("bricked_steel");

        new SteamMachineFactory("steel_water_pump", STEEL, WaterPumpBlockEntity::new, null, 0, 0, 0, 1)
                .setSteamBucketCapacity(2*MITanks.STEEL.bucketCapacity).setSteamSlotPos(23, 23)
                .setOutputBucketCapacity(2*MITanks.STEEL.bucketCapacity)
                .setLiquidOutputSlotPosition(104, 32, 1, 1)
                .setupBackground("water_pump.png")
                .setupOverlays("pump", true, true, true)
                .setupProgressBar( 79, 33, 20, 15, true)
                .setupCasing("steel");
        registerMachineTiers("compressor", RECIPE_COMPRESSOR, 1, 1, 0, 0, MIMachines::setupCompressor, false);
        registerMachineTiers("cutting_machine", RECIPE_CUTTING_MACHINE, 1, 1, 1, 0, MIMachines::setupCuttingMachine, false);
        //registerMachineTiers("fluid_extractor", RECIPE_FLUID_EXTRACTOR, 1, 0, 0, 1, MIMachines::setupFluidExtractor, false);
        registerMachineTiers("furnace", RECIPE_FURNACE, 1, 1, 0, 0, MIMachines::setupFurnace, true);
        registerMachineTiers("macerator", RECIPE_MACERATOR, 1, 4, 0, 0, MIMachines::setupMacerator, false);
        registerMachineTiers("mixer", RECIPE_MIXER, 4, 2, 0, 0, MIMachines::setupMixer, false);
        new SteamMachineFactory("coke_oven", BRONZE, (f, t) -> new MultiblockMachineBlockEntity(f, t, COKE_OVEN_SHAPE), RECIPE_COKE_OVEN, 1, 1, 0, 0)
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupOverlays("coke_oven", true, false, false)
                .setupCasing("firebricks")
        ;
        new SteamMachineFactory("steam_blast_furnace", BRONZE, (f, t) -> new MultiblockMachineBlockEntity(f, t, COKE_OVEN_SHAPE), RECIPE_BLAST_FURNACE, 1, 1, 0, 0)
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupOverlays("steam_blast_furnace", true, false, false)
                .setupCasing("firebricks")
        ;
        registerHatches();
    }
}

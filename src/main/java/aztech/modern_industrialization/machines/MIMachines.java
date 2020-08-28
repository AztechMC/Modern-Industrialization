package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.SteamMachineFactory;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.special.SteamBoilerBlockEntity;
import aztech.modern_industrialization.machines.special.WaterPumpBlockEntity;
import net.minecraft.util.registry.Registry;

public class MIMachines {
    // Recipe
    public static final MachineRecipeType RECIPE_COMPRESSOR = new MachineRecipeType().withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_FLUID_EXTRACTOR = new MachineRecipeType().withItemInputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_FURNACE = new MachineRecipeType().withItemInputs().withItemOutputs(); // TODO: import from vanilla
    public static final MachineRecipeType RECIPE_MACERATOR = new MachineRecipeType().withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_MIXER = new MachineRecipeType().withItemInputs().withItemOutputs();

    // Bronze
    public static MachineFactory BRONZE_BOILER;
    public static MachineFactory BRONZE_WATER_PUMP;

    public static MachineFactory BRONZE_COMPRESSOR;
    public static MachineFactory BRONZE_FLUID_EXTRACTOR;
    public static MachineFactory BRONZE_FURNACE;
    public static MachineFactory BRONZE_MACERATOR;
    public static MachineFactory BRONZE_MIXER;

    static {
        BRONZE_BOILER = new MachineFactory("bronze_boiler", () -> new SteamBoilerBlockEntity(BRONZE_BOILER, null), 1, 0, 1, 1)
                .setInputBucketCapacity(64).setOutputBucketCapacity(64)
                .setInputSlotPosition(15, 32, 1, 1)
                .setInputLiquidSlotPosition(50, 32, 1, 1).setLiquidOutputSlotPosition(134, 32, 1, 1)
                .setupProgressBar(176, 0, 15, 51, 14, 14, false, true)
                .setupEfficiencyBar(0, 166, 50, 62, 100, 2)
                .setupBackground("steam_boiler.png")
                .setupCasing("bricked_bronze")
                .setupOverlays("boiler", true, false, false);
        BRONZE_WATER_PUMP = new SteamMachineFactory("bronze_water_pump", () -> new WaterPumpBlockEntity(BRONZE_WATER_PUMP, null), 0, 0, 0, 1)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setOutputBucketCapacity(64)
                .setLiquidOutputSlotPosition(104, 32, 1, 1)
                .setupBackground("water_pump.png")
                .setupCasing("bronze")
                .setupOverlays("pump", true, true, true);
        BRONZE_COMPRESSOR = new SteamMachineFactory("bronze_compressor", () -> new MachineBlockEntity(BRONZE_COMPRESSOR, RECIPE_COMPRESSOR), 1, 1)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true)
                .setupCasing("bronze")
                .setupOverlays("compressor", true, true, true);
        BRONZE_FLUID_EXTRACTOR = new SteamMachineFactory("bronze_fluid_extractor", () -> new MachineBlockEntity(BRONZE_FLUID_EXTRACTOR, RECIPE_FLUID_EXTRACTOR), 1, 0, 0, 1)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 1, 1).setLiquidOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupCasing("bronze")
                .setupOverlays("fluid_extractor", true, true, true);
        BRONZE_FURNACE = new SteamMachineFactory("bronze_furnace", () -> new MachineBlockEntity(BRONZE_FURNACE, RECIPE_FURNACE), 1, 1)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true)
                .setupCasing("bricked_bronze")
                .setupOverlays("furnace", true, false, false);
        BRONZE_MACERATOR = new SteamMachineFactory("bronze_macerator", () -> new MachineBlockEntity(BRONZE_MACERATOR, RECIPE_MACERATOR), 1, 4)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 2, 2)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupCasing("bronze")
                .setupOverlays("macerator", true, false, true);
        BRONZE_MIXER = new SteamMachineFactory("bronze_mixer", () -> new MachineBlockEntity(BRONZE_MIXER, RECIPE_MIXER), 4, 2)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 2, 2).setOutputSlotPosition(102, 45, 1, 2)
                .setupCasing("bronze")
                .setupOverlays("mixer", true, true, true);
    }

    public static void setupRecipes() {
        registerRecipe("compressor", RECIPE_COMPRESSOR);
        registerRecipe("fluid_extractor", RECIPE_FLUID_EXTRACTOR);
        registerRecipe("furnace", RECIPE_FURNACE);
        registerRecipe("macerator", RECIPE_MACERATOR);
        registerRecipe("mixer", RECIPE_MIXER);
    }

    private static void registerRecipe(String name, MachineRecipeType type) {
        Registry.register(Registry.RECIPE_TYPE, new MIIdentifier(name), type);
        Registry.register(Registry.RECIPE_SERIALIZER, new MIIdentifier(name), type);
    }
}

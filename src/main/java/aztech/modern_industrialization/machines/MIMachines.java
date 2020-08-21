package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.SteamMachineFactory;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
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
    public static MachineFactory BRONZE_PUMP;

    public static MachineFactory BRONZE_COMPRESSOR;
    public static MachineFactory BRONZE_FLUID_EXTRACTOR;
    public static MachineFactory BRONZE_FURNACE;
    public static MachineFactory BRONZE_MACERATOR;
    public static MachineFactory BRONZE_MIXER;

    static {
        BRONZE_COMPRESSOR = new SteamMachineFactory("bronze_compressor", () -> new MachineBlockEntity(BRONZE_COMPRESSOR, RECIPE_COMPRESSOR), 1, 1)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true);
        BRONZE_FLUID_EXTRACTOR = new SteamMachineFactory("bronze_fluid_extractor", () -> new MachineBlockEntity(BRONZE_FLUID_EXTRACTOR, RECIPE_FLUID_EXTRACTOR), 1, 0, 0, 1)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 1, 1).setLiquidOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png");
        BRONZE_FURNACE = new SteamMachineFactory("bronze_furnace", () -> new MachineBlockEntity(BRONZE_FURNACE, RECIPE_FURNACE), 1, 1)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true);
        BRONZE_MACERATOR = new SteamMachineFactory("bronze_macerator", () -> new MachineBlockEntity(BRONZE_MACERATOR, RECIPE_MACERATOR), 1, 4)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 2, 2)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png");
        BRONZE_MIXER = new SteamMachineFactory("bronze_mixer", () -> new MachineBlockEntity(BRONZE_MIXER, RECIPE_MIXER), 4, 2)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setInputSlotPosition(56, 45, 2, 2).setOutputSlotPosition(102, 45, 1, 2);
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

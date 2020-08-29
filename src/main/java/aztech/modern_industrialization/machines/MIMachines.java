package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.impl.SteamMachineFactory;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.special.SteamBoilerBlockEntity;
import aztech.modern_industrialization.machines.special.WaterPumpBlockEntity;
import net.minecraft.util.registry.Registry;

import java.util.*;

import static aztech.modern_industrialization.machines.impl.MachineTier.*;

public class MIMachines {
    // Recipe
    public static final Map<MachineRecipeType, RecipeInfo> RECIPE_TYPES = new HashMap<>();
    private static MachineRecipeType createRecipeType(String kind) {
        MachineRecipeType type = new MachineRecipeType(new MIIdentifier(kind));
        RECIPE_TYPES.put(type, new RecipeInfo());
        return type;
    }

    public static class RecipeInfo {
        public final List<MachineFactory> factories = new ArrayList<>();
    }

    public static final MachineRecipeType RECIPE_COMPRESSOR = createRecipeType("compressor").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_CUTTING_SAW = createRecipeType("cutting_saw").withItemInputs().withFluidInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_FLUID_EXTRACTOR = createRecipeType("fluid_extractor").withItemInputs().withFluidOutputs();
    public static final MachineRecipeType RECIPE_FURNACE = createRecipeType("furnace").withItemInputs().withItemOutputs(); // TODO: import from vanilla
    public static final MachineRecipeType RECIPE_MACERATOR = createRecipeType("macerator").withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_MIXER = createRecipeType("mixer").withItemInputs().withItemOutputs();

    // Bronze
    public static MachineFactory BRONZE_BOILER;
    public static MachineFactory BRONZE_WATER_PUMP;

    static {
        BRONZE_BOILER = new MachineFactory("bronze_boiler", BRONZE, SteamBoilerBlockEntity::new, null, 1, 0, 1, 1)
                .setInputBucketCapacity(64).setOutputBucketCapacity(64)
                .setInputSlotPosition(15, 32, 1, 1)
                .setInputLiquidSlotPosition(50, 32, 1, 1).setLiquidOutputSlotPosition(134, 32, 1, 1)
                .setupProgressBar(176, 0, 15, 51, 14, 14, false, true)
                .setupEfficiencyBar(0, 166, 50, 62, 100, 2)
                .setupBackground("steam_boiler.png")
                .setupOverlays("boiler", true, false, false)
                .setupCasing("bricked_bronze");
        BRONZE_WATER_PUMP = new SteamMachineFactory("bronze_water_pump", BRONZE, WaterPumpBlockEntity::new, null, 0, 0, 0, 1)
                .setSteamBucketCapacity(64).setSteamSlotPos(23, 23)
                .setOutputBucketCapacity(64)
                .setLiquidOutputSlotPosition(104, 32, 1, 1)
                .setupBackground("water_pump.png")
                .setupOverlays("pump", true, true, true)
                .setupCasing("bronze");
        registerMachineTiers("compressor", RECIPE_COMPRESSOR, 1, 1, 0, 0, MIMachines::setupCompressor, false);
        registerMachineTiers("cutting_saw", RECIPE_CUTTING_SAW, 1, 1, 1, 0, MIMachines::setupCuttingSaw, false);
        registerMachineTiers("fluid_extractor", RECIPE_FLUID_EXTRACTOR, 1, 0, 0, 1, MIMachines::setupFluidExtractor, false);
        registerMachineTiers("furnace", RECIPE_FURNACE, 1, 1, 0, 0, MIMachines::setupFurnace, true);
        registerMachineTiers("macerator", RECIPE_MACERATOR, 1, 4, 0, 0, MIMachines::setupMacerator, false);
        registerMachineTiers("mixer", RECIPE_MIXER, 4, 2, 0, 0, MIMachines::setupMixer, false);
    }

    public static MachineFactory setupCompressor(MachineFactory factory) {
        return factory
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true)
                .setupOverlays("compressor", true, true, true);
    }

    public static MachineFactory setupCuttingSaw(MachineFactory factory) {
        return factory
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true)
                .setupOverlays("cutting_saw", true, false, false);
    }

    public static MachineFactory setupFluidExtractor(MachineFactory factory) {
        return factory.setInputSlotPosition(56, 45, 1, 1).setLiquidOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true).setupBackground("steam_furnace.png")
                .setupOverlays("fluid_extractor", true, true, true);
    }

    public static MachineFactory setupFurnace(MachineFactory factory) {
        return factory
                .setInputSlotPosition(56, 45, 1, 1).setOutputSlotPosition(102, 45, 1, 1)
                .setupProgressBar(76, 45, 22, 15, true)
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
                .setInputSlotPosition(56, 45, 2, 2).setOutputSlotPosition(102, 45, 1, 2)
                .setupOverlays("mixer", true, true, true);
    }

    @FunctionalInterface
    private interface MachineSetup {
        MachineFactory setup(MachineFactory factory);
    }

    private static void registerMachineTiers(String machineType, MachineRecipeType recipeType, int inputSlots, int outputSlots, int fluidInputSlots, int fluidOutputSlots, MachineSetup setup, boolean steamBricked) {
        for(MachineTier tier : MachineTier.values()) {
            MachineFactory factory;
            if(tier.isSteam()) {
                factory = new SteamMachineFactory(tier.toString() + "_" + machineType, tier, MachineBlockEntity::new, recipeType, inputSlots, outputSlots, fluidInputSlots, fluidOutputSlots)
                    .setSteamBucketCapacity(2).setSteamSlotPos(23, 23);
                factory.setupCasing((steamBricked ? "bricked_" : "") + tier.toString());
            } else {
                return;
                // TODO: electric machines
            }
            setup.setup(factory);
        }
    }

    public static void setupRecipes() {
        for(MachineRecipeType recipe : RECIPE_TYPES.keySet()) {
            registerRecipe(recipe);
        }
    }

    private static void registerRecipe(MachineRecipeType type) {
        Registry.register(Registry.RECIPE_TYPE, type.getId(), type);
        Registry.register(Registry.RECIPE_SERIALIZER, type.getId(), type);
    }
}

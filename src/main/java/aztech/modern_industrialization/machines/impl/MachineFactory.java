package aztech.modern_industrialization.machines.impl;

import static aztech.modern_industrialization.machines.impl.MachineSlotType.*;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.MIMachines;
import aztech.modern_industrialization.machines.recipe.FurnaceRecipeProxy;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;

public class MachineFactory {

    public MachineBlock block;
    public BlockItem item;
    public BlockEntityType blockEntityType;
    public final MachineTier tier;
    public final MachineRecipeType recipeType;

    public MachineModel machineModel;
    private String casing; // example: "bricked_bronze"
    private String machineType; // example: "boiler"
    private boolean frontOverlay;
    private boolean sideOverlay;
    private boolean topOverlay;

    public final Supplier<MachineBlockEntity> blockEntityConstructor;

    private String machineID;
    private static Map<String, MachineFactory> map = new TreeMap<String, MachineFactory>();

    private int inputSlots;
    private int outputSlots;

    private int liquidInputSlots;
    private int liquidOutputSlots;

    private int slots;

    private String translationKey = "machine_recipe.default";

    // slot index -> inputSlots next liquidInputSlots next outputSlots next
    // liquidOutputSlots

    private int[] slotPositionsX;
    private int[] slotPositionsY;

    private int inventoryPosX = 8;
    private int inventoryPosY = 84;

    private boolean hasProgressBar = false;
    private int progressBarX;
    private int progressBarY;
    private int progressBarDrawX;
    private int progressBarDrawY;
    private int progressBarSizeX;
    private int progressBarSizeY;

    private boolean progressBarHorizontal;
    private boolean progressBarFlipped;

    boolean hasEfficiencyBar = false;
    int efficiencyBarX;
    int efficiencyBarY;
    int efficiencyBarDrawX;
    int efficiencyBarDrawY;
    int efficiencyBarSizeX;
    int efficiencyBarSizeY;
    boolean efficiencyBarDrawTooltip = true;

    boolean hasEnergyBar = false;
    int electricityBarX;
    int electricityBarY;

    private int inputBucketCapacity = 16;
    private int outputBucketCapacity = 16;

    private MIIdentifier backgroundIdentifier = new MIIdentifier("textures/gui/container/default.png");

    private int backgroundWidth = 176;
    private int backgroundHeight = 166;

    public MachineFactory(String ID, MachineTier tier, BlockEntityFactory blockEntityFactory, MachineRecipeType type, int inputSlots, int outputSlots,
            int liquidInputSlots, int liquidOutputSlots) {
        this.machineID = ID;
        this.tier = tier;

        if (map.containsKey(machineID)) {
            throw new IllegalArgumentException("Machine ID already taken : " + machineID);
        } else {
            map.put(machineID, this);
        }

        this.blockEntityConstructor = () -> blockEntityFactory.create(this);
        if (type != null) {
            if (type instanceof FurnaceRecipeProxy) {
                MIMachines.WORKSTATIONS_FURNACES.add(this);
            } else {
                MIMachines.RECIPE_TYPES.get(type).factories.add(this);
            }
        }
        this.recipeType = type;

        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.liquidInputSlots = liquidInputSlots;
        this.liquidOutputSlots = liquidOutputSlots;

        slots = liquidInputSlots + liquidOutputSlots + inputSlots + outputSlots;

        slotPositionsX = new int[slots];
        slotPositionsY = new int[slots];

        setTranslationKey("block.modern_industrialization." + machineID);
        setupBackground(machineID + ".png");

    }

    public static MachineFactory getFactoryByID(String machineID) {
        return map.get(machineID);
    }

    public MachineFactory(String ID, MachineTier tier, BlockEntityFactory blockEntityFactory, MachineRecipeType type, int inputSlots,
            int outputSlots) {
        this(ID, tier, blockEntityFactory, type, inputSlots, outputSlots, 0, 0);
    }

    public int getInputSlots() {
        return inputSlots;
    }

    public int getOutputSlots() {
        return outputSlots;
    }

    public int getLiquidInputSlots() {
        return liquidInputSlots;
    }

    public int getLiquidOutputSlots() {
        return liquidOutputSlots;
    }

    private int[] getRange(int start, int end) {
        int[] range = new int[end - start];
        for (int i = start; i < end; i++)
            range[i - start] = i;
        return range;
    }

    public int[] getInputIndices() {
        return getRange(0, inputSlots);
    }

    public int[] getFluidInputIndices() {
        return getRange(inputSlots + (this instanceof SteamMachineFactory ? 1 : 0), inputSlots + liquidInputSlots);
    }

    public int[] getOutputIndices() {
        return getRange(inputSlots + liquidInputSlots, inputSlots + liquidInputSlots + outputSlots);
    }

    public int[] getFluidOutputIndices() {
        return getRange(inputSlots + liquidInputSlots + outputSlots, inputSlots + liquidInputSlots + outputSlots + liquidOutputSlots);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getSlots() {
        return slots;
    }

    public int getSlotPosX(int slotIndex) {
        return slotPositionsX[slotIndex];
    }

    public int getSlotPosY(int slotIndex) {
        return slotPositionsY[slotIndex];
    }

    public MachineFactory setSlotPos(int slotIndex, int posX, int posY) {
        slotPositionsX[slotIndex] = posX;
        slotPositionsY[slotIndex] = posY;
        return this;
    }

    public int getInventoryPosX() {
        return inventoryPosX;
    }

    public int getInventoryPosY() {
        return inventoryPosY;
    }

    public MachineFactory setInventoryPos(int posX, int posY) {
        this.inventoryPosX = posX;
        this.inventoryPosY = posY;
        return this;
    }

    public MachineFactory setInputSlotPosition(int x, int y, int column, int row) {
        if (row * column != inputSlots) {
            throw new IllegalArgumentException("Row x Column : " + row + " and " + column + " must be que equal to inputSlot : " + inputSlots);
        } else {
            setInputSlotPositionWithDelta(x, y, column, row, 0);
        }
        return this;
    }

    public MachineFactory setInputLiquidSlotPosition(int x, int y, int column, int row) {
        if (row * column != liquidInputSlots) {
            throw new IllegalArgumentException(
                    "Row x Column : " + row + " and " + column + " must be que equal to liquidInputSlots : " + liquidInputSlots);
        } else {
            setInputSlotPositionWithDelta(x, y, column, row, inputSlots);
        }
        return this;
    }

    public MachineFactory setOutputSlotPosition(int x, int y, int column, int row) {
        if (row * column != outputSlots) {
            throw new IllegalArgumentException("Row x Column : " + row + " and " + column + " must be que equal to outputSlots : " + outputSlots);
        } else {
            setInputSlotPositionWithDelta(x, y, column, row, inputSlots + liquidInputSlots);
        }
        return this;
    }

    public MachineFactory setLiquidOutputSlotPosition(int x, int y, int column, int row) {
        if (row * column != liquidOutputSlots) {
            throw new IllegalArgumentException(
                    "Row x Column : " + row + " and " + column + " must be que equal to liquidOutputSlots : " + liquidOutputSlots);
        } else {
            setInputSlotPositionWithDelta(x, y, column, row, inputSlots + liquidInputSlots + outputSlots);
        }
        return this;
    }

    protected void setInputSlotPositionWithDelta(int x, int y, int column, int row, int delta) {
        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                int index = delta + i + j * column;
                setSlotPos(index, x + i * 18, y + j * 18);
            }
        }
    }

    public MachineFactory setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
        return this;
    }

    public MachineFactory setupProgressBar(int x, int y, int drawX, int drawY, int sizeX, int sizeY, boolean horizontal, boolean flipped) {
        this.hasProgressBar = true;
        this.progressBarX = x;
        this.progressBarY = y;
        this.progressBarDrawX = drawX;
        this.progressBarDrawY = drawY;
        this.progressBarHorizontal = horizontal;
        this.progressBarFlipped = flipped;
        this.progressBarSizeX = sizeX;
        this.progressBarSizeY = sizeY;
        return this;
    }

    public MachineFactory setupProgressBar(int drawX, int drawY, int sizeX, int sizeY, boolean horizontal) {
        setupProgressBar(176, 0, drawX, drawY, sizeX, sizeY, horizontal, false);
        return this;
    }

    public MachineFactory setupEfficiencyBar(int x, int y, int drawX, int drawY, int sizeX, int sizeY) {
        return setupEfficiencyBar(x, y, drawX, drawY, sizeX, sizeY, false);
    }

    public MachineFactory setupEfficiencyBar(int x, int y, int drawX, int drawY, int sizeX, int sizeY, boolean onlyElectric) {
        if (!onlyElectric || tier.isElectric()) {
            this.hasEfficiencyBar = true;
            this.efficiencyBarX = x;
            this.efficiencyBarY = y;
            this.efficiencyBarDrawX = drawX;
            this.efficiencyBarDrawY = drawY;
            this.efficiencyBarSizeX = sizeX;
            this.efficiencyBarSizeY = sizeY;
        }
        return this;
    }

    public MachineFactory setupElectricityBar(int x, int y) {
        return setupElectricityBar(x, y, true);
    }

    public MachineFactory setupElectricityBar(int x, int y, boolean checkTier) {
        if (!checkTier || tier.isElectric()) {
            hasEnergyBar = true;
            this.electricityBarX = x;
            this.electricityBarY = y;
        }
        return this;
    }

    public MachineFactory hideEfficiencyTooltip() {
        this.efficiencyBarDrawTooltip = false;
        return this;
    }

    public int getProgressBarSizeX() {
        return progressBarSizeX;
    }

    public int getProgressBarSizeY() {
        return progressBarSizeY;
    }

    public boolean hasProgressBar() {
        return hasProgressBar;
    }

    public int getProgressBarX() {
        return progressBarX;
    }

    public int getProgressBarY() {
        return progressBarY;
    }

    public int getProgressBarDrawX() {
        return progressBarDrawX;
    }

    public int getProgressBarDrawY() {
        return progressBarDrawY;
    }

    public boolean isProgressBarHorizontal() {
        return progressBarHorizontal;
    }

    public boolean isProgressBarFlipped() {
        return progressBarFlipped;
    }

    public MIIdentifier getBackgroundIdentifier() {
        return backgroundIdentifier;
    }

    public int getBackgroundHeight() {
        return backgroundHeight;
    }

    public int getBackgroundWidth() {
        return backgroundWidth;
    }

    public MachineFactory setupBackground(String filename, int backgroundWidth, int backgroundHeight) {
        backgroundIdentifier = new MIIdentifier("textures/gui/container/" + filename);
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
        return this;
    }

    public MachineFactory setupBackground(String filename) {
        return setupBackground(filename, 176, 166);
    }

    public MachineSlotType getSlotType(int index) {
        if (index < 0 || index > slots) {
            throw new IllegalArgumentException("index : " + index + " is out of range : " + 0 + " " + (slots - 1));
        } else {
            if (index < inputSlots) {
                return INPUT_SLOT;
            } else if (index < inputSlots + liquidInputSlots) {
                return LIQUID_INPUT_SLOT;
            } else if (index < inputSlots + liquidInputSlots + outputSlots) {
                return OUTPUT_SLOT;
            } else {
                return LIQUID_OUTPUT_SLOT;
            }
        }

    }

    public int getInputBucketCapacity() {
        return inputBucketCapacity;
    }

    public MachineFactory setInputBucketCapacity(int inputBucketCapacity) {
        this.inputBucketCapacity = inputBucketCapacity;
        return this;
    }

    public int getOutputBucketCapacity() {
        return outputBucketCapacity;
    }

    public MachineFactory setOutputBucketCapacity(int outputBucketCapacity) {
        this.outputBucketCapacity = outputBucketCapacity;
        return this;
    }

    public String getID() {
        return machineID;
    }

    public boolean isFluidSlot(int l) {
        return (l >= inputSlots && l < inputSlots + liquidInputSlots)
                || (l >= inputSlots + liquidInputSlots + outputSlots && l < inputSlots + liquidInputSlots + outputSlots + liquidOutputSlots);
    }

    public static Iterable<MachineFactory> getFactories() {
        return map.values();
    }

    public MachineFactory setupCasing(String category) {
        casing = category + "/";
        return this;
    }

    public MachineFactory setupOverlays(String machineType, boolean front, boolean side, boolean top) {
        this.machineType = machineType;
        this.frontOverlay = front;
        this.sideOverlay = side;
        this.topOverlay = top;
        return this;
    }

    public MachineModel buildModel() {
        machineModel = new MachineModel(machineID, new MIIdentifier("blocks/casings/" + casing)).withOutputOverlay(
                new MIIdentifier("blocks/overlays/output"), new MIIdentifier("blocks/overlays/extract_items"),
                new MIIdentifier("blocks/overlays/extract_fluids"));
        String machineFolder = "blocks/machines/" + machineType + "/";
        if (frontOverlay)
            machineModel.withFrontOverlay(new MIIdentifier(machineFolder + "overlay_front"),
                    new MIIdentifier(machineFolder + "overlay_front_active"));
        if (sideOverlay)
            machineModel.withSideOverlay(new MIIdentifier(machineFolder + "overlay_side"), new MIIdentifier(machineFolder + "overlay_side_active"));
        if (topOverlay)
            machineModel.withTopOverlay(new MIIdentifier(machineFolder + "overlay_top"), new MIIdentifier(machineFolder + "overlay_top_active"));
        return machineModel;
    }
}

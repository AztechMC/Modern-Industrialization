package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.model.block.ModelProvider;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;

import java.util.HashMap;
import java.util.function.Supplier;

import static aztech.modern_industrialization.machines.impl.MachineSlotType.*;

public class MachineFactory {

    public MachineBlock block;
    public BlockItem item;
    public BlockEntityType blockEntityType;

    public MachineModel machineModel;

    public final Supplier<MachineBlockEntity> blockEntityConstructor;

    private String machineID;
    private static HashMap<String, MachineFactory> map = new HashMap<String, MachineFactory>();

    private int inputSlots;
    private int outputSlots;

    private int liquidInputSlots;
    private int liquidOutputSlots;

    private int slots;

    private String translationKey = "machine.default";

    // slot index -> inputSlots next liquidInputSlots next outputSlots next liquidOutputSlots

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

    private int inputBucketCapacity = 16;
    private int outputBucketCapacity = 16;

    private MIIdentifier backgroundIdentifier = new MIIdentifier("textures/gui/container/default.png");

    private int backgroundWidth = 176;
    private int backgroundHeight = 166;

    public MachineFactory(String ID, Supplier<MachineBlockEntity> blockEntityConstructor, int inputSlots, int outputSlots, int liquidInputSlots, int liquidOutputSlots){
        this.machineID = ID;

        if(map.containsKey(machineID)){
            throw new IllegalArgumentException("Machine ID already taken : " + machineID);
        }else{
            map.put(machineID, this);
        }

        this.blockEntityConstructor = blockEntityConstructor;

        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.liquidInputSlots = liquidInputSlots;
        this.liquidOutputSlots = liquidOutputSlots;

        slots =  liquidInputSlots + liquidOutputSlots + inputSlots + outputSlots;

        slotPositionsX = new int[slots];
        slotPositionsY = new int[slots];

        setTranslationKey("block.modern_industrialization."+machineID);
        setupBackground(machineID+".png");

        // TODO : REFACTOR AND ADD PARAMTER
        machineModel = new MachineModel(machineID, new MIIdentifier("blocks/casings/steam/bricked_bronze/"))
                .withFrontOverlay(new MIIdentifier("blocks/generators/boiler/coal/overlay_front"), new MIIdentifier("blocks/generators/boiler/coal/overlay_front_active"))
                .withOutputOverlay(new MIIdentifier("blocks/overlays/output"));

        ModelProvider.modelMap.put(new MIIdentifier("block/"+machineID), machineModel);
        ModelProvider.modelMap.put(new MIIdentifier("item/"+machineID), machineModel);

    }

    public static MachineFactory getFactoryByID(String machineID) {
        return map.get(machineID);
    }

    public MachineFactory(String ID,  Supplier<MachineBlockEntity> blockEntityConstructor, int inputSlots, int outputSlots){
        this(ID, blockEntityConstructor, inputSlots, outputSlots, 0 , 0);
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

    public int getInventoryPosX(){
        return inventoryPosX;
    }

    public int getInventoryPosY(){
        return inventoryPosY;
    }

    public MachineFactory setInventoryPos(int posX, int posY){
        this.inventoryPosX = posX;
        this.inventoryPosY = posY;
        return this;
    }

    public MachineFactory setInputSlotPosition(int x, int y, int column, int row){
        if(row*column != inputSlots){
            throw new IllegalArgumentException("Row x Column : " + row + " and " + column + " must be que equal to inputSlot : " + inputSlots );
        }else{
            setInputSlotPositionWithDelta(x, y, column, row, 0);
        }
        return this;
    }

    public MachineFactory setInputLiquidSlotPosition(int x, int y, int column, int row){
        if(row*column != liquidInputSlots){
            throw new IllegalArgumentException("Row x Column : " + row + " and " + column + " must be que equal to liquidInputSlots : " + liquidInputSlots );
        }else{
            setInputSlotPositionWithDelta(x, y, column, row, inputSlots);
        }
        return this;
    }

    public MachineFactory setOutputSlotPosition(int x, int y, int column, int row){
        if(row*column != outputSlots){
            throw new IllegalArgumentException("Row x Column : " + row + " and " + column + " must be que equal to outputSlots : " + outputSlots );
        }else{
            setInputSlotPositionWithDelta(x, y, column, row, inputSlots + liquidInputSlots);
        }
        return this;
    }

    public MachineFactory setLiquidOutputSlotPosition(int x, int y, int column, int row){
        if(row*column != liquidOutputSlots){
            throw new IllegalArgumentException("Row x Column : " + row + " and " + column + " must be que equal to liquidOutputSlots : " + liquidOutputSlots );
        }else{
            setInputSlotPositionWithDelta(x, y, column, row, inputSlots + liquidInputSlots + outputSlots);
        }
        return this;
    }

    protected void setInputSlotPositionWithDelta(int x, int y, int column, int row, int delta){
        for(int i = 0; i < column; i++){
            for(int j = 0; j < row; j++){
                int index = delta + i + j*column;
                setSlotPos(index, x + i*18, y + j*18);
            }
        }
    }

    public MachineFactory setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
        return this;
    }

    public MachineFactory setupProgressBar(int x, int y, int drawX, int drawY, int sizeX, int sizeY, boolean horizontal, boolean flipped){
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
        this.hasEfficiencyBar = true;
        this.efficiencyBarX = x;
        this.efficiencyBarY = y;
        this.efficiencyBarDrawX = drawX;
        this.efficiencyBarDrawY = drawY;
        this.efficiencyBarSizeX = sizeX;
        this.efficiencyBarSizeY = sizeY;
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

    public MachineFactory setupBackground(String filename, int backgroundWidth,int backgroundHeight){
        backgroundIdentifier = new MIIdentifier("textures/gui/container/"+filename);
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
        return this;
    }

    public MachineFactory setupBackground(String filename){
        return setupBackground(filename, 176, 166);
    }

    public MachineSlotType getSlotType(int index) {
        if(index < 0 || index > slots){
            throw new IllegalArgumentException("index : " + index + " is out of range : " + 0 + " " + (slots-1));
        }else{
            if( index < inputSlots){
                return INPUT_SLOT;
            }else if(index < inputSlots+liquidInputSlots){
                return LIQUID_INPUT_SLOT;
            }else if(index < inputSlots+liquidInputSlots + outputSlots){
                return OUTPUT_SLOT;
            }else{
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
                || (l >= inputSlots + liquidInputSlots + outputSlots &&
                l < inputSlots + liquidInputSlots + outputSlots + liquidOutputSlots);
    }

    public static Iterable<MachineFactory> getFactories(){
        return map.values();
    }
}

package aztech.modern_industrialization.machines.impl;

import java.util.function.Supplier;

public class SteamMachineFactory extends MachineFactory {

    private int steamBucketCapacity;

    public SteamMachineFactory(String ID, Supplier<MachineBlockEntity> blockEntityConstructor, int inputSlots, int outputSlots, int liquidInputSlots, int liquidOutputSlots) {
        super(ID, blockEntityConstructor, inputSlots, outputSlots, liquidInputSlots + 1, liquidOutputSlots);
    }

    public SteamMachineFactory(String ID, Supplier<MachineBlockEntity> blockEntityConstructor, int inputSlots, int outputSlots) {
        super(ID, blockEntityConstructor, inputSlots, outputSlots,  1, 0);
    }

    public SteamMachineFactory setSteamSlotPos(int posX, int posY) {
        setSlotPos(this.getInputSlots(), posX, posY);
        return this;
    }

    @Override
    public MachineFactory setInputLiquidSlotPosition(int x, int y, int column, int row){
        if(row*column != this.getLiquidInputSlots() - 1){ // one slot is reserved for steam input
            throw new IllegalArgumentException("Row x Column : " + row + " and " + column + " must be que equal to liquidInputSlots : " + (this.getLiquidInputSlots() - 1));
        }else{
            setInputSlotPositionWithDelta(x, y, column, row, this.getInputSlots() + 1);
        }
        return this;
    }

    public int getSteamBucketCapacity() {
        return steamBucketCapacity;
    }

    public SteamMachineFactory setSteamBucketCapacity(int steamBucketCapacity) {
        this.steamBucketCapacity = steamBucketCapacity;
        return this;
    }
}

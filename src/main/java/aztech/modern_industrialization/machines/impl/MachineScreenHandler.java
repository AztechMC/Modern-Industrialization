package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.factory.MachineFactory;
import aztech.modern_industrialization.fluid.gui.FluidContainerScreenHandler;
import aztech.modern_industrialization.fluid.gui.FluidSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

import static aztech.modern_industrialization.machines.factory.MachineSlotType.*;


public class MachineScreenHandler extends FluidContainerScreenHandler {

    private Inventory inventory;
    private PropertyDelegate propertyDelegate;
    private MachineFactory factory;

    public MachineScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleInventory(buf.readInt()),
                new ArrayPropertyDelegate(buf.readInt()), MachineFactory.getFactoryByID(buf.readString()));
    }

    public MachineScreenHandler(int syncId, PlayerInventory playerInventory,
                                Inventory inventory, PropertyDelegate propertyDelegate, MachineFactory factory) {

        super(ModernIndustrialization.SCREEN_HANDLER_TYPE_MACHINE, syncId, factory.getSlots());

        this.inventory = inventory;
        this.factory = factory;
        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);

        for(int i = 0; i < factory.getSlots(); i++){
            if(factory.getSlotType(i) == INPUT_SLOT){
                this.addSlot(new Slot(inventory, i, factory.getSlotPosX(i), factory.getSlotPosY(i)));
            }else if(factory.getSlotType(i) == OUTPUT_SLOT){
                this.addSlot(new OutputSlot(inventory, i, factory.getSlotPosX(i), factory.getSlotPosY(i)));
            }else if(factory.getSlotType(i) == LIQUID_INPUT_SLOT){
                this.addSlot(new FluidSlot(inventory, i, factory.getSlotPosX(i), factory.getSlotPosY(i)));
            }else{
                this.addSlot(new LiquidOutputSlot(inventory, i, factory.getSlotPosX(i), factory.getSlotPosY(i)));
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9,
                        factory.getInventoryPosX() + j * 18, factory.getInventoryPosY() + i * 18));
            }
        }


        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j,
                    factory.getInventoryPosX() + j * 18, 58 + factory.getInventoryPosY()));
        }

    }
    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public MachineFactory getMachineFactory() {
        return factory;
    }
    public int getTickProgress() { return propertyDelegate.get(1); }
    public int getTickRecipe() { return propertyDelegate.get(2); }
    public boolean getIsActive() { return propertyDelegate.get(0) == 1; }


    private static class OutputSlot extends Slot {

        public OutputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }

    private static class LiquidOutputSlot extends FluidSlot {

        public LiquidOutputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsertFluid(Fluid fluid) {
            return false;
        }
    }
}

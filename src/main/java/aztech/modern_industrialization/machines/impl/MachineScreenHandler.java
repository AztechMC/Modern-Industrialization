package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.inventory.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

import static aztech.modern_industrialization.machines.impl.MachineSlotType.*;


public class MachineScreenHandler extends ConfigurableScreenHandler {

    public ConfigurableInventory inventory;
    private PropertyDelegate propertyDelegate;
    private MachineFactory factory;

    public MachineScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, ConfigurableInventories.clientOfBuf(buf),
                new ArrayPropertyDelegate(buf.readInt()), MachineFactory.getFactoryByID(buf.readString()));
    }

    public MachineScreenHandler(int syncId, PlayerInventory playerInventory,
                                ConfigurableInventory inventory, PropertyDelegate propertyDelegate, MachineFactory factory) {

        super(ModernIndustrialization.SCREEN_HANDLER_TYPE_MACHINE, syncId, playerInventory, inventory);

        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);
        this.factory = factory;
        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);


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

        int itemCnt = 0;
        for(int i = 0; i < factory.getSlots(); i++){
            if(factory.getSlotType(i) == INPUT_SLOT || factory.getSlotType(i) == OUTPUT_SLOT){
                ConfigurableItemStack stack = inventory.getItemStacks().get(itemCnt);
                this.addSlot(stack.new ConfigurableItemSlot(inventory, itemCnt, factory.getSlotPosX(i), factory.getSlotPosY(i)));
                ++itemCnt;
            }else{
                ConfigurableFluidStack stack = inventory.getFluidStacks().get(i - itemCnt);
                this.addSlot(stack.new ConfigurableFluidSlot(inventory, factory.getSlotPosX(i), factory.getSlotPosY(i)));
            }
        }

        addSlot(new LockingModeSlot(inventory, 152, 7));
    }
    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void close(PlayerEntity player) {
        inventory.onClose(playerInventory.player);
        super.close(player);
    }

    public MachineFactory getMachineFactory() {
        return factory;
    }
    public int getTickProgress() { return propertyDelegate.get(1); }
    public int getTickRecipe() { return propertyDelegate.get(2); }
    public boolean getIsActive() { return propertyDelegate.get(0) == 1; }
    public int getEfficiencyTicks() { return propertyDelegate.get(3); }
    public int getMaxEfficiencyTicks() { return propertyDelegate.get(4); }
}

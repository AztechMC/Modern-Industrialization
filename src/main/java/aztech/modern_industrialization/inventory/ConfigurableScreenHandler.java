package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.fluid.FluidContainerItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * The ScreenHandler for a configurable inventory.
 * The first slots must be the player slots for shift-click to work correctly!
 */ // TODO: lockable item slots
public abstract class ConfigurableScreenHandler extends ScreenHandler {
    private static final int PLAYER_SLOTS = 36;
    public boolean lockingMode = false;
    protected PlayerInventory playerInventory;
    protected ConfigurableInventory inventory;
    private List<ConfigurableItemStack> trackedItems;
    private List<ConfigurableFluidStack> trackedFluids;

    protected ConfigurableScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ConfigurableInventory inventory) {
        super(type, syncId);
        this.playerInventory = playerInventory;
        this.inventory = inventory;

        if(playerInventory.player instanceof ServerPlayerEntity) {
            trackedItems = ConfigurableItemStack.copyList(inventory.getItemStacks());
            trackedFluids = ConfigurableFluidStack.copyList(inventory.getFluidStacks());
        }
    }

    @Override
    public void sendContentUpdates() {
        if(playerInventory.player instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity)playerInventory.player;
            for(int i = 0; i < trackedItems.size(); i++) {
                if(!trackedItems.get(i).equals(inventory.getItemStacks().get(i))) {
                    trackedItems.set(i, new ConfigurableItemStack(inventory.getItemStacks().get(i)));
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(syncId);
                    buf.writeInt(i);
                    buf.writeCompoundTag(trackedItems.get(i).writeToTag(new CompoundTag()));
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ConfigurableInventoryPackets.UPDATE_ITEM_SLOT, buf);
                }
            }
            for(int i = 0; i < trackedFluids.size(); i++) {
                if(!trackedFluids.get(i).equals(inventory.getFluidStacks().get(i))) {
                    trackedFluids.set(i, new ConfigurableFluidStack(inventory.getFluidStacks().get(i)));
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(syncId);
                    buf.writeInt(i);
                    buf.writeCompoundTag(trackedFluids.get(i).writeToTag(new CompoundTag()));
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ConfigurableInventoryPackets.UPDATE_FLUID_SLOT, buf);
                }
            }
        }
        super.sendContentUpdates();
    }

    @Override
    public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if(i >= 0) {
            Slot slot = this.slots.get(i);
            if (slot instanceof LockingModeSlot) {
                if(actionType != SlotActionType.PICKUP) {
                    return ItemStack.EMPTY;
                }
                lockingMode = !lockingMode;
                // sync locking state TODO: handle data re-sent
                return new ItemStack(Items.DIAMOND, lockingMode ? 1 : 0);
            } else if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                if(actionType != SlotActionType.PICKUP) {
                    return ItemStack.EMPTY;
                }
                ConfigurableFluidStack.ConfigurableFluidSlot fluidSlot = (ConfigurableFluidStack.ConfigurableFluidSlot) slot;
                ConfigurableFluidStack fluidStack = fluidSlot.getConfStack();
                if(lockingMode) {
                    fluidStack.togglePlayerLock();
                } else {
                    ItemStack heldStack = playerEntity.inventory.getCursorStack();
                    if (heldStack.getItem() instanceof FluidContainerItem) {
                        FluidContainerItem fluidContainer = (FluidContainerItem) heldStack.getItem();
                        // Try to extract from held item, then try to insert into held item
                        Fluid extractedFluid = fluidStack.getFluid();
                        if (extractedFluid == Fluids.EMPTY) {
                            extractedFluid = fluidContainer.getExtractableFluid(heldStack);
                        }
                        int extractedAmount = 0;
                        if (fluidSlot.canInsertFluid(extractedFluid)) {
                            extractedAmount = fluidContainer.extractFluid(heldStack, extractedFluid, fluidStack.getRemainingSpace(), FluidContainerItem.cursorPlayerConsumer(playerEntity));
                        }
                        if (extractedAmount > 0) {
                            fluidStack.increment(extractedAmount);
                            fluidStack.setFluid(extractedFluid);
                            fluidStack.updateDisplayedItem();
                            inventory.markDirty();
                        } else {
                            Fluid fluid = fluidStack.getFluid();
                            if (fluidSlot.canExtractFluid(fluid)) {
                                int insertedAmount = fluidContainer.insertFluid(heldStack, fluid, fluidStack.getAmount(), FluidContainerItem.cursorPlayerConsumer(playerEntity));
                                fluidStack.decrement(insertedAmount);
                                fluidStack.updateDisplayedItem();
                                inventory.markDirty();
                            }
                        }
                    }
                }
                return fluidSlot.getStack().copy();
            } else if(slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                if(lockingMode) {
                    if(actionType != SlotActionType.PICKUP) {
                        return ItemStack.EMPTY;
                    }
                    ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                    ConfigurableItemStack itemStack = itemSlot.getConfStack();
                    itemStack.togglePlayerLock();
                    return itemStack.getStack().copy();
                }
            }
        }
        return super.onSlotClick(i, j, actionType, playerEntity);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if(slot != null && slot.hasStack()) {
            if(!slot.canTakeItems(player)) return newStack;
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if(slotIndex < PLAYER_SLOTS) {
                // from player to container inventory
                if(!this.insertItem(originalStack, PLAYER_SLOTS, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if(!this.insertItem(originalStack, 0, PLAYER_SLOTS, false)) {
                // from container inventory to player
                return ItemStack.EMPTY;
            }

            if(originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    // (almost) Copy-paste from ScreenHandler, Mojang forgot to check slot2.canInsert(stack) at one of the places.
    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }

        Slot slot2;
        ItemStack itemStack;
        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (fromLast) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot2 = this.slots.get(i);
                itemStack = slot2.getStack();
                if (!itemStack.isEmpty() && canStacksCombine(stack, itemStack) && slot2.canInsert(stack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= stack.getMaxCount()) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot2.markDirty();
                        bl = true;
                    } else if (itemStack.getCount() < stack.getMaxCount()) {
                        stack.decrement(stack.getMaxCount() - itemStack.getCount());
                        itemStack.setCount(stack.getMaxCount());
                        slot2.markDirty();
                        bl = true;
                    }
                }

                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (fromLast) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (fromLast) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot2 = (Slot)this.slots.get(i);
                itemStack = slot2.getStack();
                if (itemStack.isEmpty() && slot2.canInsert(stack)) {
                    if (stack.getCount() > slot2.getMaxItemCount()) {
                        slot2.setStack(stack.split(slot2.getMaxItemCount()));
                    } else {
                        slot2.setStack(stack.split(stack.getCount()));
                    }

                    slot2.markDirty();
                    bl = true;
                    break;
                }

                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return bl;
    }

    public static class LockingModeSlot extends Slot {
        public LockingModeSlot(Inventory inventory, int x, int y) {
            super(inventory, -1, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStack(ItemStack stack) {

        }

        @Override
        public boolean doDrawHoveringEffect() {
            return true;
        }
    }
}

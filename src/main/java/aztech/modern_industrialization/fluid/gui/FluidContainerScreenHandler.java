package aztech.modern_industrialization.fluid.gui;

import aztech.modern_industrialization.fluid.FluidContainerItem;
import aztech.modern_industrialization.fluid.FluidStackItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * A `ScreenHandler` that recognizes `FluidSlot`s. Slots must be divided in two parts: the container and the inventory.
 * The container slots must have ids ranging from 0 to containerSlotCount (excluded).
 */
public abstract class FluidContainerScreenHandler extends ScreenHandler {
    private int containerSlotCount;

    protected FluidContainerScreenHandler(ScreenHandlerType<?> type, int syncId, int containerSlotCount) {
        super(type, syncId);
        this.containerSlotCount = containerSlotCount;
    }

    @Override
    public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if(actionType == SlotActionType.PICKUP && i >= 0) {
            Slot slot = this.slots.get(i);
            if(slot instanceof FluidSlot) {
                FluidSlot fluidSlot = (FluidSlot)slot;
                ItemStack heldStack = playerEntity.inventory.getCursorStack();
                if(heldStack.getItem() instanceof FluidContainerItem) {
                    FluidContainerItem fluidContainer = (FluidContainerItem) heldStack.getItem();
                    ItemStack fluidStack = slot.getStack();
                    int amount = FluidStackItem.getAmount(fluidStack);
                    int capacity = FluidStackItem.getCapacity(fluidStack);
                    // Try to extract from held item, then try to insert into held item
                    Fluid extractedFluid = FluidStackItem.getFluid(fluidStack);
                    if(extractedFluid == Fluids.EMPTY) {
                        extractedFluid = fluidContainer.getExtractableFluid();
                    }
                    int extractedAmount = 0;
                    if(fluidSlot.canInsertFluid(extractedFluid)) {
                        final boolean[] firstInvoke = new boolean[]{true};
                        extractedAmount = fluidContainer.extractFluid(heldStack, extractedFluid, capacity - amount, stack -> {
                            if (firstInvoke[0]) {
                                playerEntity.inventory.setCursorStack(stack);
                                firstInvoke[0] = false;
                            } else {
                                playerEntity.inventory.offerOrDrop(playerEntity.world, stack);
                            }
                        });
                    }
                    if(extractedAmount > 0) {
                        FluidStackItem.setAmount(fluidStack, amount + extractedAmount);
                        FluidStackItem.setFluid(fluidStack, extractedFluid);
                    } else {
                        Fluid fluid = FluidStackItem.getFluid(fluidStack);
                        if(fluidSlot.canExtractFluid(fluid)) {
                            final boolean[] firstInvoke_ = new boolean[]{true};
                            int insertedAmount = fluidContainer.insertFluid(heldStack, fluid, amount, stack -> {
                                if (firstInvoke_[0]) {
                                    playerEntity.inventory.setCursorStack(stack);
                                    firstInvoke_[0] = false;
                                } else {
                                    playerEntity.inventory.offerOrDrop(playerEntity.world, stack);
                                }
                                // TODO: fix copy/paste
                            });
                            FluidStackItem.setAmount(fluidStack, amount - insertedAmount);
                        }
                    }
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
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if(slotIndex < this.containerSlotCount) {
                // from container to player inventory
                if(!this.insertItem(originalStack, this.containerSlotCount, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if(!this.insertItem(originalStack, 0, this.containerSlotCount, false)) {
                // from player inventory to container
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
}

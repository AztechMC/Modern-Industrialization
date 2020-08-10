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
 * A `ScreenHandler` that recognizes `FluidSlot`s.
 */
public abstract class FluidContainerScreenHandler extends ScreenHandler {
    protected FluidContainerScreenHandler(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if(actionType == SlotActionType.PICKUP && i >= 0) {
            Slot slot = this.slots.get(i);
            if(slot instanceof FluidSlot) {
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
                    final boolean[] firstInvoke = new boolean[] {true};
                    int extractedAmount = fluidContainer.extractFluid(heldStack, extractedFluid, capacity - amount, stack -> {
                        if(firstInvoke[0]) {
                            playerEntity.inventory.setCursorStack(stack);
                            firstInvoke[0] = false;
                        } else {
                            playerEntity.inventory.offerOrDrop(playerEntity.world, stack);
                        }
                    });
                    if(extractedAmount > 0) {
                        FluidStackItem.setAmount(fluidStack, amount + extractedAmount);
                        FluidStackItem.setFluid(fluidStack, extractedFluid);
                    } else {
                        Fluid fluid = FluidStackItem.getFluid(fluidStack);
                        final boolean[] firstInvoke_ = new boolean[] {true};
                        int insertedAmount = fluidContainer.insertFluid(heldStack, fluid, amount, stack -> {
                            if(firstInvoke_[0]) {
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
        return super.onSlotClick(i, j, actionType, playerEntity);
    }
}

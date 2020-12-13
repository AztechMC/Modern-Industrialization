/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.inventory;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.fluid.FluidApi;
import dev.technici4n.fasttransferlib.api.fluid.FluidIo;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * The ScreenHandler for a configurable inventory. The first slots must be the
 * player slots for shift-click to work correctly!
 */ // TODO: lockable item slots
public abstract class ConfigurableScreenHandler extends ScreenHandler {
    private static final int PLAYER_SLOTS = 36;
    public boolean lockingMode = false;
    protected PlayerInventory playerInventory;
    protected MIInventory inventory;
    private List<ConfigurableItemStack> trackedItems;
    private List<ConfigurableFluidStack> trackedFluids;

    protected ConfigurableScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, MIInventory inventory) {
        super(type, syncId);
        this.playerInventory = playerInventory;
        this.inventory = inventory;

        if (playerInventory.player instanceof ServerPlayerEntity) {
            trackedItems = ConfigurableItemStack.copyList(inventory.itemStacks);
            trackedFluids = ConfigurableFluidStack.copyList(inventory.fluidStacks);
        }
    }

    @Override
    public void sendContentUpdates() {
        if (playerInventory.player instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) playerInventory.player;
            for (int i = 0; i < trackedItems.size(); i++) {
                if (!trackedItems.get(i).equals(inventory.itemStacks.get(i))) {
                    trackedItems.set(i, new ConfigurableItemStack(inventory.itemStacks.get(i)));
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(syncId);
                    buf.writeInt(i);
                    buf.writeCompoundTag(trackedItems.get(i).writeToTag(new CompoundTag()));
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ConfigurableInventoryPackets.UPDATE_ITEM_SLOT, buf);
                }
            }
            for (int i = 0; i < trackedFluids.size(); i++) {
                if (!trackedFluids.get(i).equals(inventory.fluidStacks.get(i))) {
                    trackedFluids.set(i, new ConfigurableFluidStack(inventory.fluidStacks.get(i)));
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
        if (i >= 0) {
            Slot slot = this.slots.get(i);
            if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                if (actionType != SlotActionType.PICKUP) {
                    return ItemStack.EMPTY;
                }
                ConfigurableFluidStack.ConfigurableFluidSlot fluidSlot = (ConfigurableFluidStack.ConfigurableFluidSlot) slot;
                ConfigurableFluidStack fluidStack = fluidSlot.getConfStack();
                if (lockingMode) {
                    fluidStack.togglePlayerLock();
                } else {
                    FluidIo io = FluidApi.ofPlayerCursor(playerEntity);
                    // Extract first
                    if (io != null && io.supportsFluidExtraction()) {
                        for (int id = 0; id < io.getFluidSlotCount(); ++id) {
                            Fluid fluid = io.getFluid(id);
                            if (fluid != Fluids.EMPTY && fluidSlot.canInsertFluid(fluid)) {
                                long extracted = io.extract(id, fluid, fluidStack.getRemainingSpace(), Simulation.ACT);
                                if (extracted > 0) {
                                    fluidStack.increment(extracted);
                                    fluidStack.setFluid(fluid);
                                    // TODO: markDirty?
                                    return fluidSlot.getStack().copy();
                                }
                            }
                        }
                    }
                    // Otherwise insert
                    if (io != null && io.supportsFluidInsertion()) {
                        Fluid fluid = fluidStack.getFluid();
                        if (fluidSlot.canExtractFluid(fluid)) {
                            long leftover = io.insert(fluid, fluidStack.getAmount(), Simulation.ACT);
                            if (leftover != fluidStack.getAmount()) {
                                fluidStack.setAmount(leftover);
                                // TODO: markDirty???
                                return fluidSlot.getStack().copy();
                            }
                        }
                    }
                }
                return fluidSlot.getStack().copy();
            } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                if (lockingMode) {
                    if (actionType != SlotActionType.PICKUP) {
                        return ItemStack.EMPTY;
                    }
                    ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                    ConfigurableItemStack itemStack = itemSlot.getConfStack();
                    itemStack.togglePlayerLock(playerInventory.getCursorStack());
                    return itemStack.getItemKey().toStack(itemStack.getCount()).copy();
                }
            }
        }
        return super.onSlotClick(i, j, actionType, playerEntity);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            if (!slot.canTakeItems(player))
                return newStack;
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (slotIndex < PLAYER_SLOTS) { // from player to container inventory
                if (!this.insertItem(originalStack, PLAYER_SLOTS, this.slots.size(), false)) {
                    if (slotIndex < 27) { // inside inventory
                        if (!this.insertItem(originalStack, 27, 36, false)) { // toolbar
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.insertItem(originalStack, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.insertItem(originalStack, 0, PLAYER_SLOTS, true)) { // from container inventory to player
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    // (almost) Copy-paste from ScreenHandler, Mojang forgot to check
    // slot2.canInsert(stack) at one of the places.
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
            while (!stack.isEmpty()) {
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

            while (true) {
                if (fromLast) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot2 = (Slot) this.slots.get(i);
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
}

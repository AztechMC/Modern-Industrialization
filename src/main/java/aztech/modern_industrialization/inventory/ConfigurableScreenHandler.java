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

import dev.technici4n.fasttransferlib.experimental.api.context.ContainerItemContext;
import dev.technici4n.fasttransferlib.experimental.api.fluid.ItemFluidStorage;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * The ScreenHandler for a configurable inventory. The first slots must be the
 * player slots for shift-click to work correctly!
 */
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
            trackedItems = ConfigurableItemStack.copyList(inventory.getItemStacks());
            trackedFluids = ConfigurableFluidStack.copyList(inventory.getFluidStacks());
        }
    }

    @Override
    public void sendContentUpdates() {
        if (playerInventory.player instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) playerInventory.player;
            for (int i = 0; i < trackedItems.size(); i++) {
                if (!trackedItems.get(i).equals(inventory.getItemStacks().get(i))) {
                    trackedItems.set(i, new ConfigurableItemStack(inventory.getItemStacks().get(i)));
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(syncId);
                    buf.writeInt(i);
                    buf.writeNbt(trackedItems.get(i).toNbt());
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ConfigurableInventoryPackets.UPDATE_ITEM_SLOT, buf);
                }
            }
            for (int i = 0; i < trackedFluids.size(); i++) {
                if (!trackedFluids.get(i).equals(inventory.getFluidStacks().get(i))) {
                    trackedFluids.set(i, new ConfigurableFluidStack(inventory.getFluidStacks().get(i)));
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(syncId);
                    buf.writeInt(i);
                    buf.writeNbt(trackedFluids.get(i).toNbt());
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ConfigurableInventoryPackets.UPDATE_FLUID_SLOT, buf);
                }
            }
        }
        super.sendContentUpdates();
    }

    @Override
    public void onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if (i >= 0) {
            Slot slot = this.slots.get(i);
            if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot fluidSlot) {
                if (actionType != SlotActionType.PICKUP) {
                    return;
                }
                ConfigurableFluidStack fluidStack = fluidSlot.getConfStack();
                if (lockingMode) {
                    fluidStack.togglePlayerLock();
                } else {
                    Storage<FluidKey> io = ContainerItemContext.ofPlayerCursor(playerEntity, this).find(ItemFluidStorage.ITEM);
                    if (io != null) {
                        // Extract first
                        long previousAmount = fluidStack.amount;
                        try (Transaction transaction = Transaction.openOuter()) {
                            for (StorageView<FluidKey> view : io.iterable(transaction)) {
                                FluidKey fluid = view.resource();
                                if (!fluid.isEmpty() && fluidSlot.canInsertFluid(fluid)) {
                                    try (Transaction tx = transaction.openNested()) {
                                        long extracted = view.extract(fluid, fluidStack.getRemainingSpace(), tx);
                                        if (extracted > 0) {
                                            tx.commit();
                                            fluidStack.increment(extracted);
                                            fluidStack.setKey(fluid);
                                        }
                                    }
                                }
                            }
                            transaction.commit();
                        }
                        if (previousAmount != fluidStack.amount) {
                            // TODO: markDirty?
                            return;
                        }

                        // Otherwise insert
                        FluidKey fluid = fluidStack.resource();
                        if (!fluid.isEmpty() && fluidSlot.canExtractFluid(fluid)) {
                            try (Transaction tx = Transaction.openOuter()) {
                                fluidStack.decrement(io.insert(fluid, fluidStack.amount(), tx));
                                tx.commit();
                                // TODO: markDirty?
                                return;
                            }
                        }
                    }
                }
                return;
            } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                if (lockingMode) {
                    if (actionType != SlotActionType.PICKUP) {
                        return;
                    }
                    ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                    ConfigurableItemStack itemStack = itemSlot.getConfStack();
                    itemStack.togglePlayerLock(getCursorStack().getItem());
                    return;
                }
            }
        }
        super.onSlotClick(i, j, actionType, playerEntity);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasStack() && slot.canTakeItems(player)) {
            if (slotIndex < PLAYER_SLOTS) { // from player to container inventory
                if (!this.insertItem(slot, PLAYER_SLOTS, this.slots.size(), false)) {
                    if (slotIndex < 27) { // inside inventory
                        if (!this.insertItem(slot, 27, 36, false)) { // toolbar
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.insertItem(slot, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.insertItem(slot, 0, PLAYER_SLOTS, true)) { // from container inventory to player
                return ItemStack.EMPTY;
            }
        }

        return ItemStack.EMPTY;
    }

    @Deprecated
    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        throw new UnsupportedOperationException("Don't use this shit, use the one below instead.");
    }

    // Rewrite of ScreenHandler's buggy, long and shitty logic.
    /**
     * @return True if something was inserted.
     */
    protected boolean insertItem(Slot sourceSlot, int startIndex, int endIndex, boolean fromLast) {
        boolean insertedSomething = false;
        for (int iter = 0; iter < 2; ++iter) {
            boolean allowEmptySlots = iter == 1; // iteration 0 only allows insertion into existing slots
            int i = fromLast ? endIndex - 1 : startIndex;

            while (0 <= i && i < endIndex && !sourceSlot.getStack().isEmpty()) {
                Slot targetSlot = getSlot(i);
                ItemStack sourceStack = sourceSlot.getStack();
                ItemStack targetStack = targetSlot.getStack();

                if (targetSlot.canInsert(sourceStack)
                        && ((allowEmptySlots && targetStack.isEmpty()) || ItemStack.canCombine(targetStack, sourceStack))) {
                    int maxInsert = targetSlot.getMaxItemCount(sourceStack) - targetStack.getCount();
                    if (maxInsert > 0) {
                        ItemStack newTargetStack = sourceStack.split(maxInsert);
                        newTargetStack.increment(targetStack.getCount());
                        targetSlot.setStack(newTargetStack);
                        sourceSlot.markDirty();
                        insertedSomething = true;
                    }
                }

                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return insertedSomething;
    }
}

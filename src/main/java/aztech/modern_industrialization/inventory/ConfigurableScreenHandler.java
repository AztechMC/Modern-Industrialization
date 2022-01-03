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

import io.netty.buffer.Unpooled;
import java.util.List;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The ScreenHandler for a configurable inventory. The first slots must be the
 * player slots for shift-click to work correctly!
 */
public abstract class ConfigurableScreenHandler extends AbstractContainerMenu {
    private static final int PLAYER_SLOTS = 36;
    public boolean lockingMode = false;
    protected Inventory playerInventory;
    protected MIInventory inventory;
    private List<ConfigurableItemStack> trackedItems;
    private List<ConfigurableFluidStack> trackedFluids;

    protected ConfigurableScreenHandler(MenuType<?> type, int syncId, Inventory playerInventory, MIInventory inventory) {
        super(type, syncId);
        this.playerInventory = playerInventory;
        this.inventory = inventory;

        if (playerInventory.player instanceof ServerPlayer) {
            trackedItems = ConfigurableItemStack.copyList(inventory.getItemStacks());
            trackedFluids = ConfigurableFluidStack.copyList(inventory.getFluidStacks());
        }
    }

    @Override
    public void broadcastChanges() {
        if (playerInventory.player instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) playerInventory.player;
            for (int i = 0; i < trackedItems.size(); i++) {
                if (!trackedItems.get(i).equals(inventory.getItemStacks().get(i))) {
                    trackedItems.set(i, new ConfigurableItemStack(inventory.getItemStacks().get(i)));
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                    buf.writeInt(containerId);
                    buf.writeInt(i);
                    buf.writeNbt(trackedItems.get(i).toNbt());
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ConfigurableInventoryPackets.UPDATE_ITEM_SLOT, buf);
                }
            }
            for (int i = 0; i < trackedFluids.size(); i++) {
                if (!trackedFluids.get(i).equals(inventory.getFluidStacks().get(i))) {
                    trackedFluids.set(i, new ConfigurableFluidStack(inventory.getFluidStacks().get(i)));
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                    buf.writeInt(containerId);
                    buf.writeInt(i);
                    buf.writeNbt(trackedFluids.get(i).toNbt());
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ConfigurableInventoryPackets.UPDATE_FLUID_SLOT, buf);
                }
            }
        }
        super.broadcastChanges();
    }

    @Override
    public void clicked(int i, int j, ClickType actionType, Player playerEntity) {
        if (i >= 0) {
            Slot slot = this.slots.get(i);
            if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot fluidSlot) {
                if (actionType != ClickType.PICKUP) {
                    return;
                }
                ConfigurableFluidStack fluidStack = fluidSlot.getConfStack();
                if (lockingMode) {
                    fluidStack.togglePlayerLock();
                } else {
                    Storage<FluidVariant> io = ContainerItemContext.ofPlayerCursor(playerEntity, this).find(FluidStorage.ITEM);
                    if (io != null) {
                        // Extract first
                        long previousAmount = fluidStack.amount;
                        try (Transaction transaction = Transaction.openOuter()) {
                            for (StorageView<FluidVariant> view : io.iterable(transaction)) {
                                FluidVariant fluid = view.getResource();
                                if (!fluid.isBlank() && fluidSlot.canInsertFluid(fluid)) {
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
                        FluidVariant fluid = fluidStack.getResource();
                        if (!fluid.isBlank() && fluidSlot.canExtractFluid(fluid)) {
                            try (Transaction tx = Transaction.openOuter()) {
                                fluidStack.decrement(io.insert(fluid, fluidStack.getAmount(), tx));
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
                    if (actionType != ClickType.PICKUP) {
                        return;
                    }
                    ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                    ConfigurableItemStack itemStack = itemSlot.getConfStack();
                    itemStack.togglePlayerLock(getCarried().getItem());
                    return;
                }
            }
        }
        super.clicked(i, j, actionType, playerEntity);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem() && slot.mayPickup(player)) {
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
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
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

            while (0 <= i && i < endIndex && !sourceSlot.getItem().isEmpty()) {
                Slot targetSlot = getSlot(i);
                ItemStack sourceStack = sourceSlot.getItem();
                ItemStack targetStack = targetSlot.getItem();

                if (targetSlot.mayPlace(sourceStack)
                        && ((allowEmptySlots && targetStack.isEmpty()) || ItemStack.isSameItemSameTags(targetStack, sourceStack))) {
                    int maxInsert = targetSlot.getMaxStackSize(sourceStack) - targetStack.getCount();
                    if (maxInsert > 0) {
                        ItemStack newTargetStack = sourceStack.split(maxInsert);
                        newTargetStack.grow(targetStack.getCount());
                        targetSlot.set(newTargetStack);
                        sourceSlot.setChanged();
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

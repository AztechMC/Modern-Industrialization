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

import aztech.modern_industrialization.network.machines.UpdateFluidSlotPacket;
import aztech.modern_industrialization.network.machines.UpdateItemSlotPacket;
import aztech.modern_industrialization.util.Simulation;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The ScreenHandler for a configurable inventory. The first slots must be the
 * player slots for shift-click to work correctly!
 */
public abstract class ConfigurableScreenHandler extends AbstractContainerMenu {
    private static final int PLAYER_SLOTS = 36;
    public boolean lockingMode = false;
    protected Inventory playerInventory;
    public final MIInventory inventory;
    private List<ConfigurableItemStack> trackedItems;
    private List<ConfigurableFluidStack> trackedFluids;
    // Groups slots together to avoid shift-click splitting a stack across to unrelated subinventories.
    private final Map<Slot, SlotGroup> slotGroups = new IdentityHashMap<>();
    private final Set<SlotGroup> slotGroupIndices = new LinkedHashSet<>();

    protected ConfigurableScreenHandler(MenuType<?> type, int syncId, Inventory playerInventory, MIInventory inventory) {
        super(type, syncId);
        this.playerInventory = playerInventory;
        this.inventory = inventory;

        if (playerInventory.player instanceof ServerPlayer) {
            trackedItems = ConfigurableItemStack.copyList(inventory.getItemStacks());
            trackedFluids = ConfigurableFluidStack.copyList(inventory.getFluidStacks());
        }
    }

    public void updateSlot(int index, Slot slot) {
        var existingSlot = getSlot(index);
        var slotGroup = slotGroups.remove(existingSlot);
        if (slotGroup != null) {
            slotGroups.put(slot, slotGroup);
        }
        this.slots.set(index, slot);
    }

    protected Slot addSlot(Slot slot, SlotGroup slotGroup) {
        slotGroups.put(slot, slotGroup);
        slotGroupIndices.add(slotGroup);
        return super.addSlot(slot);
    }

    @Override
    public void broadcastChanges() {
        if (playerInventory.player instanceof ServerPlayer player) {
            for (int i = 0; i < trackedItems.size(); i++) {
                if (!trackedItems.get(i).equals(inventory.getItemStacks().get(i))) {
                    trackedItems.set(i, new ConfigurableItemStack(inventory.getItemStacks().get(i)));
                    new UpdateItemSlotPacket(containerId, i, trackedItems.get(i))
                            .sendToClient(player);
                }
            }
            for (int i = 0; i < trackedFluids.size(); i++) {
                if (!trackedFluids.get(i).equals(inventory.getFluidStacks().get(i))) {
                    trackedFluids.set(i, new ConfigurableFluidStack(inventory.getFluidStacks().get(i)));
                    new UpdateFluidSlotPacket(containerId, i, trackedFluids.get(i))
                            .sendToClient(player);
                }
            }
        }
        super.broadcastChanges();
    }

    @Override
    public void clicked(int i, int j, ClickType actionType, Player player) {
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
                    fluidSlot.playerInteract(createCarriedSlotAccess(), player, true);
                }
                return;
            } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot itemSlot) {
                if (lockingMode) {
                    switch (actionType) {
                    case PICKUP -> {
                        ConfigurableItemStack itemStack = itemSlot.getConfStack();
                        itemStack.togglePlayerLock(getCarried().getItem());
                    }
                    case QUICK_MOVE -> {
                        // Try to move everything to player inventory
                        insertItem(itemSlot, 0, PLAYER_SLOTS, true);
                        // Lock to air if empty
                        if (slot.getItem().isEmpty()) {
                            itemSlot.getConfStack().playerLock(Items.AIR, Simulation.ACT);
                        }
                    }
                    }
                    return;
                }
            }
        }
        super.clicked(i, j, actionType, player);
    }

    @Override
    public final ItemStack quickMoveStack(Player player, int slotIndex) {
        handleShiftClick(player, slotIndex);
        return ItemStack.EMPTY;
    }

    protected void handleShiftClick(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem() && slot.mayPickup(player)) {
            if (slotIndex < PLAYER_SLOTS) { // from player to container inventory
                // try to shift-click fluid first
                var ctx = SlotAccess.forContainer(player.getInventory(), slot.getContainerSlot());
                for (var maybeFluidSlot : slots) {
                    if (maybeFluidSlot instanceof ConfigurableFluidStack.ConfigurableFluidSlot fluidSlot
                            && fluidSlot.playerInteract(ctx, player, false)) {
                        return;
                    }
                }

                // move by slot group
                for (var group : slotGroupIndices) {
                    if (this.insertItem(slot, PLAYER_SLOTS, this.slots.size(), false, s -> slotGroups.get(s) == group)) {
                        return;
                    }
                }
                if (slotIndex < 27) { // inside inventory
                    this.insertItem(slot, 27, 36, false);// toolbar
                } else {
                    this.insertItem(slot, 0, 27, false);
                }
            } else { // from container inventory to player
                this.insertItem(slot, 0, PLAYER_SLOTS, true);
            }
        }
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
        return insertItem(sourceSlot, startIndex, endIndex, fromLast, s -> true);
    }

    protected boolean insertItem(Slot sourceSlot, int startIndex, int endIndex, boolean fromLast, Predicate<Slot> filter) {
        boolean insertedSomething = false;
        for (int iter = 0; iter < 2; ++iter) {
            boolean allowEmptySlots = iter == 1; // iteration 0 only allows insertion into existing slots
            int i = fromLast ? endIndex - 1 : startIndex;

            while (0 <= i && i < endIndex && !sourceSlot.getItem().isEmpty()) {
                Slot targetSlot = getSlot(i);
                ItemStack sourceStack = sourceSlot.getItem();
                ItemStack targetStack = targetSlot.getItem();

                if (filter.test(targetSlot) && targetSlot.mayPlace(sourceStack)
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

    /**
     * Return true if any slot is player locked, false otherwise.
     */
    public boolean hasUnlockedSlot() {
        for (var slot : slots) {
            if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot cis) {
                if (!cis.getConfStack().playerLocked && cis.getConfStack().playerLockable) {
                    return true;
                }
            }
            if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot cfs) {
                if (!cfs.getConfStack().playerLocked && cfs.getConfStack().playerLockable) {
                    return true;
                }
            }
        }
        return false;
    }

    public void lockAll(boolean lock) {
        for (var slot : slots) {
            if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot cis) {
                if (cis.getConfStack().playerLocked != lock) {
                    cis.getConfStack().togglePlayerLock();
                }
            }
            if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot cfs) {
                if (cfs.getConfStack().playerLocked != lock) {
                    cfs.getConfStack().togglePlayerLock();
                }
            }
        }
    }
}

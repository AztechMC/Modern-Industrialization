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

import aztech.modern_industrialization.util.NbtHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;

/**
 * An item stack that can be configured. TODO: sync lock state
 */
public class ConfigurableItemStack {
    ItemStack stack = ItemStack.EMPTY;
    Item lockedItem = null;
    private boolean playerLocked = false;
    private boolean machineLocked = false;
    boolean playerLockable = true;
    boolean playerInsert = false;
    boolean playerExtract = true;
    boolean pipesInsert = false;
    boolean pipesExtract = false;

    public ConfigurableItemStack() {
    }

    public static ConfigurableItemStack standardInputSlot() {
        ConfigurableItemStack stack = new ConfigurableItemStack();
        stack.playerInsert = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableItemStack standardOutputSlot() {
        ConfigurableItemStack stack = new ConfigurableItemStack();
        stack.pipesExtract = true;
        return stack;
    }

    public static ConfigurableItemStack standardIOSlot(boolean pipeIO) {
        ConfigurableItemStack stack = new ConfigurableItemStack();
        stack.playerInsert = true;
        if (pipeIO) {
            stack.pipesInsert = true;
            stack.pipesExtract = true;
        }
        return stack;
    }

    public ConfigurableItemStack(ConfigurableItemStack other) {
        this.stack = other.stack.copy();
        this.lockedItem = other.lockedItem;
        this.playerLocked = other.playerLocked;
        this.machineLocked = other.machineLocked;
        this.playerLockable = other.playerLockable;
        this.playerInsert = other.playerInsert;
        this.playerExtract = other.playerExtract;
        this.pipesInsert = other.pipesInsert;
        this.pipesExtract = other.pipesExtract;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConfigurableItemStack that = (ConfigurableItemStack) o;
        return playerLocked == that.playerLocked && machineLocked == that.machineLocked && playerLockable == that.playerLockable
                && playerInsert == that.playerInsert && playerExtract == that.playerExtract && pipesInsert == that.pipesInsert
                && pipesExtract == that.pipesExtract && ItemStack.areEqual(stack, that.stack) && lockedItem == that.lockedItem;
    }

    /**
     * Create a copy of a list of configurable fluid stacks.
     */
    public static ArrayList<ConfigurableItemStack> copyList(List<ConfigurableItemStack> list) {
        ArrayList<ConfigurableItemStack> copy = new ArrayList<>(list.size());
        for (ConfigurableItemStack stack : list) {
            copy.add(new ConfigurableItemStack(stack));
        }
        return copy;
    }

    public ItemStack getStack() {
        return stack;
    }

    public Item getLockedItem() {
        return lockedItem;
    }

    /**
     * Try to take some items from the stack.
     * 
     * @param count How many items to take
     * @return What was taken: a stack with at most count items.
     */
    public ItemStack splitStack(int count) {
        return count > 0 ? stack.split(count) : ItemStack.EMPTY;
    }

    /**
     * Take the stack.
     */
    public ItemStack removeStack() {
        ItemStack removed = stack;
        stack = ItemStack.EMPTY;
        return removed;
    }

    public void setStack(ItemStack stack) {
        if (lockedItem != null && stack.getItem() != lockedItem && !stack.isEmpty()) {
            throw new RuntimeException("Trying to override locked item");
        }
        this.stack = stack;
    }

    public boolean canInsert(ItemStack stack) {
        return lockedItem == null || lockedItem == stack.getItem();
    }

    public boolean isPlayerLocked() {
        return playerLocked;
    }

    public boolean isMachineLocked() {
        return machineLocked;
    }

    public void enableMachineLock(Item lockedItem) {
        if (this.lockedItem != null && lockedItem != this.lockedItem)
            throw new RuntimeException("Trying to override locked item");
        machineLocked = true;
        this.lockedItem = lockedItem;
    }

    public void disableMachineLock() {
        machineLocked = false;
        onToggleLock();
    }

    public void togglePlayerLock(ItemStack cursorStack) {
        if (playerLockable) {
            if (playerLocked && lockedItem == Items.AIR && !cursorStack.isEmpty()) {
                lockedItem = cursorStack.getItem();
            } else {
                playerLocked = !playerLocked;
            }
            onToggleLock();
        }
    }

    private void onToggleLock() {
        if (!machineLocked && !playerLocked) {
            lockedItem = null;
        } else if (lockedItem == null) {
            lockedItem = stack.getItem();
        }
    }

    public boolean canPlayerLock() {
        return playerLockable;
    }

    public CompoundTag writeToTag(CompoundTag tag) {
        stack.toTag(tag);
        if (lockedItem != null) {
            NbtHelper.putItem(tag, "lockedItem", lockedItem);
        }
        // TODO: more efficient encoding?
        tag.putBoolean("machineLocked", machineLocked);
        tag.putBoolean("playerLocked", playerLocked);
        tag.putBoolean("playerLockable", playerLockable);
        tag.putBoolean("playerInsert", playerInsert);
        tag.putBoolean("playerExtract", playerExtract);
        tag.putBoolean("pipesInsert", pipesInsert);
        tag.putBoolean("pipesExtract", pipesExtract);
        return tag;
    }

    public void readFromTag(CompoundTag tag) {
        stack = ItemStack.fromTag(tag);
        if (tag.contains("lockedItem")) {
            lockedItem = NbtHelper.getItem(tag, "lockedItem");
        }
        machineLocked = tag.getBoolean("machineLocked");
        playerLocked = tag.getBoolean("playerLocked");
        playerLockable = tag.getBoolean("playerLockable");
        playerInsert = tag.getBoolean("playerInsert");
        playerExtract = tag.getBoolean("playerExtract");
        pipesInsert = tag.getBoolean("pipesInsert");
        pipesExtract = tag.getBoolean("pipesExtract");
    }

    /**
     * Try locking the slot to the given item, return true if it succeeded
     */
    public boolean playerLock(Item item) {
        if ((stack.isEmpty() || stack.getItem() == item) && (lockedItem == null || lockedItem == Items.AIR)) {
            lockedItem = item;
            playerLocked = true;
            return true;
        }
        return false;
    }

    public boolean canPipesExtract() {
        return pipesExtract;
    }

    public boolean canPipesInsert() {
        return pipesInsert;
    }

    public class ConfigurableItemSlot extends Slot {
        private final Predicate<ItemStack> insertPredicate;

        public ConfigurableItemSlot(Inventory inventory, int id, int x, int y, Predicate<ItemStack> insertPredicate) {
            super(inventory, id, x, y);

            this.insertPredicate = insertPredicate;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return playerInsert && ConfigurableItemStack.this.canInsert(stack) && insertPredicate.test(stack);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return playerExtract;
        }

        public ConfigurableItemStack getConfStack() {
            return ConfigurableItemStack.this;
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public void setStack(ItemStack stack) {
            ConfigurableItemStack.this.stack = stack;
        }
    }
}

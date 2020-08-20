package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * An item stack that can be configured.
 */
public class ConfigurableItemStack {
    ItemStack stack = ItemStack.EMPTY;
    Item lockedItem = null;
    boolean playerLockable = true;
    boolean playerInsert = false;
    boolean playerExtract = true;
    boolean pipesInsert = false;
    boolean pipesExtract = false;

    ConfigurableItemStack() {}

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

    public ConfigurableItemStack(ConfigurableItemStack other) {
        this.stack = other.stack.copy();
        this.lockedItem = other.lockedItem;
        this.playerLockable = other.playerLockable;
        this.playerInsert = other.playerInsert;
        this.playerExtract = other.playerExtract;
        this.pipesInsert = other.pipesInsert;
        this.pipesExtract = other.pipesExtract;
    }

    /**
     * Create a copy of a list of configurable fluid stacks.
     */
    public static ArrayList<ConfigurableItemStack> copyList(List<ConfigurableItemStack> list) {
        ArrayList<ConfigurableItemStack> copy = new ArrayList<>(list.size());
        for(ConfigurableItemStack stack : list) {
            copy.add(new ConfigurableItemStack(stack));
        }
        return copy;
    }

    public ItemStack getStack() {
        return stack;
    }

    /**
     * Try to take some items from the stack.
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
        if(lockedItem != null && stack.getItem() != lockedItem) {
            throw new RuntimeException("Trying to override locked item");
        }
        this.stack = stack;
    }

    public boolean canInsert(ItemStack stack, Direction direction) {
        return lockedItem == null || lockedItem == stack.getItem();
    }

    public boolean isVisiblyLocked() {
        return playerLockable && lockedItem != null;
    }

    public void toggleLock() {
        if(playerLockable) {
            lockedItem = lockedItem == null ? stack.getItem() : null;
        }
    }

    public CompoundTag writeToTag(CompoundTag tag) {
        stack.toTag(tag);
        if(lockedItem != null) {
            NbtHelper.putItem(tag, "lockedItem", lockedItem);
        }
        // TODO: more efficient encoding?
        tag.putBoolean("playerLockable", playerLockable);
        tag.putBoolean("playerInsert", playerInsert);
        tag.putBoolean("playerExtract", playerExtract);
        tag.putBoolean("pipesInsert", pipesInsert);
        tag.putBoolean("pipesExtract", pipesExtract);
        return tag;
    }

    public void readFromTag(CompoundTag tag) {
        stack = ItemStack.fromTag(tag);
        if(tag.contains("lockedItem")) {
            lockedItem = NbtHelper.getItem(tag, "lockedItem");
        }
        playerLockable = tag.getBoolean("playerLockable");
        playerInsert = tag.getBoolean("playerInsert");
        playerExtract = tag.getBoolean("playerExtract");
        pipesInsert = tag.getBoolean("pipesInsert");
        pipesExtract = tag.getBoolean("pipesExtract");
    }

    public class ConfigurableItemSlot extends Slot {
        public ConfigurableItemSlot(Inventory inventory, int id, int x, int y) {
            super(inventory, id, x, y);
        }

        // We don't allow item insertion obviously.
        @Override
        public boolean canInsert(ItemStack stack) {
            return playerInsert && (lockedItem == null || stack.getItem() == lockedItem);
        }

        // No extraction either.
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

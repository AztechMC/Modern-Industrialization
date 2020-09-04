package aztech.modern_industrialization.inventory;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.FluidSlotIO;
import aztech.modern_industrialization.fluid.FluidStackItem;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A fluid stack that can be configured. TODO: sync fluid and lock state
 */
public class ConfigurableFluidStack {
    private FluidKey fluid = FluidKeys.EMPTY;
    private int amount = 0;
    private int capacity;
    private FluidKey lockedFluid = null;
    private boolean playerLocked = false;
    private boolean machineLocked = false;
    private boolean playerLockable = true;
    private boolean playerInsert = false;
    private boolean playerExtract = true;
    boolean pipesInsert = false;
    boolean pipesExtract = false;
    private ConfigurableInventory inventory = null;

    private ItemStack displayedStack = FluidStackItem.getEmptyStack();

    public ConfigurableFluidStack(ConfigurableInventory inventory, int capacity) {
        this.capacity = capacity;
        this.inventory = inventory;
    }

    public static ConfigurableFluidStack standardInputSlot(ConfigurableInventory inventory, int capacity) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(inventory, capacity);
        stack.playerInsert = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack standardOutputSlot(ConfigurableInventory inventory, int capacity) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(inventory, capacity);
        stack.pipesExtract = true;
        return stack;
    }

    public static ConfigurableFluidStack lockedInputSlot(ConfigurableInventory inventory, int capacity, FluidKey fluid) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(inventory, capacity);
        stack.fluid = stack.lockedFluid = fluid;
        stack.playerInsert = true;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack lockedOutputSlot(ConfigurableInventory inventory, int capacity, FluidKey fluid) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(inventory, capacity);
        stack.fluid = stack.lockedFluid = fluid;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesExtract = true;
        return stack;
    }

    public ConfigurableFluidStack(ConfigurableFluidStack other) {
        this.fluid = other.fluid;
        this.amount = other.amount;
        this.capacity = other.capacity;
        this.lockedFluid = other.lockedFluid;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurableFluidStack that = (ConfigurableFluidStack) o;
        return amount == that.amount &&
                capacity == that.capacity &&
                playerLocked == that.playerLocked &&
                machineLocked == that.machineLocked &&
                playerLockable == that.playerLockable &&
                playerInsert == that.playerInsert &&
                playerExtract == that.playerExtract &&
                pipesInsert == that.pipesInsert &&
                pipesExtract == that.pipesExtract &&
                fluid == that.fluid &&
                lockedFluid == that.lockedFluid;
    }

    /**
     * Create a copy of a list of configurable fluid stacks.
     */
    public static ArrayList<ConfigurableFluidStack> copyList(List<ConfigurableFluidStack> list) {
        ArrayList<ConfigurableFluidStack> copy = new ArrayList<>(list.size());
        for(ConfigurableFluidStack stack : list) {
            copy.add(new ConfigurableFluidStack(stack));
        }
        return copy;
    }

    public FluidKey getFluid() {
        return fluid;
    }

    public int getAmount() {
        return amount;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean canPlayerInsert() {
        return playerInsert;
    }

    public boolean canPlayerExtract() {
        return playerExtract;
    }

    public void setFluid(FluidKey fluid) {
        boolean needsUpdate = fluid != this.fluid;
        this.fluid = fluid;
        if(needsUpdate) {
            updateDisplayedItem();
        }
    }

    public void setAmount(int amount) {
        boolean needsUpdate = amount != this.amount;
        this.amount = amount;
        if(amount > capacity) throw new IllegalStateException("amount > capacity in the fluid stack");
        if(amount < 0) throw new IllegalStateException("amount < 0 in the fluid stack");
        if(amount == 0 && lockedFluid == null) {
            fluid = FluidKeys.EMPTY;
        }
        if(needsUpdate) {
            updateDisplayedItem();
        }
    }

    public void increment(int amount) {
        setAmount(this.amount + amount);
    }

    public void decrement(int amount) {
        increment(-amount);
    }

    public boolean isFluidValid(FluidKey fluid) {
        return fluid == this.fluid || (lockedFluid == null && this.fluid.isEmpty());
    }

    public int getRemainingSpace() {
        return capacity - amount;
    }

    public boolean isPlayerLocked() {
        return playerLocked;
    }

    public boolean isMachineLocked() { return machineLocked; }

    public CompoundTag writeToTag(CompoundTag tag) {
        tag.put("fluid", fluid.toTag());
        tag.putInt("amount", amount);
        tag.putInt("capacity", capacity);
        if(lockedFluid != null) {
            tag.put("lockedFluid", lockedFluid.toTag());
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
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
        amount = tag.getInt("amount");
        capacity = tag.getInt("capacity");
        if(tag.contains("lockedFluid")) {
            lockedFluid = NbtHelper.getFluidCompatible(tag, "lockedFluid");
        }
        machineLocked = tag.getBoolean("machineLocked");
        playerLocked = tag.getBoolean("playerLocked");
        playerLockable = tag.getBoolean("playerLockable");
        playerInsert = tag.getBoolean("playerInsert");
        playerExtract = tag.getBoolean("playerExtract");
        pipesInsert = tag.getBoolean("pipesInsert");
        pipesExtract = tag.getBoolean("pipesExtract");
        updateDisplayedItem();
    }

    /**
     * Update the displayed item if necessary.
     */
    public void updateDisplayedItem() {
        if(inventory == null || !inventory.isOpen()) return;

        displayedStack = FluidStackItem.getEmptyStack();
        FluidStackItem.setFluid(displayedStack, fluid.getRawFluid());
        FluidStackItem.setCapacity(displayedStack, capacity);
        FluidStackItem.setAmount(displayedStack, amount);
        if(playerInsert) {
            FluidStackItem.setIO(displayedStack, playerExtract ? FluidSlotIO.INPUT_AND_OUTPUT : FluidSlotIO.INPUT_ONLY);
        } else if(playerExtract) {
            FluidStackItem.setIO(displayedStack, FluidSlotIO.OUTPUT_ONLY);
        } else {
             throw new UnsupportedOperationException("A fluid slot must either have input or output.");
        }
    }

    public void enableMachineLock(FluidKey lockedFluid) {
        if(this.lockedFluid != null && lockedFluid != this.lockedFluid) throw new RuntimeException("Trying to override locked fluid");
        machineLocked = true;
        this.fluid = this.lockedFluid = lockedFluid;
    }

    public void disableMachineLock() {
        machineLocked = false;
        onToggleLock();
    }

    public void togglePlayerLock() {
        if(playerLockable) {
            playerLocked = !playerLocked;
            onToggleLock();
        }
    }

    private void onToggleLock() {
        if(!machineLocked && !playerLocked) {
            lockedFluid = null;
            if(amount == 0) {
                setFluid(FluidKeys.EMPTY);
                updateDisplayedItem();
            }
        } else if(lockedFluid == null) {
            lockedFluid = fluid;
        }
    }

    public class ConfigurableFluidSlot extends Slot {
        public ConfigurableFluidSlot(Inventory inventory, int x, int y) {
            super(inventory, -1, x, y);
            updateDisplayedItem();
        }

        // We don't allow item insertion obviously.
        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        // No extraction either.
        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        public boolean canInsertFluid(FluidKey fluid) {
            return playerInsert && isFluidValid(fluid);
        }

        public boolean canExtractFluid(FluidKey fluid) {
            return playerExtract;
        }

        public ConfigurableFluidStack getConfStack() {
            return ConfigurableFluidStack.this;
        }

        @Override
        public ItemStack getStack() {
            //return displayedStack;
            return ItemStack.EMPTY;
        }

        @Override
        public void setStack(ItemStack stack) {
            displayedStack = stack;
        }
    }
}

package aztech.modern_industrialization.inventory;

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
    private Fluid fluid = Fluids.EMPTY;
    private int amount = 0;
    private int capacity;
    private Fluid lockedFluid = null;
    private boolean playerLocked = false;
    private boolean machineLocked = false;
    private boolean playerLockable = true;
    private boolean playerInsert = false;
    private boolean playerExtract = true;
    boolean pipesInsert = false;
    boolean pipesExtract = false;

    /**
     * Whether this stack is for the steam input of a steam machine_recipe. It will be ignored by recipes and it will allow
     * other slots to be filled with its fluid.
     */
    boolean steamInput = false;
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

    public static ConfigurableFluidStack steamInputSlot(ConfigurableInventory inventory, int capacity) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(inventory, capacity);
        stack.fluid = stack.lockedFluid = ModernIndustrialization.FLUID_STEAM;
        stack.steamInput = true;
        stack.playerInsert = true;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack lockedInputSlot(ConfigurableInventory inventory, int capacity, Fluid fluid) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(inventory, capacity);
        stack.fluid = stack.lockedFluid = fluid;
        stack.playerInsert = true;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack lockedOutputSlot(ConfigurableInventory inventory, int capacity, Fluid fluid) {
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
        this.steamInput = other.steamInput;
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
                steamInput == that.steamInput &&
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

    public Fluid getFluid() {
        return fluid;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isSteamInput() {
        return steamInput;
    }

    public void setFluid(Fluid fluid) {
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
            fluid = Fluids.EMPTY;
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

    public boolean canInsertFluid(Fluid fluid) {
        return fluid == this.fluid || (lockedFluid == null && this.fluid == Fluids.EMPTY);
    }

    public int getRemainingSpace() {
        return capacity - amount;
    }

    public boolean isPlayerLocked() {
        return playerLocked;
    }

    public boolean isMachineLocked() { return machineLocked; }

    public CompoundTag writeToTag(CompoundTag tag) {
        NbtHelper.putFluid(tag, "fluid", fluid);
        tag.putInt("amount", amount);
        tag.putInt("capacity", capacity);
        if(lockedFluid != null) {
            NbtHelper.putFluid(tag, "lockedFluid", lockedFluid);
        }
        // TODO: more efficient encoding?
        tag.putBoolean("machineLocked", machineLocked);
        tag.putBoolean("playerLocked", playerLocked);
        tag.putBoolean("playerLockable", playerLockable);
        tag.putBoolean("playerInsert", playerInsert);
        tag.putBoolean("playerExtract", playerExtract);
        tag.putBoolean("pipesInsert", pipesInsert);
        tag.putBoolean("pipesExtract", pipesExtract);
        tag.putBoolean("steamInput", steamInput);
        return tag;
    }

    public void readFromTag(CompoundTag tag) {
        fluid = NbtHelper.getFluid(tag, "fluid");
        amount = tag.getInt("amount");
        capacity = tag.getInt("capacity");
        if(tag.contains("lockedFluid")) {
            lockedFluid = NbtHelper.getFluid(tag, "lockedFluid");
        }
        machineLocked = tag.getBoolean("machineLocked");
        playerLocked = tag.getBoolean("playerLocked");
        playerLockable = tag.getBoolean("playerLockable");
        playerInsert = tag.getBoolean("playerInsert");
        playerExtract = tag.getBoolean("playerExtract");
        pipesInsert = tag.getBoolean("pipesInsert");
        pipesExtract = tag.getBoolean("pipesExtract");
        steamInput = tag.getBoolean("steamInput");
        updateDisplayedItem();
    }

    /**
     * Update the displayed item if necessary.
     */
    public void updateDisplayedItem() {
        if(inventory == null || !inventory.isOpen()) return;

        displayedStack = FluidStackItem.getEmptyStack();
        FluidStackItem.setFluid(displayedStack, fluid);
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

    public void enableMachineLock(Fluid lockedFluid) {
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
                setFluid(Fluids.EMPTY);
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

        public boolean canInsertFluid(Fluid fluid) {
            return playerInsert && (lockedFluid == null || lockedFluid == fluid);
        }

        public boolean canExtractFluid(Fluid fluid) {
            return playerExtract;
        }

        public ConfigurableFluidStack getConfStack() {
            return ConfigurableFluidStack.this;
        }

        @Override
        public ItemStack getStack() {
            return displayedStack;
        }

        @Override
        public void setStack(ItemStack stack) {
            displayedStack = stack;
        }
    }
}

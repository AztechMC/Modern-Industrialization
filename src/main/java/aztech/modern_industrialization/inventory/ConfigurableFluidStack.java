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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Participant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;

/**
 * A fluid stack that can be configured.
 */
public class ConfigurableFluidStack implements StorageView<Fluid>, Participant<FluidState> {
    private Fluid fluid = Fluids.EMPTY;
    private long amount = 0;
    private long capacity;
    private Fluid lockedFluid = null;
    private boolean playerLocked = false;
    private boolean machineLocked = false;
    private boolean playerLockable = true;
    private boolean playerInsert = false;
    private boolean playerExtract = true;
    private boolean pipesInsert = false;
    private boolean pipesExtract = false;

    public ConfigurableFluidStack(long capacity) {
        this.capacity = capacity;
    }

    public static ConfigurableFluidStack standardInputSlot(long capacity) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.playerInsert = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack standardOutputSlot(long capacity) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.pipesExtract = true;
        return stack;
    }

    public static ConfigurableFluidStack lockedInputSlot(long capacity, Fluid fluid) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.fluid = stack.lockedFluid = fluid;
        stack.playerInsert = true;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack lockedOutputSlot(long capacity, Fluid fluid) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.fluid = stack.lockedFluid = fluid;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesExtract = true;
        return stack;
    }

    public ConfigurableFluidStack(ConfigurableFluidStack other) {
        this(other.capacity);
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConfigurableFluidStack that = (ConfigurableFluidStack) o;
        return amount == that.amount && capacity == that.capacity && playerLocked == that.playerLocked && machineLocked == that.machineLocked
                && playerLockable == that.playerLockable && playerInsert == that.playerInsert && playerExtract == that.playerExtract
                && pipesInsert == that.pipesInsert && pipesExtract == that.pipesExtract && fluid == that.fluid && lockedFluid == that.lockedFluid;
    }

    /**
     * Create a copy of a list of configurable fluid stacks.
     */
    public static ArrayList<ConfigurableFluidStack> copyList(List<ConfigurableFluidStack> list) {
        ArrayList<ConfigurableFluidStack> copy = new ArrayList<>(list.size());
        for (ConfigurableFluidStack stack : list) {
            copy.add(new ConfigurableFluidStack(stack));
        }
        return copy;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public long getAmount() {
        return amount;
    }

    public long getCapacity() {
        return capacity;
    }

    public boolean canPlayerInsert() {
        return playerInsert;
    }

    public boolean canPlayerExtract() {
        return playerExtract;
    }

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }

    public void setAmount(long amount) {
        this.amount = amount;
        if (amount > capacity)
            throw new IllegalStateException("amount > capacity in the fluid stack");
        if (amount < 0)
            throw new IllegalStateException("amount < 0 in the fluid stack");
        if (amount == 0 && lockedFluid == null) {
            fluid = Fluids.EMPTY;
        }
    }

    public void increment(long amount) {
        setAmount(this.amount + amount);
    }

    public void decrement(long amount) {
        increment(-amount);
    }

    public boolean isValid(Fluid fluid) {
        return fluid == this.fluid || (lockedFluid == null && this.fluid == Fluids.EMPTY);
    }

    public long getRemainingSpace() {
        return capacity - amount;
    }

    public boolean isPlayerLocked() {
        return playerLocked;
    }

    public boolean isMachineLocked() {
        return machineLocked;
    }

    public CompoundTag writeToTag(CompoundTag tag) {
        NbtHelper.putFluid(tag, "fluid", fluid);
        tag.putLong("amount_ftl", amount);
        tag.putLong("capacity_ftl", capacity);
        if (lockedFluid != null) {
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
        return tag;
    }

    public void readFromTag(CompoundTag tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
        if (tag.contains("amount")) {
            amount = tag.getInt("amount") * 81;
            capacity = tag.getInt("capacity") * 81;
        } else {
            amount = tag.getLong("amount_ftl");
            capacity = tag.getLong("capacity_ftl");
        }
        if (tag.contains("lockedFluid")) {
            lockedFluid = NbtHelper.getFluidCompatible(tag, "lockedFluid");
        }
        machineLocked = tag.getBoolean("machineLocked");
        playerLocked = tag.getBoolean("playerLocked");
        playerLockable = tag.getBoolean("playerLockable");
        playerInsert = tag.getBoolean("playerInsert");
        playerExtract = tag.getBoolean("playerExtract");
        pipesInsert = tag.getBoolean("pipesInsert");
        pipesExtract = tag.getBoolean("pipesExtract");
        if (fluid == Fluids.EMPTY) {
            amount = 0;
        }
    }

    public void enableMachineLock(Fluid lockedFluid) {
        if (this.lockedFluid != null && lockedFluid != this.lockedFluid)
            throw new RuntimeException("Trying to override locked fluid");
        machineLocked = true;
        this.fluid = this.lockedFluid = lockedFluid;
    }

    public void disableMachineLock() {
        machineLocked = false;
        onToggleLock();
    }

    public void togglePlayerLock() {
        if (playerLockable) {
            playerLocked = !playerLocked;
            onToggleLock();
        }
    }

    private void onToggleLock() {
        if (!machineLocked && !playerLocked) {
            lockedFluid = null;
            if (amount == 0) {
                setFluid(Fluids.EMPTY);
            }
        } else if (lockedFluid == null) {
            lockedFluid = fluid;
        }
    }

    public Fluid getLockedFluid() {
        return lockedFluid;
    }

    public boolean playerLock(Fluid fluid) {
        if (lockedFluid == null && (this.fluid == Fluids.EMPTY || this.fluid == fluid)) {
            lockedFluid = fluid;
            this.fluid = fluid;
            playerLocked = true;
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return amount == 0;
    }

    public boolean canPlayerLock() {
        return playerLockable;
    }

    public boolean canPipesExtract() {
        return pipesExtract;
    }

    public boolean canPipesInsert() {
        return pipesInsert;
    }

    @Override
    public long extract(Fluid fluid, long maxAmount, Transaction transaction) {
        FluidPreconditions.notEmptyNotNegative(fluid, maxAmount);
        if (pipesExtract && fluid == getFluid()) {
            long extracted = Math.min(maxAmount, amount);
            transaction.enlist(ConfigurableFluidStack.this);
            decrement(extracted);
            return extracted;
        }
        return 0;
    }

    @Override
    public Fluid resource() {
        return fluid;
    }

    @Override
    public long amount() {
        return amount;
    }

    @Override
    public FluidState onEnlist() {
        return new FluidState(fluid, amount);
    }

    @Override
    public void onClose(FluidState fluidState, TransactionResult result) {
        if (result.wasAborted()) {
            this.fluid = fluidState.fluid;
            this.amount = fluidState.amount;
        }
    }

    public class ConfigurableFluidSlot extends Slot {
        private final Runnable markDirty;

        public ConfigurableFluidSlot(Runnable markDirty, int x, int y) {
            super(null, -1, x, y);

            this.markDirty = markDirty;
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
            return playerInsert && isValid(fluid);
        }

        public boolean canExtractFluid(Fluid fluid) {
            return playerExtract;
        }

        public ConfigurableFluidStack getConfStack() {
            return ConfigurableFluidStack.this;
        }

        @Override
        public ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStack(ItemStack stack) {
        }

        @Override
        public void markDirty() {
            markDirty.run();
        }
    }
}

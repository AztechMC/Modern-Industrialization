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
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

/**
 * A fluid stack that can be configured.
 */
public class ConfigurableFluidStack extends SnapshotParticipant<ResourceAmount<FluidKey>> implements StorageView<FluidKey>, IConfigurableSlot {
    private FluidKey fluid = FluidKey.empty();
    private long amount = 0;
    private long capacity;
    private FluidKey lockedFluid = null;
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
        stack.fluid = stack.lockedFluid = FluidKey.of(fluid);
        stack.playerInsert = true;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack lockedOutputSlot(long capacity, Fluid fluid) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.fluid = stack.lockedFluid = FluidKey.of(fluid);
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
    public SlotConfig getConfig() {
        return new SlotConfig(playerLockable, playerInsert, playerExtract, pipesInsert, pipesExtract);
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

    public FluidKey getFluid() {
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

    public void setFluid(FluidKey fluid) {
        this.fluid = fluid;
    }

    public void setAmount(long amount) {
        this.amount = amount;
        if (amount > capacity)
            throw new IllegalStateException("amount > capacity in the fluid stack");
        if (amount < 0)
            throw new IllegalStateException("amount < 0 in the fluid stack");
        if (amount == 0 && lockedFluid == null) {
            fluid = FluidKey.empty();
        }
    }

    public void increment(long amount) {
        setAmount(this.amount + amount);
    }

    public void decrement(long amount) {
        increment(-amount);
    }

    public boolean isValid(FluidKey fluid) {
        return fluid.equals(this.fluid) || (lockedFluid == null && this.fluid.isEmpty());
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

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
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

    public static ConfigurableFluidStack fromNbt(NbtCompound tag) {
        ConfigurableFluidStack fs = new ConfigurableFluidStack(0);
        fs.fluid = NbtHelper.getFluidCompatible(tag, "fluid");
        if (tag.contains("amount")) {
            fs.amount = tag.getInt("amount") * 81;
            fs.capacity = tag.getInt("capacity") * 81;
        } else {
            fs.amount = tag.getLong("amount_ftl");
            fs.capacity = tag.getLong("capacity_ftl");
        }
        if (tag.contains("lockedFluid")) {
            fs.lockedFluid = NbtHelper.getFluidCompatible(tag, "lockedFluid");
        }
        fs.machineLocked = tag.getBoolean("machineLocked");
        fs.playerLocked = tag.getBoolean("playerLocked");
        fs.playerLockable = tag.getBoolean("playerLockable");
        fs.playerInsert = tag.getBoolean("playerInsert");
        fs.playerExtract = tag.getBoolean("playerExtract");
        fs.pipesInsert = tag.getBoolean("pipesInsert");
        fs.pipesExtract = tag.getBoolean("pipesExtract");
        if (fs.fluid.isEmpty()) {
            fs.amount = 0;
        }
        return fs;
    }

    public void enableMachineLock(FluidKey lockedFluid) {
        if (this.lockedFluid != null && !lockedFluid.equals(this.lockedFluid))
            throw new RuntimeException("Trying to override locked fluid");
        machineLocked = true;
        this.fluid = lockedFluid;
        this.lockedFluid = lockedFluid;
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
                setFluid(FluidKey.empty());
            }
        } else if (lockedFluid == null) {
            lockedFluid = fluid;
        }
    }

    public FluidKey getLockedFluid() {
        return lockedFluid;
    }

    public boolean isLockedTo(FluidKey fluid) {
        return Objects.equals(lockedFluid, fluid);
    }

    public boolean playerLock(FluidKey fluid) {
        if (lockedFluid == null && (this.fluid.isEmpty() || this.fluid.equals(fluid))) {
            lockedFluid = fluid;
            this.fluid = fluid;
            playerLocked = true;
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return getFluid().isEmpty();
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
    public long extract(FluidKey fluid, long maxAmount, Transaction transaction) {
        FluidPreconditions.notEmptyNotNegative(fluid, maxAmount);
        if (pipesExtract && fluid.equals(this.fluid)) {
            long extracted = Math.min(maxAmount, amount);
            updateSnapshots(transaction);
            decrement(extracted);
            return extracted;
        }
        return 0;
    }

    @Override
    public FluidKey resource() {
        return fluid;
    }

    @Override
    public long amount() {
        return amount;
    }

    @Override
    public long capacity() {
        return capacity;
    }

    @Override
    protected ResourceAmount<FluidKey> createSnapshot() {
        return new ResourceAmount<>(fluid, amount);
    }

    @Override
    protected void readSnapshot(ResourceAmount<FluidKey> snapshot) {
        this.fluid = snapshot.resource();
        this.amount = snapshot.amount();
    }

    public class ConfigurableFluidSlot extends Slot {
        private final Runnable markDirty;

        public ConfigurableFluidSlot(ConfigurableFluidSlot other) {
            this(other.markDirty, other.x, other.y);

            this.id = other.id;
        }

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

        public boolean canInsertFluid(FluidKey fluid) {
            return playerInsert && isValid(fluid);
        }

        public boolean canExtractFluid(FluidKey fluid) {
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

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

import aztech.modern_industrialization.api.ReiDraggable;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.UnsupportedOperationInventory;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.registry.Registry;

/**
 * A fluid stack that can be configured.
 */
public class ConfigurableFluidStack extends AbstractConfigurableStack<Fluid, FluidVariant> {
    private long capacity;

    public ConfigurableFluidStack(long capacity) {
        super();
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
        stack.key = FluidVariant.of(fluid);
        stack.lockedInstance = fluid;
        stack.playerInsert = true;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack lockedOutputSlot(long capacity, Fluid fluid) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.key = FluidVariant.of(fluid);
        stack.lockedInstance = fluid;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesExtract = true;
        return stack;
    }

    public ConfigurableFluidStack(ConfigurableFluidStack other) {
        super(other);
        this.capacity = other.capacity;
    }

    public ConfigurableFluidStack(NbtCompound compound) {
        super(compound);
        this.capacity = compound.getLong("capacity");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ConfigurableFluidStack that = (ConfigurableFluidStack) o;
        return capacity == that.capacity;
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

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    protected Fluid getEmptyInstance() {
        return Fluids.EMPTY;
    }

    @Override
    protected Registry<Fluid> getRegistry() {
        return Registry.FLUID;
    }

    @Override
    protected FluidVariant readVariantFromNbt(NbtCompound compound) {
        return FluidVariant.fromNbt(compound);
    }

    public long getCapacity() {
        return capacity;
    }

    @Override
    protected long getRemainingCapacityFor(FluidVariant key) {
        return getRemainingSpace();
    }

    public void setAmount(long amount) {
        super.setAmount(amount);
        if (amount > capacity)
            throw new IllegalStateException("amount > capacity in the fluid stack");
        if (amount < 0)
            throw new IllegalStateException("amount < 0 in the fluid stack");
    }

    public void setCapacity(long capacity) {
        Preconditions.checkArgument(capacity >= 0, "Fluid Capacity must be > 0");
        this.capacity = capacity;
        if (amount > capacity)
            amount = capacity;
    }

    public long getRemainingSpace() {
        return capacity - amount;
    }

    public NbtCompound toNbt() {
        NbtCompound tag = super.toNbt();
        tag.putLong("capacity", capacity);
        return tag;
    }

    public class ConfigurableFluidSlot extends Slot implements ReiDraggable {
        private final Runnable markDirty;

        public ConfigurableFluidSlot(ConfigurableFluidSlot other) {
            this(other.markDirty, other.x, other.y);

            this.id = other.id;
        }

        public ConfigurableFluidSlot(Runnable markDirty, int x, int y) {
            super(new UnsupportedOperationInventory(), -1, x, y);

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

        public boolean canInsertFluid(FluidVariant fluid) {
            FluidVariant storedFluid = getConfStack().getResource();
            return playerInsert && isResourceAllowedByLock(fluid.getFluid()) && (storedFluid.isBlank() || storedFluid.equals(fluid));
        }

        public boolean canExtractFluid(FluidVariant fluid) {
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

        @Override
        public boolean dragFluid(FluidVariant fluidKey, Simulation simulation) {
            return playerLock(fluidKey.getFluid(), simulation);
        }

        @Override
        public boolean dragItem(ItemVariant itemKey, Simulation simulation) {
            return false;
        }
    }
}

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.registry.Registry;

/**
 * An item stack that can be configured.
 */
public class ConfigurableItemStack extends AbstractConfigurableStack<Item, ItemVariant> {
    private int adjustedCapacity = 64;

    public ConfigurableItemStack() {
    }

    public ConfigurableItemStack(NbtCompound compound) {
        super(compound);
        this.adjustedCapacity = compound.getInt("adjCap");
    }

    @Override
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        nbt.putInt("adjCap", this.adjustedCapacity);
        return nbt;
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
        super(other);
        this.adjustedCapacity = other.adjustedCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ConfigurableItemStack that = (ConfigurableItemStack) o;
        return adjustedCapacity == that.adjustedCapacity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), adjustedCapacity);
    }

    @Override
    protected ItemVariant getBlankVariant() {
        return ItemVariant.blank();
    }

    @Override
    protected Item getEmptyInstance() {
        return Items.AIR;
    }

    @Override
    protected Registry<Item> getRegistry() {
        return Registry.ITEM;
    }

    @Override
    protected ItemVariant readVariantFromNbt(NbtCompound compound) {
        return ItemVariant.fromNbt(compound);
    }

    @Override
    public long getCapacity() {
        return key.isBlank() ? adjustedCapacity : Math.min(adjustedCapacity, key.getItem().getMaxCount());
    }

    @Override
    public long getRemainingCapacityFor(ItemVariant key) {
        return Math.min(key.getItem().getMaxCount(), adjustedCapacity) - amount;
    }

    @Override
    public void setAmount(long amount) {
        super.setAmount(amount);
        if (adjustedCapacity < amount) {
            adjustedCapacity = (int) amount;
        }
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

    public boolean isValid(ItemStack stack) {
        return isResourceAllowedByLock(stack.getItem());
    }

    public void adjustCapacity(boolean isIncrease, boolean isShiftDown) {
        int delta = isShiftDown ? 8 : 1;
        if (!isIncrease) {
            delta = -delta;
        }
        adjustedCapacity = Math.min(64, Math.max((int) amount, adjustedCapacity + delta));
    }

    public int getAdjustedCapacity() {
        return adjustedCapacity;
    }

    public class ConfigurableItemSlot extends Slot implements ReiDraggable {
        private final Predicate<ItemStack> insertPredicate;
        private final Runnable markDirty;
        // Vanilla MC code modifies the stack returned by `getStack()` directly, but it
        // calls `markDirty()` when that happens, so we just cache the returned stack,
        // and set it when `markDirty()` is called.
        private ItemStack cachedReturnedStack = null;

        public ConfigurableItemSlot(ConfigurableItemSlot other) {
            this(other.markDirty, other.x, other.y, other.insertPredicate);

            this.id = other.id;
        }

        public ConfigurableItemSlot(Runnable markDirty, int x, int y, Predicate<ItemStack> insertPredicate) {
            super(new UnsupportedOperationInventory(), 0, x, y);

            this.insertPredicate = insertPredicate;
            this.markDirty = markDirty;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return playerInsert && ConfigurableItemStack.this.isValid(stack) && insertPredicate.test(stack);
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
            return cachedReturnedStack = key.toStack((int) amount);
        }

        @Override
        public void setStack(ItemStack stack) {
            key = ItemVariant.of(stack);
            amount = stack.getCount();
            markDirty.run();
            cachedReturnedStack = stack;
        }

        @Override
        public void markDirty() {
            if (cachedReturnedStack != null) {
                setStack(cachedReturnedStack);
            }
        }

        @Override
        public int getMaxItemCount() {
            return adjustedCapacity;
        }

        @Override
        public ItemStack takeStack(int amount) {
            ItemStack stack = key.toStack(amount);
            decrement(amount);
            cachedReturnedStack = null;
            markDirty.run();
            return stack;
        }

        @Override
        public boolean dragFluid(FluidVariant fluidKey, Simulation simulation) {
            return false;
        }

        @Override
        public boolean dragItem(ItemVariant itemKey, Simulation simulation) {
            return playerLock(itemKey.getItem(), simulation);
        }
    }
}

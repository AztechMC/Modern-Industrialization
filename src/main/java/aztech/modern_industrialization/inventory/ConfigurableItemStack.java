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

import aztech.modern_industrialization.api.machine.component.ItemAccess;
import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * An item stack that can be configured.
 */
public class ConfigurableItemStack extends AbstractConfigurableStack<Item, ItemVariant> implements ItemAccess {
    private int adjustedCapacity = 64;

    public ConfigurableItemStack() {
    }

    public ConfigurableItemStack(CompoundTag compound) {
        super(compound);
        this.adjustedCapacity = compound.getInt("adjCap");
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag nbt = super.toNbt();
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

    public static ConfigurableItemStack lockedInputSlot(Item item) {
        ConfigurableItemStack stack = new ConfigurableItemStack();
        stack.key = ItemVariant.of(item);
        stack.lockedInstance = item;
        stack.playerInsert = true;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesInsert = true;
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
        return BuiltInRegistries.ITEM;
    }

    @Override
    protected ItemVariant readVariantFromNbt(CompoundTag compound) {
        return ItemVariant.fromNbt(compound);
    }

    @Override
    public long getCapacity() {
        return key.isBlank() ? adjustedCapacity : Math.min(adjustedCapacity, key.getItem().getMaxStackSize());
    }

    @Override
    public long getRemainingCapacityFor(ItemVariant key) {
        return Math.min(key.getItem().getMaxStackSize(), adjustedCapacity) - amount;
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

    @Override
    public ItemVariant getVariant() {
        return getResource();
    }

    public class ConfigurableItemSlot extends HackySlot implements ReiDraggable, BackgroundRenderedSlot {
        private final Predicate<ItemStack> insertPredicate;
        private final Runnable markDirty;

        public ConfigurableItemSlot(ConfigurableItemSlot other) {
            this(other.markDirty, other.x, other.y, other.insertPredicate);

            this.index = other.index;
        }

        public ConfigurableItemSlot(Runnable markDirty, int x, int y, Predicate<ItemStack> insertPredicate) {
            super(x, y);

            this.insertPredicate = insertPredicate;
            this.markDirty = markDirty;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return playerInsert && ConfigurableItemStack.this.isValid(stack) && insertPredicate.test(stack);
        }

        @Override
        public boolean mayPickup(Player playerEntity) {
            return playerExtract;
        }

        public ConfigurableItemStack getConfStack() {
            return ConfigurableItemStack.this;
        }

        @Override
        protected ItemStack getRealStack() {
            return key.toStack((int) amount);
        }

        @Override
        protected void setRealStack(ItemStack stack) {
            key = ItemVariant.of(stack);
            amount = stack.getCount();
            notifyListeners();
            markDirty.run();
        }

        @Override
        public int getMaxStackSize() {
            return adjustedCapacity;
        }

        @Override
        public boolean dragFluid(FluidVariant fluidKey, Simulation simulation) {
            return false;
        }

        @Override
        public boolean dragItem(ItemVariant itemKey, Simulation simulation) {
            return playerLock(itemKey.getItem(), simulation);
        }

        @Override
        public int getBackgroundU() {
            return isPlayerLocked() ? 72 : isMachineLocked() ? 108 : 0;
        }
    }
}

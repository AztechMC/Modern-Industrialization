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
import aztech.modern_industrialization.util.MIExtraCodecs;
import aztech.modern_industrialization.util.Simulation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

/**
 * An item stack that can be configured.
 */
public class ConfigurableItemStack extends AbstractConfigurableStack<Item, ItemVariant> implements ItemAccess {
    // TODO: more efficient encoding?
    public static final Codec<ConfigurableItemStack> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                            ItemVariant.CODEC.fieldOf("key").forGetter(s -> s.key),
                            MIExtraCodecs.NON_NEGATIVE_LONG.fieldOf("amount").forGetter(s -> s.amount),
                            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("locked").forGetter(s -> Optional.ofNullable(s.lockedInstance)),
                            Codec.BOOL.fieldOf("machineLocked").forGetter(s -> s.machineLocked),
                            Codec.BOOL.fieldOf("playerLocked").forGetter(s -> s.playerLocked),
                            Codec.BOOL.fieldOf("playerLockable").forGetter(s -> s.playerLockable),
                            Codec.BOOL.fieldOf("playerInsert").forGetter(s -> s.playerInsert),
                            Codec.BOOL.fieldOf("playerExtract").forGetter(s -> s.playerExtract),
                            Codec.BOOL.fieldOf("pipesInsert").forGetter(s -> s.pipesInsert),
                            Codec.BOOL.fieldOf("pipesExtract").forGetter(s -> s.pipesExtract),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("adjCap").forGetter(s -> s.adjustedCapacity))
                    .apply(i, ConfigurableItemStack::new));
    public static final StreamCodec<ByteBuf, ConfigurableItemStack> STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(CODEC);
    public static final StreamCodec<ByteBuf, List<ConfigurableItemStack>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    private int adjustedCapacity = 64;

    public ConfigurableItemStack() {}

    private ConfigurableItemStack(ItemVariant key, long amount, Optional<Item> lockedInstance, boolean playerLocked, boolean machineLocked, boolean playerLockable, boolean playerInsert, boolean playerExtract, boolean pipesInsert, boolean pipesExtract, int adjustedCapacity) {
        super(key, amount, lockedInstance.orElse(null), playerLocked, machineLocked, playerLockable, playerInsert, playerExtract, pipesInsert, pipesExtract);
        this.adjustedCapacity = adjustedCapacity;
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
    public boolean equals(@Nullable Object o) {
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
    protected ItemVariant readVariantFromNbt(CompoundTag compound, HolderLookup.Provider registries) {
        return ItemVariant.fromNbt(compound, registries);
    }

    @Override
    public long getCapacity() {
        return key.isBlank() ? adjustedCapacity : Math.min(adjustedCapacity, key.getMaxStackSize());
    }

    @Override
    public long getRemainingCapacityFor(ItemVariant key) {
        if (adjustedCapacity < amount) {
            return 0; // Make sure we don't get negative counts if this happens!
        }
        return Math.min(key.getMaxStackSize(), adjustedCapacity) - amount;
    }

    @Override
    public long getTotalCapacityFor(Item instance) {
        return Math.min(ItemVariant.of(instance).getMaxStackSize(), adjustedCapacity);
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
        adjustedCapacity = Mth.clamp(adjustedCapacity + delta, 0, 64);
        notifyListeners();
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

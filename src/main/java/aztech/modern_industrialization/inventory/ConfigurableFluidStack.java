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

import aztech.modern_industrialization.api.machine.component.FluidAccess;
import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.util.MIExtraCodecs;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.UnsupportedOperationInventory;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * A fluid stack that can be configured.
 */
public class ConfigurableFluidStack extends AbstractConfigurableStack<Fluid, FluidResource> implements FluidAccess {
    // TODO: more efficient encoding?
    public static final Codec<ConfigurableFluidStack> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    FluidResource.OPTIONAL_CODEC.fieldOf("key").forGetter(s -> s.key),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("amount").forGetter(s -> s.amount),
                    BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("locked").forGetter(s -> Optional.ofNullable(s.lockedInstance)),
                    Codec.BOOL.fieldOf("machineLocked").forGetter(s -> s.machineLocked),
                    Codec.BOOL.fieldOf("playerLocked").forGetter(s -> s.playerLocked),
                    Codec.BOOL.fieldOf("playerLockable").forGetter(s -> s.playerLockable),
                    Codec.BOOL.fieldOf("playerInsert").forGetter(s -> s.playerInsert),
                    Codec.BOOL.fieldOf("playerExtract").forGetter(s -> s.playerExtract),
                    Codec.BOOL.fieldOf("pipesInsert").forGetter(s -> s.pipesInsert),
                    Codec.BOOL.fieldOf("pipesExtract").forGetter(s -> s.pipesExtract),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(s -> s.capacity))
                    .apply(i, ConfigurableFluidStack::new));
    public static final StreamCodec<ByteBuf, ConfigurableFluidStack> STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(CODEC);
    public static final StreamCodec<ByteBuf, List<ConfigurableFluidStack>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    private int capacity;

    public ConfigurableFluidStack(int capacity) {
        super();
        this.capacity = capacity;
    }

    public static ConfigurableFluidStack standardInputSlot(int capacity) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.playerInsert = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack standardOutputSlot(int capacity) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.pipesExtract = true;
        return stack;
    }

    public static ConfigurableFluidStack standardIOSlot(int capacity, boolean pipeIO) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.playerInsert = true;
        if (pipeIO) {
            stack.pipesInsert = true;
            stack.pipesExtract = true;
        }
        return stack;
    }

    public static ConfigurableFluidStack lockedInputSlot(int capacity, Fluid fluid) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.key = FluidResource.of(fluid);
        stack.lockedInstance = fluid;
        stack.playerInsert = true;
        stack.playerLockable = false;
        stack.playerLocked = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableFluidStack lockedOutputSlot(int capacity, Fluid fluid) {
        ConfigurableFluidStack stack = new ConfigurableFluidStack(capacity);
        stack.key = FluidResource.of(fluid);
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

    private ConfigurableFluidStack(FluidResource key, int amount, Optional<Fluid> lockedInstance, boolean playerLocked, boolean machineLocked, boolean playerLockable, boolean playerInsert, boolean playerExtract, boolean pipesInsert, boolean pipesExtract, int capacity) {
        super(key, amount, lockedInstance.orElse(null), playerLocked, machineLocked, playerLockable, playerInsert, playerExtract, pipesInsert, pipesExtract);
        this.capacity = capacity;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ConfigurableFluidStack that = (ConfigurableFluidStack) o;
        return capacity == that.capacity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), capacity);
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
    protected FluidResource getBlankVariant() {
        return FluidResource.EMPTY;
    }

    @Override
    protected Fluid getEmptyInstance() {
        return Fluids.EMPTY;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    protected int getRemainingCapacityFor(FluidResource key) {
        return getRemainingSpace();
    }

    @Override
    public int getTotalCapacityFor(Fluid instance) {
        return capacity;
    }

    public void setAmount(int amount) {
        super.setAmount(amount);
        if (amount > capacity)
            throw new IllegalStateException("amount > capacity in the fluid stack");
        if (amount < 0)
            throw new IllegalStateException("amount < 0 in the fluid stack");
    }

    public void setCapacity(int capacity) {
        Preconditions.checkArgument(capacity >= 0, "Fluid Capacity must be > 0");
        this.capacity = capacity;
        if (amount > capacity)
            amount = capacity;
    }

    public int getRemainingSpace() {
        return capacity - amount;
    }

    @Override
    public FluidResource getVariant() {
        return FluidResource.of(getResource().toStack(1));
    }

    public class ConfigurableFluidSlot extends Slot implements ReiDraggable, BackgroundRenderedSlot {
        private final Runnable markDirty;

        public ConfigurableFluidSlot(ConfigurableFluidSlot other) {
            this(other.markDirty, other.x, other.y);

            this.index = other.index;
        }

        public ConfigurableFluidSlot(Runnable markDirty, int x, int y) {
            super(new UnsupportedOperationInventory(), -1, x, y);

            this.markDirty = markDirty;
        }

        // We don't allow item insertion obviously.
        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        // No extraction either.
        @Override
        public boolean mayPickup(Player playerEntity) {
            return false;
        }

        public boolean canInsertFluid(FluidResource fluid) {
            FluidResource storedFluid = getConfStack().getResource();
            return playerInsert && isResourceAllowedByLock(fluid.getFluid()) && (storedFluid.isEmpty() || storedFluid.equals(fluid));
        }

        public boolean canExtractFluid(FluidResource fluid) {
            return playerExtract;
        }

        public ConfigurableFluidStack getConfStack() {
            return ConfigurableFluidStack.this;
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {}

        @Override
        public void setChanged() {
            markDirty.run();
        }

        @Override
        public boolean dragFluid(FluidResource fluidResource, Simulation simulation) {
            return playerLock(fluidResource.getFluid(), simulation);
        }

        @Override
        public boolean dragItem(ItemResource itemResource, Simulation simulation) {
            return false;
        }

        @Override
        public int getBackgroundU() {
            return isPlayerLocked() ? 90 : isMachineLocked() ? 126 : 18;
        }

        public boolean playerInteract(SlotAccess slot, Player player, boolean allowSlotExtract) {
            var fluidHandlerItem = FluidUtil.getFluidHandler(slot.get()).orElse(null);
            if (fluidHandlerItem == null) {
                return false;
            }

            // Copy contents into temporary IFluidHandler
            var slotTank = new FluidTank(Ints.saturatedCast(getCapacity()), fs -> canInsertFluid(FluidResource.of(fs)));
            slotTank.setFluid(getVariant().toStack(Ints.saturatedCast(getAmount())));

            // Extract first
            var extractResult = FluidUtil.tryEmptyContainerAndStow(
                    slot.get(),
                    slotTank,
                    new PlayerMainInvWrapper(player.getInventory()),
                    Integer.MAX_VALUE,
                    player,
                    true);
            if (extractResult.isSuccess()) {
                slot.set(extractResult.getResult());
                setKey(FluidResource.of(slotTank.getFluid()));
                setAmount(slotTank.getFluidAmount());
                return true;
            }

            // Otherwise insert
            if (!allowSlotExtract || isEmpty() || !canExtractFluid(getResource())) {
                return false;
            }

            var insertResult = FluidUtil.tryFillContainerAndStow(
                    slot.get(),
                    slotTank,
                    new PlayerMainInvWrapper(player.getInventory()),
                    Integer.MAX_VALUE,
                    player,
                    true);
            if (insertResult.isSuccess()) {
                slot.set(insertResult.getResult());
                setKey(FluidResource.of(slotTank.getFluid()));
                setAmount(slotTank.getFluidAmount());
                return true;
            }

            return false;
        }
    }
}

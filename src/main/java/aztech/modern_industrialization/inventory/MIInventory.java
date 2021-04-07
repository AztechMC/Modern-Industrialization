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

import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import alexiil.mc.lib.attributes.item.ItemExtractable;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import alexiil.mc.lib.attributes.item.ItemInvUtil;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.transferapi.impl.compat.WrappedFluidStorage;
import aztech.modern_industrialization.transferapi.impl.compat.WrappedItemStorage;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class MIInventory implements IComponent {
    public static final MIInventory EMPTY;

    public final List<ConfigurableItemStack> itemStacks;
    public final List<ConfigurableFluidStack> fluidStacks;
    public final MIItemStorage itemStorage;
    public final MIFluidStorage fluidStorage;
    public final SlotPositions itemPositions;
    public final SlotPositions fluidPositions;

    public MIInventory(List<ConfigurableItemStack> itemStacks, List<ConfigurableFluidStack> fluidStacks, SlotPositions itemPositions,
            SlotPositions fluidPositions) {
        // Must be array lists to allow using .set() in readNbt()
        this.itemStacks = new ArrayList<>(itemStacks);
        this.fluidStacks = new ArrayList<>(fluidStacks);
        this.itemStorage = new MIItemStorage(itemStacks);
        this.fluidStorage = new MIFluidStorage(fluidStacks);
        this.itemPositions = itemPositions;
        this.fluidPositions = fluidPositions;
        if (itemPositions.size() != itemStacks.size()) {
            throw new IllegalArgumentException(
                    "Mismatched item slots and positions. Slot count: " + itemStacks.size() + ", position count: " + itemPositions.size());
        }
        if (fluidPositions.size() != fluidStacks.size()) {
            throw new IllegalArgumentException(
                    "Mismatched fluid slots and positions. Slot count: " + fluidStacks.size() + ", position count: " + fluidPositions.size());
        }
    }

    public void autoExtractItems(World world, BlockPos pos, Direction direction) {
        // LBA
        ItemInsertable target = ItemAttributes.INSERTABLE.get(world, pos.offset(direction), SearchOptions.inDirection(direction));
        ItemInvUtil.moveMultiple(new WrappedItemStorage(itemStorage), target);
    }

    public void autoExtractFluids(World world, BlockPos pos, Direction direction) {
        // LBA
        FluidInsertable target = FluidAttributes.INSERTABLE.get(world, pos.offset(direction), SearchOptions.inDirection(direction));
        FluidVolumeUtil.move(new WrappedFluidStorage(fluidStorage), target);
    }

    public void autoInsertItems(World world, BlockPos pos, Direction direction) {
        // LBA
        ItemExtractable target = ItemAttributes.EXTRACTABLE.get(world, pos.offset(direction), SearchOptions.inDirection(direction));
        ItemInvUtil.moveMultiple(target, new WrappedItemStorage(itemStorage));
    }

    public void autoInsertFluids(World world, BlockPos pos, Direction direction) {
        // LBA
        FluidExtractable target = FluidAttributes.EXTRACTABLE.get(world, pos.offset(direction), SearchOptions.inDirection(direction));
        FluidVolumeUtil.move(target, new WrappedFluidStorage(fluidStorage));
    }

    public void writeNbt(CompoundTag tag) {
        NbtHelper.putList(tag, "items", itemStacks, ConfigurableItemStack::toNbt);
        NbtHelper.putList(tag, "fluids", fluidStacks, ConfigurableFluidStack::toNbt);
    }

    public void readNbt(CompoundTag tag) {
        List<ConfigurableItemStack> newItemStacks = new ArrayList<>();
        List<ConfigurableFluidStack> newFluidStacks = new ArrayList<>();

        NbtHelper.getList(tag, "items", newItemStacks, ConfigurableItemStack::fromNbt);
        NbtHelper.getList(tag, "fluids", newFluidStacks, ConfigurableFluidStack::fromNbt);

        SlotConfig.readSlotList(itemStacks, newItemStacks);
        SlotConfig.readSlotList(fluidStacks, newFluidStacks);
    }

    static {
        EMPTY = new MIInventory(Collections.emptyList(), Collections.emptyList(), SlotPositions.empty(), SlotPositions.empty());
    }
}

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

import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.StorageUtil2;
import dev.technici4n.fasttransferlib.experimental.api.item.ItemKey;
import dev.technici4n.fasttransferlib.experimental.api.item.ItemStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class MIInventory implements IComponent {
    public static final MIInventory EMPTY;

    public final MIItemStorage itemStorage;
    public final MIFluidStorage fluidStorage;
    public final SlotPositions itemPositions;
    public final SlotPositions fluidPositions;

    /**
     * Build a new MI inventory. If you need to access the stacks, make sure to
     * reference them through this inventory and not directly!
     */
    public MIInventory(List<ConfigurableItemStack> itemStacks, List<ConfigurableFluidStack> fluidStacks, SlotPositions itemPositions,
            SlotPositions fluidPositions) {
        // Must be array lists to allow using .set() in readNbt()
        this.itemStorage = new MIItemStorage(new ArrayList<>(itemStacks));
        this.fluidStorage = new MIFluidStorage(new ArrayList<>(fluidStacks));
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

    public List<ConfigurableItemStack> getItemStacks() {
        return itemStorage.stacks;
    }

    public List<ConfigurableFluidStack> getFluidStacks() {
        return fluidStorage.stacks;
    }

    public void autoExtractItems(World world, BlockPos pos, Direction direction) {
        Storage<ItemKey> target = ItemStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());
        target = StorageUtil2.wrapInventory(target);

        if (target != null) {
            StorageUtil.move(itemStorage, target, k -> true, Long.MAX_VALUE, null);
        }
    }

    public void autoExtractFluids(World world, BlockPos pos, Direction direction) {
        Storage<FluidKey> target = FluidStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());

        if (target != null) {
            StorageUtil.move(fluidStorage, target, k -> true, Long.MAX_VALUE, null);
        }
    }

    public void autoInsertItems(World world, BlockPos pos, Direction direction) {
        Storage<ItemKey> target = ItemStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());

        if (target != null) {
            StorageUtil.move(target, itemStorage, k -> true, Long.MAX_VALUE, null);
        }
    }

    public void autoInsertFluids(World world, BlockPos pos, Direction direction) {
        Storage<FluidKey> target = FluidStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());

        if (target != null) {
            StorageUtil.move(target, fluidStorage, k -> true, Long.MAX_VALUE, null);
        }
    }

    public void writeNbt(NbtCompound tag) {
        NbtHelper.putList(tag, "items", itemStorage.stacks, ConfigurableItemStack::toNbt);
        NbtHelper.putList(tag, "fluids", fluidStorage.stacks, ConfigurableFluidStack::toNbt);
    }

    public void readNbt(NbtCompound tag) {
        List<ConfigurableItemStack> newItemStacks = new ArrayList<>();
        List<ConfigurableFluidStack> newFluidStacks = new ArrayList<>();

        NbtHelper.getList(tag, "items", newItemStacks, ConfigurableItemStack::new);
        NbtHelper.getList(tag, "fluids", newFluidStacks, ConfigurableFluidStack::new);

        SlotConfig.readSlotList(itemStorage.stacks, newItemStacks);
        SlotConfig.readSlotList(fluidStorage.stacks, newFluidStacks);
    }

    static {
        EMPTY = new MIInventory(Collections.emptyList(), Collections.emptyList(), SlotPositions.empty(), SlotPositions.empty());
    }
}

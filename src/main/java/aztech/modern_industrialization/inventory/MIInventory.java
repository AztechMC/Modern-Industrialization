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
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidApi;
import net.fabricmc.fabric.api.transfer.v1.item.ItemApi;
import net.fabricmc.fabric.api.transfer.v1.storage.Movement;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class MIInventory {
    public static final MIInventory EMPTY;

    public final List<ConfigurableItemStack> itemStacks;
    public final List<ConfigurableFluidStack> fluidStacks;
    public final MIItemStorage itemStorage;
    public final MIFluidStorage fluidStorage;
    public final SlotPositions itemPositions;
    public final SlotPositions fluidPositions;

    public MIInventory(List<ConfigurableItemStack> itemStacks, List<ConfigurableFluidStack> fluidStacks, SlotPositions itemPositions,
            SlotPositions fluidPositions) {
        this.itemStacks = itemStacks;
        this.fluidStacks = fluidStacks;
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
        Storage<ItemKey> target = ItemApi.SIDED.get(world, pos.offset(direction), direction.getOpposite());
        if (target != null) {
            autoExtractItems(target);
        }
    }

    public void autoExtractItems(Storage<ItemKey> target) {
        Movement.move(itemStorage, target, k -> true, Integer.MAX_VALUE);
    }

    public void autoExtractFluids(World world, BlockPos pos, Direction direction) {
        Storage<Fluid> target = FluidApi.SIDED.get(world, pos.offset(direction), direction.getOpposite());
        if (target != null) {
            Movement.move(fluidStorage, target, f -> true, Integer.MAX_VALUE);
        }
    }

    public void writeNbt(CompoundTag tag) {
        NbtHelper.putList(tag, "items", itemStacks, ConfigurableItemStack::writeToTag);
        NbtHelper.putList(tag, "fluids", fluidStacks, ConfigurableFluidStack::writeToTag);
    }

    public void readNbt(CompoundTag tag) {
        // This is a failsafe in case the number of slots in a machine changed
        // When this happens, we destroy all items/fluids, but at least we don't crash
        // the world.
        // TODO: find a better solution?
        List<ConfigurableItemStack> itemStacksCopy = ConfigurableItemStack.copyList(itemStacks);
        List<ConfigurableFluidStack> fluidStacksCopy = ConfigurableFluidStack.copyList(fluidStacks);

        NbtHelper.getList(tag, "items", itemStacks, ConfigurableItemStack::readFromTag);
        NbtHelper.getList(tag, "fluids", fluidStacks, ConfigurableFluidStack::readFromTag);

        if (itemStacksCopy.size() != itemStacks.size()) {
            itemStacks.clear();
            itemStacks.addAll(itemStacksCopy);
        }
        if (fluidStacksCopy.size() != fluidStacks.size()) {
            fluidStacks.clear();
            fluidStacks.addAll(fluidStacksCopy);
        }
    }

    static {
        EMPTY = new MIInventory(Collections.emptyList(), Collections.emptyList(), SlotPositions.empty(), SlotPositions.empty());
    }
}

package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.fluid.FluidInventory;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic configurable inventory class.
 * Don't forget to call the nbt serialization functions!
 */
public interface ConfigurableInventory extends Inventory, SidedInventory, FluidInventory {
    /**
     * The item stack list, must always be the same. It will be mutated by the inventory.
     */
    List<ConfigurableItemStack> getItemStacks();

    /**
     * The fluid stack list, must always be the same. It will be mutated by the inventory.
     */
    List<ConfigurableFluidStack> getFluidStacks();

    /**
     * Whether the inventory is currently open, i.e. a player can see its contents.
     */
    boolean isOpen();

    @Override
    default int size() {
        return getItemStacks().size();
    }

    @Override
    default boolean isEmpty() {
        return false;
    }

    @Override
    default ItemStack getStack(int index) {
        return getItemStacks().get(index).stack;
    }

    @Override
    default ItemStack removeStack(int slot, int count) {
        ItemStack result = getItemStacks().get(slot).splitStack(count);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    default ItemStack removeStack(int slot) {
        return getItemStacks().get(slot).removeStack();
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        stack.setCount(Math.min(stack.getCount(), getMaxCountPerStack()));
        getItemStacks().get(slot).setStack(stack);
    }

    @Override
    default int getMaxCountPerStack() {
        return 64;
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    default int[] getAvailableSlots(Direction side) {
        int[] ret = new int[getItemStacks().size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = i;
        }
        return ret;
    }

    @Override
    default boolean canInsert(int slot, ItemStack stack, Direction dir) {
        ConfigurableItemStack itemStack = getItemStacks().get(slot);
        return itemStack.pipesInsert && itemStack.canInsert(stack, dir);
    }

    @Override
    default boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return getItemStacks().get(slot).pipesExtract;
    }

    default void autoExtractItems(Direction direction) {
        // TODO this is the hook to auto-extract items, must yet be implemented and called!
    }

    @Override
    default int insert(Direction direction, Fluid fluid, int maxAmount, boolean simulate) {
        int inserted = insert(direction, fluid, maxAmount, simulate, 0);
        if (inserted > 0 && !simulate) {
            markDirty();
        }
        return inserted;
    }

    @Deprecated // Don't call this function directly
    default int insert(Direction direction, Fluid fluid, int maxAmount, boolean simulate, int firstStack) {
        if (firstStack == 0) {
            // Try to find a slot that contains the fluid already.
            while (firstStack < getFluidStacks().size()) {
                ConfigurableFluidStack fluidStack = getFluidStacks().get(firstStack);
                if(!fluidStack.pipesInsert) {
                    firstStack++;
                    continue;
                }
                if (
                        (fluidStack.getFluid() == Fluids.EMPTY && fluidStack.steamInput && fluidStack.canInsertFluid(fluid))
                                || (fluid == fluidStack.getFluid())
                ) break;
                firstStack++;
            }
        }
        for (int i = firstStack; i < getFluidStacks().size(); i++) {
            ConfigurableFluidStack fluidStack = getFluidStacks().get(i);
            if (!fluidStack.pipesInsert) continue;
            if (fluidStack.canInsertFluid(fluid)) {
                int ins = Math.min(maxAmount, fluidStack.getRemainingSpace());
                if (!simulate) {
                    fluidStack.setFluid(fluid);
                    fluidStack.increment(ins);
                    if (isOpen()) {
                        fluidStack.updateDisplayedItem();
                    }
                }
                return fluidStack.steamInput ? ins + insert(direction, fluid, maxAmount - ins, simulate, i + 1) : ins;
            }
        }
        return 0;
    }

    @Override
    default int extract(Direction direction, Fluid fluid, int maxAmount, boolean simulate) {
        for (int i = getFluidStacks().size(); i-- > 0; ) {
            ConfigurableFluidStack fluidStack = getFluidStacks().get(i);
            if (!fluidStack.pipesExtract) continue;
            if (fluidStack.getFluid() == fluid) {
                int ext = Math.min(maxAmount, fluidStack.getAmount());
                if (!simulate) {
                    fluidStack.decrement(ext);
                    markDirty();
                }
                return ext;
            }
        }
        return 0;
    }

    @Override
    default Fluid[] getExtractableFluids(Direction direction) {
        List<Fluid> extractableFluids = new ArrayList<>();
        for (int i = getFluidStacks().size(); i-- > 0; ) {
            ConfigurableFluidStack fluidStack = getFluidStacks().get(i);
            if (!fluidStack.pipesExtract) continue;
            if (fluidStack.getFluid() != Fluids.EMPTY) {
                extractableFluids.add(fluidStack.getFluid());
            }
        }
        return extractableFluids.toArray(new Fluid[0]);
    }

    @Override
    default boolean canFluidContainerConnect(Direction direction) {
        return getFluidStacks().size() > 0;
    }

    default void writeToTag(CompoundTag tag) {
        NbtHelper.putList(tag, "items", getItemStacks(), ConfigurableItemStack::writeToTag);
        NbtHelper.putList(tag, "fluids", getFluidStacks(), ConfigurableFluidStack::writeToTag);
    }

    default void readFromTag(CompoundTag tag) {
        NbtHelper.getList(tag, "items", getItemStacks(), ConfigurableItemStack::readFromTag);
        NbtHelper.getList(tag, "fluids", getFluidStacks(), ConfigurableFluidStack::readFromTag);
    }
}

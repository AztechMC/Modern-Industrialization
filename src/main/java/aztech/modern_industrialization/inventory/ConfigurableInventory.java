package aztech.modern_industrialization.inventory;

import alexiil.mc.lib.attributes.SearchOption;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.FluidTransferable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import aztech.modern_industrialization.util.ItemStackHelper;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.math.RoundingMode;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static alexiil.mc.lib.attributes.Simulation.ACTION;

/**
 * A generic configurable inventory class.
 * Don't forget to call the nbt serialization functions!
 */
public interface ConfigurableInventory extends Inventory, SidedInventory, FluidTransferable {
    /**
     * The item stack list, must always be the same. It will be mutated by the inventory.
     */
    List<ConfigurableItemStack> getItemStacks();

    /**
     * The fluid stack list, must always be the same. It will be mutated by the inventory.
     */
    List<ConfigurableFluidStack> getFluidStacks();

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
        return 64; // WARNING: never change this!!!
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
        return itemStack.pipesInsert && itemStack.canInsert(stack);
    }

    @Override
    default boolean isValid(int slot, ItemStack stack) {
        return getItemStacks().get(slot).canInsert(stack);
    }

    @Override
    default boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return getItemStacks().get(slot).pipesExtract;
    }

    default void autoExtractItems(World world, BlockPos pos, Direction direction) {
        SearchOption option = SearchOptions.inDirection(direction);
        if(ItemAttributes.INSERTABLE.getAll(world, pos.offset(direction), option).hasOfferedAny()) {
            ItemInsertable insertable = ItemAttributes.INSERTABLE.get(world, pos.offset(direction), option);
            for(ConfigurableItemStack stack : getItemStacks()) {
                if(stack.pipesExtract && !stack.stack.isEmpty()) {
                    stack.stack = insertable.insert(stack.stack);
                    markDirty();
                }
            }
        }
    }

    default void autoExtractFluids(World world, BlockPos pos, Direction direction) {
        SearchOption option = SearchOptions.inDirection(direction);
        if(FluidAttributes.INSERTABLE.getAll(world, pos.offset(direction), option).hasOfferedAny()) {
            FluidInsertable insertable = FluidAttributes.INSERTABLE.get(world, pos.offset(direction), option);
            for(ConfigurableFluidStack stack : getFluidStacks()) {
                if(!stack.getFluid().isEmpty() && stack.pipesExtract) {
                    FluidVolume leftover = insertable.attemptInsertion(stack.getFluid().withAmount(FluidAmount.of(stack.getAmount(), 1000)), ACTION);
                    stack.setAmount(leftover.amount().asInt(1000, RoundingMode.FLOOR));
                    markDirty();
                }
            }
        }
    }

    @Override
    default FluidVolume attemptInsertion(FluidVolume fluid, Simulation simulation) { // TODO: don't lose fluid
        int leftover = internalInsert(getFluidStacks(), fluid.getFluidKey(), fluid.amount().asInt(1000, RoundingMode.FLOOR), simulation, s -> s.pipesInsert, s -> {}, this::markDirty);
        return fluid.getFluidKey().withAmount(FluidAmount.of(leftover, 1000));
    }

    /**
     * Internal insert. Returns leftover fluid.
     */
    static int internalInsert(List<ConfigurableFluidStack> fluidStacks, FluidKey fluid, int amount, Simulation simulation, Predicate<ConfigurableFluidStack> stackFilter, Consumer<Integer> stackUpdater, Runnable markDirty) {
        int index = -1;
        // First, try to find a slot that contains the fluid. If we couldn't find one, we insert in any stack
        outer: for(int tries = 0; tries < 2; ++tries) {
            for(int i = 0; i < fluidStacks.size(); i++) {
                ConfigurableFluidStack stack = fluidStacks.get(i);
                if (stackFilter.test(stack) && stack.isFluidValid(fluid) && (tries == 1 || stack.getFluid() == fluid)) {
                    index = i;
                    break outer;
                }
            }
        }
        if(index == -1) return amount;
        ConfigurableFluidStack targetStack = fluidStacks.get(index);
        int ins = Math.min(amount, targetStack.getRemainingSpace());
        if (ins > 0) {
            if (simulation.isAction()) {
                targetStack.setFluid(fluid);
                targetStack.increment(ins);
                markDirty.run();
            }
            stackUpdater.accept(index);
        }
        return amount - ins;
    }

    @Override
    default FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
        for(ConfigurableFluidStack fluidStack : getFluidStacks()) {
            FluidKey fluid = fluidStack.getFluid();
            if(fluidStack.pipesExtract && !fluid.isEmpty() && filter.matches(fluid)) {
                int ext = Math.min(maxAmount.asInt(1000, RoundingMode.FLOOR), fluidStack.getAmount());
                if(simulation.isAction()) {
                    fluidStack.decrement(ext);
                    markDirty();
                }
                return fluid.withAmount(FluidAmount.of(ext, 1000));
            }
        }
        return FluidKeys.EMPTY.withAmount(FluidAmount.ZERO);
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

package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.util.NbtHelper;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.fluid.*;
import dev.technici4n.fasttransferlib.api.item.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MIInventory {
    public final List<ConfigurableItemStack> itemStacks;
    public final List<ConfigurableFluidStack> fluidStacks;
    private final MIItemView itemView;
    private final MIFluidView fluidView;
    private final Runnable markDirty;

    public MIInventory(List<ConfigurableItemStack> itemStacks, List<ConfigurableFluidStack> fluidStacks, Runnable markDirty) {
        this.itemStacks = itemStacks;
        this.fluidStacks = fluidStacks;
        this.itemView = itemStacks.size() > 0 ? new MIItemView() : null;
        this.fluidView = fluidStacks.size() > 0 ? new MIFluidView() : null;
        this.markDirty = markDirty;
    }

    public void autoExtractItems(World world, BlockPos pos, Direction direction) {
        ItemView view = ItemApi.SIDED_VIEW.get(world, pos.offset(direction), direction.getOpposite());
        if (view instanceof ItemInsertable) {
            autoExtractItems((ItemInsertable) view);
        }
    }

    public void autoExtractItems(ItemInsertable target) {
        ItemMovement.moveMultiple(itemView, target, Integer.MAX_VALUE);
    }

    public void autoExtractFluids(World world, BlockPos pos, Direction direction) {
        FluidView view = FluidApi.SIDED_VIEW.get(world, pos.offset(direction), direction.getOpposite());
        if (view instanceof FluidInsertable) {
            FluidMovement.moveRange(fluidView, (FluidInsertable) view, Long.MAX_VALUE);
        }
    }

    public void writeToTag(CompoundTag tag) {
        NbtHelper.putList(tag, "items", itemStacks, ConfigurableItemStack::writeToTag);
        NbtHelper.putList(tag, "fluids", fluidStacks, ConfigurableFluidStack::writeToTag);
    }

    public void readFromTag(CompoundTag tag) {
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

    public @Nullable MIItemView getItemView() {
        return itemView;
    }

    public @Nullable MIFluidView getFluidView() {
        return fluidView;
    }

    public class MIItemView implements ItemInsertable, ItemExtractable {
        private MIItemView() {}

        @Override
        public int extract(int slot, ItemKey key, int maxCount, Simulation simulation) {
            ConfigurableItemStack stack = itemStacks.get(slot);
            if (!stack.pipesExtract || stack.key != key) return 0;

            int extracted = Math.min(maxCount, stack.count);

            if (simulation.isActing()) {
                stack.count -= extracted;
                if (stack.count == 0) stack.key = ItemKey.EMPTY;

                markDirty.run();
            }

            return extracted;
        }

        @Override
        public int insert(ItemKey key, int count, Simulation simulation) {
            boolean success = false;

            for (int outer = 0; outer < 2 && !success; ++outer) {
                for (int i = 0; i < itemStacks.size(); ++i) {
                    ConfigurableItemStack stack = itemStacks.get(i);
                    if (stack.pipesInsert && stack.canInsert(key.getItem())) {
                        if (stack.key.equals(key) || (stack.key.isEmpty() && outer == 1)) {
                            int maxSlotInsert = Math.min(64, key.getItem().getMaxCount()) - stack.count;
                            int inserted = Math.min(count, maxSlotInsert);
                            if (inserted == 0) continue;

                            if (simulation.isActing()) {
                                stack.key = key;
                                stack.count += inserted;
                                markDirty.run();
                            }

                            count -= inserted;
                            success = true;
                        }
                    }
                }
            }

            return count;
        }

        @Override
        public int getItemSlotCount() {
            return itemStacks.size();
        }

        @Override
        public ItemKey getItemKey(int i) {
            return itemStacks.get(i).key;
        }

        @Override
        public int getItemCount(int i) {
            return itemStacks.get(i).count;
        }
    }

    public class MIFluidView implements FluidInsertable, FluidExtractable {
        private MIFluidView() {}

        @Override
        public long extract(int slot, Fluid fluid, long maxAmount, Simulation simulation) {
            ConfigurableFluidStack stack = fluidStacks.get(slot);
            if (!stack.pipesExtract || stack.getFluid() != fluid) return 0;

            long extracted = Math.min(maxAmount, stack.getAmount());

            if (simulation.isActing()) {
                stack.decrement(extracted);
                markDirty.run();
            }

            return extracted;
        }

        @Override
        public long insert(Fluid fluid, long amount, Simulation simulation) {
            for (int outer = 0; outer < 2; ++outer) {
                for (int i = 0; i < fluidStacks.size(); ++i) {
                    ConfigurableFluidStack stack = fluidStacks.get(i);
                    if (stack.pipesInsert && stack.isFluidValid(fluid)) {
                        if (stack.getFluid() == fluid || (stack.getFluid() == Fluids.EMPTY && outer == 1)) {
                            long inserted = Math.min(amount, stack.getRemainingSpace());
                            if (inserted == 0) continue;

                            if (simulation.isActing()) {
                                stack.setFluid(fluid);
                                stack.increment(inserted);
                                markDirty.run();
                            }

                            return amount - inserted;
                        }
                    }
                }
            }

            return amount;
        }

        @Override
        public int getFluidSlotCount() {
            return fluidStacks.size();
        }

        @Override
        public Fluid getFluid(int i) {
            return fluidStacks.get(i).getFluid();
        }

        @Override
        public long getFluidAmount(int i) {
            return fluidStacks.get(i).getAmount();
        }
    }
}

package aztech.modern_industrialization.blocks.tank;

import alexiil.mc.lib.attributes.AttributeProviderItem;
import alexiil.mc.lib.attributes.ItemAttributeList;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidTransferable;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.misc.AbstractItemBasedAttribute;
import alexiil.mc.lib.attributes.misc.LimitedConsumer;
import alexiil.mc.lib.attributes.misc.Reference;
import aztech.modern_industrialization.util.NbtHelper;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TankItem extends BlockItem implements AttributeProviderItem {
    public final int capacity;

    public TankItem(Block block, Settings settings, int capacity) {
        super(block, settings);
        this.capacity = capacity;
    }

    public boolean isEmpty(ItemStack stack) {
        return stack.getSubTag("BlockEntityTag") == null;
    }

    public FluidKey getFluid(ItemStack stack) {
        return NbtHelper.getFluidCompatible(stack.getSubTag("BlockEntityTag"), "fluid");
    }

    private void setFluid(ItemStack stack, FluidKey fluid) {
        stack.getSubTag("BlockEntityTag").put("fluid", fluid.toTag());
    }

    public int getAmount(ItemStack stack) {
        return Math.min(stack.getSubTag("BlockEntityTag").getInt("amount"), capacity);
    }

    private void setAmount(ItemStack stack, int amount) {
        stack.getSubTag("BlockEntityTag").putInt("amount", amount);
    }

    private void setCapacity(ItemStack stack, int capacity) {
        stack.getSubTag("BlockEntityTag").putInt("capacity", capacity);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
        if (!isEmpty(stack)) {
            tooltip.add(getFluid(stack).name);
            String quantity = getAmount(stack) + " / " + capacity;
            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_quantity", quantity).setStyle(style));
        } else {
            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_empty").setStyle(style));
        }
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state) {
        ((TankBlockEntity) world.getBlockEntity(pos)).setCapacity(capacity);
        return super.postPlacement(pos, world, player, stack, state);
    }

    @Override
    public void addAllAttributes(Reference<ItemStack> stack, LimitedConsumer<ItemStack> excess, ItemAttributeList<?> to) {
        to.offer(new TankFluidTransferable(stack, excess));
    }

    private class TankFluidTransferable extends AbstractItemBasedAttribute implements FluidTransferable {
        protected TankFluidTransferable(Reference<ItemStack> stackRef, LimitedConsumer<ItemStack> excessStacks) {
            super(stackRef, excessStacks);
        }

        @Override
        public FluidVolume attemptInsertion(FluidVolume fluid, Simulation simulation) {
            int inserted = 0;
            if (isEmpty(stackRef.get())) {
                inserted = Math.min(capacity, fluid.amount().asInt(1000, RoundingMode.FLOOR));
            } else if (!fluid.getFluidKey().isEmpty()) {
                FluidKey storedFluid = getFluid(stackRef.get());
                if (fluid.getFluidKey() == storedFluid) {
                    int amount = getAmount(stackRef.get());
                    inserted = Math.min(capacity - amount, fluid.amount().asInt(1000, RoundingMode.FLOOR));
                }
            }
            if (inserted > 0) {
                if (!sendStacks(fluid.getFluidKey(), inserted, simulation)) {
                    return fluid;
                }
            }
            return fluid.getFluidKey().withAmount(fluid.amount().sub(FluidAmount.of(inserted, 1000)));
        }

        @Override
        public FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
            if (isEmpty(stackRef.get()))
                return FluidVolumeUtil.EMPTY;

            FluidKey fluid = getFluid(stackRef.get());
            if (filter.matches(fluid)) {
                int amount = getAmount(stackRef.get());
                int ext = Math.min(amount, maxAmount.asInt(1000, RoundingMode.FLOOR));
                if (ext > 0) {
                    if (!sendStacks(fluid, amount - ext, simulation)) {
                        return FluidVolumeUtil.EMPTY;
                    } else {
                        return fluid.withAmount(FluidAmount.of(ext, 1000));
                    }
                }
            }
            return FluidVolumeUtil.EMPTY;
        }

        private boolean sendStacks(FluidKey newFluid, int newAmount, Simulation simulation) {
            List<ItemStack> resultingStacks = new ArrayList<>(2);
            ItemStack remainder = stackRef.get();
            if (remainder.getCount() > 1) {
                remainder = remainder.copy();
                remainder.decrement(1);
                resultingStacks.add(remainder);
            }

            ItemStack filledStack = new ItemStack(TankItem.this);
            if (newAmount != 0) {
                filledStack.getOrCreateSubTag("BlockEntityTag");
                setCapacity(filledStack, capacity);
                setFluid(filledStack, newFluid);
                setAmount(filledStack, newAmount);
            }
            resultingStacks.add(filledStack);

            if (stackRef.isValid(resultingStacks.get(0))) {
                if (simulation.isAction()) {
                    stackRef.set(resultingStacks.get(0));
                }
            } else {
                return false;
            }
            return resultingStacks.size() == 1 || excessStacks.offer(resultingStacks.get(1), simulation);
        }
    }
}

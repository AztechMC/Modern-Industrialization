package aztech.modern_industrialization.blocks.tank;

import aztech.modern_industrialization.fluid.FluidContainerItem;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class TankItem extends BlockItem implements FluidContainerItem {
    public final int capacity;

    public TankItem(Block block, Settings settings, int capacity) {
        super(block, settings);
        this.capacity = capacity;
    }

    @Override
    public int insertFluid(ItemStack stack, Fluid fluid, int maxAmount, Consumer<ItemStack> stackUpdate) {
        if(isEmpty(stack)) {
            int inserted = Math.min(capacity, maxAmount);
            if(inserted > 0) {
                sendRemainder(stack, stackUpdate);
                sendStack(fluid, inserted, stackUpdate);
            }
            return inserted;
        } else if(fluid != Fluids.EMPTY) {
            Fluid storedFluid = getFluid(stack);
            int amount = getAmount(stack);
            if(fluid == storedFluid) {
                int inserted = Math.min(capacity-amount, maxAmount);
                if(inserted > 0) {
                    sendRemainder(stack, stackUpdate);
                    sendStack(fluid, amount + inserted, stackUpdate);
                }
                return inserted;
            }
        }
        return 0;
    }

    @Override
    public int extractFluid(ItemStack stack, Fluid fluid, int maxAmount, Consumer<ItemStack> stackUpdate) {
        if (!isEmpty(stack)) {
            if (fluid == getFluid(stack)) {
                int amount = getAmount(stack);
                int extracted = Math.min(amount, maxAmount);
                if(extracted > 0) {
                    sendRemainder(stack, stackUpdate);
                    sendStack(fluid, amount - extracted, stackUpdate);
                }
                return extracted;
            }
        }
        return 0;
    }

    @Override
    public Fluid getExtractableFluid(ItemStack stack) {
        return isEmpty(stack) ? Fluids.EMPTY : getFluid(stack);
    }

    private void sendRemainder(ItemStack stack, Consumer<ItemStack> stackUpdate) {
        ItemStack remainderStack = stack.copy();
        remainderStack.decrement(1);
        if(!remainderStack.isEmpty()) {
            stackUpdate.accept(remainderStack);
        }
    }

    private void sendStack(Fluid fluid, int amount, Consumer<ItemStack> stackUpdate) {
        ItemStack filledStack = new ItemStack(this);
        if(amount != 0) {
            filledStack.getOrCreateSubTag("BlockEntityTag");
            setCapacity(filledStack, capacity);
            setFluid(filledStack, fluid);
            setAmount(filledStack, amount);
        }
        stackUpdate.accept(filledStack);
    }

    public boolean isEmpty(ItemStack stack) {
        return stack.getSubTag("BlockEntityTag") == null;
    }

    public Fluid getFluid(ItemStack stack) {
        return NbtHelper.getFluid(stack.getSubTag("BlockEntityTag"), "fluid");
    }
    private void setFluid(ItemStack stack, Fluid fluid) {
        NbtHelper.putFluid(stack.getSubTag("BlockEntityTag"), "fluid", fluid);
    }
    public int getAmount(ItemStack stack) {
        return stack.getSubTag("BlockEntityTag").getInt("amount");
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
            Fluid fluid = getFluid(stack);
            Identifier fluid_id = Registry.FLUID.getId(fluid);
            TranslatableText fluid_name = new TranslatableText("block." + fluid_id.getNamespace() + "." + fluid_id.getPath());
            tooltip.add(fluid_name.setStyle(style));
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
}

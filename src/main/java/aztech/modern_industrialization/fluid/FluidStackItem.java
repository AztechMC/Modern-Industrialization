package aztech.modern_industrialization.fluid;

import aztech.modern_industrialization.ModernIndustrialization;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;

/**
 * An evil hack to store a fluid/amount/capacity in an ItemStack.
 */
public class FluidStackItem extends Item {
    public static ItemStack getEmptyStack() {
        ItemStack stack = new ItemStack(ModernIndustrialization.ITEM_FLUID_SLOT);
        stack.setTag(new CompoundTag());
        setFluid(stack, Fluids.EMPTY);
        setAmount(stack, 0);
        setCapacity(stack, 0);
        return stack;
    }

    public FluidStackItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(new LiteralText("Stored fluid: " + getFluid(stack).getClass().getCanonicalName()));
        tooltip.add(new LiteralText(getAmount(stack) + " / " + getCapacity(stack) + " drops"));
        // TODO: improve this
    }

    public static Fluid getFluid(ItemStack stack) {
        return Registry.FLUID.get(stack.getTag().getInt("fluid_id"));
    }

    public static int getAmount(ItemStack stack) {
        return stack.getTag().getInt("fluid_amount");
    }

    public static int getCapacity(ItemStack stack) {
        return stack.getTag().getInt("fluid_capacity");
    }

    public static void setFluid(ItemStack stack, Fluid fluid) {
        stack.getTag().putInt("fluid_id", Registry.FLUID.getRawId(fluid));
    }

    public static void setAmount(ItemStack stack, int amount) {
        int capacity = getCapacity(stack);
        amount = Math.min(capacity, amount);
        stack.getTag().putInt("fluid_amount", amount);
        if(amount == 0) {
            setFluid(stack, Fluids.EMPTY);
        }
    }

    public static void setCapacity(ItemStack stack, int capacity) {
        stack.getTag().putInt("fluid_capacity", capacity);
    }
}

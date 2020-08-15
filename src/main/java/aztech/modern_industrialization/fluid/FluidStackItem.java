package aztech.modern_industrialization.fluid;

import aztech.modern_industrialization.ModernIndustrialization;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
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
        setIO(stack, FluidSlotIO.INPUT_AND_OUTPUT);
        return stack;
    }

    public FluidStackItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.clear(); // remove Fluid Slot
        if(getAmount(stack) > 0){
            Fluid fluid = getFluid(stack);
            Identifier fluid_id = Registry.FLUID.getId(fluid);
            TranslatableText fluid_name = new TranslatableText("block."+ fluid_id.getNamespace()+"."+ fluid_id.getPath());
            tooltip.add(fluid_name);
        }else{
            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_empty"));
        }
        String quantity = getAmount(stack) + " / " + getCapacity(stack);
        tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_quantity", quantity));

        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);

        switch(getIO(stack)){
            case INPUT_AND_OUTPUT:
                tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_IO").setStyle(style));
                break;
            case INPUT_ONLY:
                tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_input").setStyle(style));
                break;
            case OUTPUT_ONLY:
                tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_output").setStyle(style));
                break;
        }

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

    public static FluidSlotIO getIO(ItemStack stack){
        int fluid_io = stack.getTag().getInt("fluid_io");
        if(fluid_io == FluidSlotIO.INPUT_ONLY.getValue()){
            return FluidSlotIO.INPUT_ONLY;
        }else if(fluid_io == FluidSlotIO.OUTPUT_ONLY.getValue()){
            return FluidSlotIO.OUTPUT_ONLY;
        }else if(fluid_io == FluidSlotIO.INPUT_AND_OUTPUT.getValue()){
            return FluidSlotIO.INPUT_AND_OUTPUT;
        }else{
            return FluidSlotIO.INPUT_AND_OUTPUT; // defaut value
        }

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

    public static void setIO(ItemStack stack, FluidSlotIO fluid_io){
        stack.getTag().putInt("fluid_io", fluid_io.getValue());
    }
}

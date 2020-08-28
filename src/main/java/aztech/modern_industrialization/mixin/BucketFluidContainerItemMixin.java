package aztech.modern_industrialization.mixin;

import aztech.modern_industrialization.fluid.FluidContainerItem;
import aztech.modern_industrialization.fluid.FluidUnit;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

/**
 * A mixin making `BucketItem` implement `FluidContainerItem`.
 */
@Mixin(BucketItem.class)
public class BucketFluidContainerItemMixin implements FluidContainerItem {
    private static final Fluid EMPTY_FLUID = Fluids.EMPTY;

    @Shadow
    private Fluid fluid;

    @Override
    public int insertFluid(ItemStack stack, Fluid fluid, int maxAmount, Consumer<ItemStack> stackUpdate) {
        if(this.fluid == EMPTY_FLUID) {
            if(maxAmount >= FluidUnit.DROPS_PER_BUCKET) {
                if(stack.getCount() > 1) {
                    stackUpdate.accept(new ItemStack(stack.getItem(), stack.getCount() - 1));
                }
                stackUpdate.accept(new ItemStack(fluid.getBucketItem()));
                return FluidUnit.DROPS_PER_BUCKET;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public int extractFluid(ItemStack stack, Fluid fluid, int maxAmount, Consumer<ItemStack> stackUpdate) {
        if(fluid == this.fluid) {
            if(maxAmount >= FluidUnit.DROPS_PER_BUCKET) {
                stackUpdate.accept(new ItemStack(Items.BUCKET));
                return FluidUnit.DROPS_PER_BUCKET;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public Fluid getExtractableFluid(ItemStack stack) {
        return fluid == EMPTY_FLUID ? null : fluid;
    }
}

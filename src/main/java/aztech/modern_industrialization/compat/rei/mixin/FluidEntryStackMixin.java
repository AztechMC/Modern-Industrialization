package aztech.modern_industrialization.compat.rei.mixin;

import me.shedaniel.rei.impl.AbstractEntryStack;
import me.shedaniel.rei.impl.FluidEntryStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidEntryStack.class)
public abstract class FluidEntryStackMixin extends AbstractEntryStack {
    @Shadow
    private Fluid fluid;

    @Inject(method="asFormattedText", at=@At("HEAD"), cancellable = true, remap = false)
    public void asFormattedText(CallbackInfoReturnable<Text> ci) {
        try {
            ci.setReturnValue(fluid.getDefaultState().getBlockState().getBlock().getName());
            ci.cancel();
        } catch(NullPointerException ignored) {

        }
    }
}

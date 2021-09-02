package aztech.modern_industrialization.mixin.client;

import aztech.modern_industrialization.mixin_impl.MITooltipComponents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin {
	// Synthetic lambda body in renderTooltip
	@Inject(at = @At("HEAD"), method = "method_32635(Ljava/util/List;Lnet/minecraft/client/item/TooltipData;)V", cancellable = true)
	private static void injectRenderTooltipLambda(List<TooltipComponent> components, TooltipData data, CallbackInfo ci) {
		TooltipComponent component = MITooltipComponents.of(data);

		if (component != null) {
			components.add(1, component);
			ci.cancel();
		}
	}
}

package aztech.modern_industrialization.mixin;

import aztech.modern_industrialization.items.tools.QuantumSword;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerMixin {
   @Redirect(method = "attack", at = @At(target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", value = "INVOKE"))
   public double getAttributeValue(Player player, Attribute attribute) {
       if (player.getMainHandItem().getItem() instanceof QuantumSword) {
           // We just need this value to be positive
           return 1;
       }
       else {
           return player.getAttributeValue(attribute);
       }
   }
}

package aztech.modern_industrialization.mixin;

import aztech.modern_industrialization.items.tools.QuantumSword;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public abstract class MobMixin  {
    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    public boolean hurt(Entity target, DamageSource damageSource, float value) {
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof QuantumSword) {
            target.kill();
            return true;
        }
        else {
            return target.hurt(damageSource, value);
        }
    }

    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D"))
    public double getAttributeValue(Mob mob, Attribute attribute) {
        if (attribute == Attributes.ATTACK_DAMAGE && mob.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof QuantumSword) {
            // We just need this value to be positive
            return 1;
        }
        else {
            return mob.getAttributeValue(attribute);
        }
    }
}

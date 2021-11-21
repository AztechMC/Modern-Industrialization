/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.mixin;

import aztech.modern_industrialization.items.armor.MIArmorEffects;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @reason Prevent fly into wall and fall damage when wearing rubber armor.
 */
@Mixin(LivingEntity.class)
class LivingEntityMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSleeping()Z", shift = At.Shift.BEFORE, by = 1), method = "damage", cancellable = true)
    void injectDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Object tis = this;
        if (tis instanceof PlayerEntity player) {
            if (MIArmorEffects.quantumArmorPreventsDamage(player) || tryCancelDamage(source, amount, player)) {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * Return true if the damage is cancelled.
     */
    private static boolean tryCancelDamage(DamageSource source, float amount, PlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        // Find a suitable stack that can "tank" the damage
        ItemStack tankingStack = null;
        EquipmentSlot es = null;
        if (source == DamageSource.FLY_INTO_WALL) {
            es = EquipmentSlot.HEAD;
            ItemStack head = inventory.armor.get(es.getEntitySlotId());
            if (MIArmorEffects.canTankFlyIntoWall(head)) {
                tankingStack = head;
            }
        } else if (source == DamageSource.FALL) {
            es = EquipmentSlot.FEET;
            ItemStack head = inventory.armor.get(es.getEntitySlotId());
            if (MIArmorEffects.canTankFall(head)) {
                tankingStack = head;
            }
        }
        // Have the stack tank the damage
        if (tankingStack != null) {
            int intAmount = (int) Math.ceil(amount);
            final EquipmentSlot equipmentSlot = es;
            tankingStack.damage(intAmount, player, p -> player.sendEquipmentBreakStatus(equipmentSlot));
            return true;
        }
        return false;
    }
}
